package com.studiversity.feature.membership

import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import java.util.*

interface Membership {
    val membershipRepository: MembershipRepository
    val userMembershipRepository: UserMembershipRepository

    val id: UUID
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