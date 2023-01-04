package com.studiversity.feature.membership

import com.studiversity.feature.membership.model.ManualJoinMemberRequest
import com.studiversity.feature.membership.model.Member
import com.studiversity.feature.membership.model.ScopeMember
import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.Role
import com.studiversity.feature.role.repository.RoleRepository
import com.studiversity.logger.logger
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

abstract class Membership {
    abstract val transactionWorker: TransactionWorker
    abstract val membershipRepository: MembershipRepository
    abstract val userMembershipRepository: UserMembershipRepository
    abstract val roleRepository: RoleRepository
    abstract val membershipId: UUID

    val scopeId by lazy { membershipRepository.findScopeIdByMembershipId(membershipId) }
}

class SelfMembership(
    override val transactionWorker: TransactionWorker,
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository,
    override val roleRepository: RoleRepository,
    override val membershipId: UUID,
) : Membership() {

    fun selfJoin(userId: UUID) {
        userMembershipRepository.addMember(Member(userId, membershipId))
    }
}

class ManualMembership(
    override val transactionWorker: TransactionWorker,
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository,
    override val roleRepository: RoleRepository,
    override val membershipId: UUID,
) : Membership() {

    fun joinMember(manualJoinMemberRequest: ManualJoinMemberRequest): ScopeMember = transactionWorker {
        logger.debug { "JOIN MEMBER: ${manualJoinMemberRequest.userId} TO MEMBERSHIP: $membershipId" }
        val userId = manualJoinMemberRequest.userId
        if (userMembershipRepository.existMember(userId, membershipId))
            throw BadRequestException(MembershipErrors.MEMBER_ALREADY_EXIST_IN_MEMBERSHIP)
        userMembershipRepository.addMember(
            Member(userId = userId, membershipId = membershipId)
        )
        roleRepository.addUserRolesToScope(userId, manualJoinMemberRequest.roleIds, scopeId)
        userMembershipRepository.findMemberByScope(userId, scopeId)
    }
}

abstract class ExternalMembership(private val coroutineScope: CoroutineScope) : Membership() {

    fun init() {
        syncMembers()
        coroutineScope.launch {
            onAddMembers().collect {
                logger.info { "add members: $it in membership: $membershipId" }
                if (it.isNotEmpty())
                    userMembershipRepository.addMembersToMembership(it, membershipId)
            }
        }
        coroutineScope.launch {
            onRemoveMembers().collect {
                logger.info { "remove members: $it in membership: $membershipId instance: ${this@ExternalMembership}" }
                if (it.isNotEmpty())
                    userMembershipRepository.removeMembersFromMembership(it, membershipId)
            }
        }
        coroutineScope.launch {
            userMembershipRepository.listenTestRealtime()
        }
    }

    private fun syncMembers() {
        coroutineScope.launch {
            onSyncGetAddedMembers().collect {
                logger.info { "first add members: $it in membership: $membershipId" }
                if (it.isNotEmpty())
                    userMembershipRepository.addMembersToMembership(it, membershipId)
            }
        }
        coroutineScope.launch {
            onSyncGetRemovedMembers().collect {
                logger.info { "first remove members: $it in membership: $membershipId" }
                if (it.isNotEmpty())
                    userMembershipRepository.removeMembersFromMembership(it, membershipId)
            }
        }
    }

    abstract suspend fun onSyncGetAddedMembers(): Flow<List<UUID>>

    abstract suspend fun onSyncGetRemovedMembers(): Flow<List<UUID>>

    abstract fun onAddMembers(): Flow<List<UUID>>

    abstract fun onRemoveMembers(): Flow<List<UUID>>
}

class StudyGroupExternalMembership(
    coroutineScope: CoroutineScope,
    override val transactionWorker: TransactionWorker,
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository,
    override val roleRepository: RoleRepository,
    override val membershipId: UUID,
) : ExternalMembership(coroutineScope) {

    private val groupIds: StateFlow<List<UUID>> =
        membershipRepository.observeStudyGroupIdsOfExternalMembershipsByMembershipId(membershipId)

    private val observeStudyGroupMembershipsByScopeId = groupIds.flatMapLatest {
        logger.debug { "FLAT MAP GROUP IDS: $it" }
        membershipRepository.observeMembershipsByScopeIds(it)
    }.shareIn(coroutineScope, SharingStarted.Lazily)

    override suspend fun onSyncGetAddedMembers(): Flow<List<UUID>> {
        return observeStudyGroupMembershipsByScopeId.map {
            userMembershipRepository.findUnrelatedMembersByManyToOneMemberships(
                it,
                membershipId
            )
        }
    }

    override suspend fun onSyncGetRemovedMembers(): Flow<List<UUID>> {
        return observeStudyGroupMembershipsByScopeId.map {
            userMembershipRepository.findUnrelatedMembersByOneToManyMemberships(
                membershipId,
                it
            )
        }
    }

    override fun onAddMembers(): Flow<List<UUID>> {
        return observeStudyGroupMembershipsByScopeId.flatMapLatest { membershipIds ->
            membershipIds.map { membershipId ->
                userMembershipRepository.observeAddedMembersByMembershipId(membershipId)
                    .map { Member(it, membershipId) }
            }.merge()
                .filter { member ->
                    roleRepository.hasRoleIn(member.userId, Role.Student, groupIds.value)
                            && !userMembershipRepository.existMember(member.userId, membershipId)
                }.map { listOf(it.userId) }
        }
    }

    override fun onRemoveMembers(): Flow<List<UUID>> {
        return observeStudyGroupMembershipsByScopeId.flatMapLatest { membershipIds ->
            membershipIds.map { membershipId ->
                userMembershipRepository.observeRemovedMembersByMembershipId(membershipId)
            }.merge()
                .filterNot { userMembershipRepository.existMemberByScopeIds(it, groupIds.value) }
                .map { listOf(it) }
        }
    }
}