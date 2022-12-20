package com.studiversity.feature.membership

import com.studiversity.feature.membership.model.Member
import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import java.util.*

interface Membership {
    val id: UUID
    val membershipRepository: MembershipRepository
    val userMembershipRepository: UserMembershipRepository
}

class SelfMembership(
    override val id: UUID,
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository
) : Membership {
    fun selfJoin(userId: UUID) {
        userMembershipRepository.addMember(userId, id)
    }
}

class ManualMembership(
    override val id: UUID,
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository
) : Membership

abstract class ExternalMembership : Membership {

    abstract fun onFirstGetMembers(): Flow<List<Member>>

    abstract fun onAddMembers(): Flow<List<Member>>

    abstract fun onRemoveMembers(): Flow<List<Member>>
}

class ExternalGroupMembership(
    override val id: UUID,
    override val membershipRepository: MembershipRepository,
    override val userMembershipRepository: UserMembershipRepository,
    private val groupId: UUID
) : ExternalMembership() {
    override fun onFirstGetMembers(): Flow<List<Member>> {
        return userMembershipRepository.findMembersByScope(groupId)
    }

    override fun onAddMembers(): Flow<List<Member>> =
        userMembershipRepository.findJoinTimestampOfLastMemberByScope(groupId).flatMapConcat { lastJoinTimestamp ->
            if (lastJoinTimestamp != null) {
                userMembershipRepository.findMembersByScopeAndGreaterJoinTimestamp(groupId, lastJoinTimestamp)
            } else emptyFlow()
        }


    override fun onRemoveMembers(): Flow<List<Member>> =
        userMembershipRepository.findLeaveTimestampOfLastMemberByScope(groupId).flatMapConcat { lastLeaveTimestamp ->
            userMembershipRepository.findMembersByScopeAndGreaterLeaveTimestamp(groupId, lastLeaveTimestamp)
        }
}