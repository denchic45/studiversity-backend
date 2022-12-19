package com.studiversity.feature.membership

import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import java.util.*
import kotlin.reflect.KClass

class MembershipService {

    val factory = mapOf<KClass<out Membership>, (membershipId: UUID) -> Membership>(
        ManualMembership::class to { ManualMembership(MembershipRepository(), UserMembershipRepository()) },
        SelfMembership::class to { SelfMembership(MembershipRepository(), UserMembershipRepository()) },
    )

    inline fun <reified T : Membership> getMembership(membershipId: UUID): T {
        return (factory[T::class]?.invoke(membershipId)
            ?: throw IllegalArgumentException("No membership dependency with type: ${T::class}")) as T
    }
}