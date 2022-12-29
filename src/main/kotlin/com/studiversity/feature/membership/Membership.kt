package com.studiversity.feature.membership

import com.studiversity.feature.membership.model.Member
import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.Role
import com.studiversity.feature.role.repository.RoleRepository
import com.studiversity.logger.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

interface Membership {
    val membershipRepository: MembershipRepository
    val userMembershipRepository: UserMembershipRepository
    val membershipId: UUID
}

class SelfMembership(
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository,
    override val membershipId: UUID,
) : Membership {
    fun selfJoin(userId: UUID) {
        userMembershipRepository.addMember(Member(userId, membershipId))
    }
}

class ManualMembership(
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository,
    override val membershipId: UUID,
) : Membership

abstract class ExternalMembership(private val coroutineScope: CoroutineScope) : Membership {

    fun init() {
        syncMembers()
        coroutineScope.launch {
            onAddMember().collect {
                logger.info { "add members: $this in membership: $membershipId" }
                if (it.isNotEmpty())
                    userMembershipRepository.addMembersToMembership(it, membershipId)
            }
        }
        coroutineScope.launch {
            onRemoveMember().collect {
                logger.info { "remove members: $this in membership: $membershipId" }
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
                logger.info { "first add members: $this in membership: $membershipId" }
                if (it.isNotEmpty())
                    userMembershipRepository.addMembersToMembership(it, membershipId)
            }
        }
        coroutineScope.launch {
            onSyncGetRemovedMembers().collect {
                logger.info { "first remove members: $this in membership: $membershipId" }
                if (it.isNotEmpty())
                    userMembershipRepository.removeMembersFromMembership(it, membershipId)
            }
        }
    }

    abstract suspend fun onSyncGetAddedMembers(): Flow<List<UUID>>

    abstract suspend fun onSyncGetRemovedMembers(): Flow<List<UUID>>

    abstract fun onAddMember(): Flow<List<UUID>>

    abstract fun onRemoveMember(): Flow<List<UUID>>
}

class StudyGroupExternalMembership(
    coroutineScope: CoroutineScope,
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository,
    private val roleRepository: RoleRepository,
    override val membershipId: UUID,
) : ExternalMembership(coroutineScope) {

    private val groupIds: StateFlow<List<UUID>> =
        membershipRepository.observeStudyGroupIdsOfExternalMembershipsByMembershipId(membershipId)

    private val observeStudyGroupMembershipsByScopeId = groupIds.flatMapConcat {
        membershipRepository.observeMembershipsByScopeIds(it)
    }

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

    override fun onAddMember(): Flow<List<UUID>> {
        return observeStudyGroupMembershipsByScopeId
            .flatMapConcat { membershipIds ->
                //TODO Слушать отдельно каждый membership и брать из него членов.
                // Переиспользовать также для прослушивания удаленных пользователей.
                // Не допускать добавления уже существующего члена!

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


    @OptIn(FlowPreview::class)
    override fun onRemoveMember(): Flow<List<UUID>> {
        //TODO Перед удалением члена, убедиться, что его нет в других членствах других групп (scopes), иначе ничего не делать!
        return observeStudyGroupMembershipsByScopeId.flatMapConcat { membershipIds ->
            membershipIds.map { membershipId ->
                userMembershipRepository.observeRemovedMembersByMembershipId(membershipId)
            }.merge()
                .filterNot { userMembershipRepository.existMemberByScopeIds(it, groupIds.value) }
                .map { listOf(it) }

//                .map {
//                userMembershipRepository.findUnrelatedMembersByOneToManyMemberships(
//                    membershipId,
//                    observeMembershipsByScopeId.first()
//                )
//            }
        }
    }
}