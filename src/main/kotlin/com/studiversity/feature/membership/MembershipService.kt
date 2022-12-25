package com.studiversity.feature.membership

import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.repository.RoleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.reflect.KClass

class MembershipService(
    private val coroutineScope: CoroutineScope,
    private val membershipRepository: MembershipRepository,
    private val userMembershipRepository: UserMembershipRepository,
    private val roleRepository: RoleRepository,
) {

    private val factory = mapOf<KClass<out Membership>, (membershipId: UUID) -> Membership>(
        ManualMembership::class to { id -> ManualMembership(membershipRepository, userMembershipRepository, id) },
        SelfMembership::class to { id -> SelfMembership(membershipRepository, userMembershipRepository, id) },
        StudyGroupExternalMembership::class to { id ->
            StudyGroupExternalMembership(
                coroutineScope,
                membershipRepository,
                userMembershipRepository,
                roleRepository,
                id
            )
        }
    )

    private inline fun <reified T : Membership> getMembership(membershipId: UUID): T {
        return (factory[T::class]?.invoke(membershipId)
            ?: throw IllegalArgumentException("No membership dependency with type: ${T::class}")) as T
    }

    fun getGroupExternalMemberships(): List<StudyGroupExternalMembership> {
        return membershipRepository.findIdsByType("by_group")
            .map { membershipId -> getMembership(membershipId) }
    }

    fun observeAddMemberships(): Flow<StudyGroupExternalMembership> {
        return membershipRepository.observeAddExternalStudyGroupMemberships()
            .map { getMembership(it) }
    }

    fun observeRemoveMemberships(): Flow<UUID> {
        return membershipRepository.observeRemoveExternalStudyGroupMemberships()
    }
}