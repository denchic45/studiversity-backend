package com.studiversity.feature.membership

import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import java.util.*
import kotlin.reflect.KClass

class MembershipService {

    val factory = mapOf<KClass<out Membership>, (membershipId: UUID) -> Membership>(
        ManualMembership::class to { id -> ManualMembership(id,MembershipRepository(), UserMembershipRepository()) },
        SelfMembership::class to { id -> SelfMembership(id,MembershipRepository(), UserMembershipRepository()) },
    )

    inline fun <reified T : Membership> getMembership(membershipId: UUID): T {
        return (factory[T::class]?.invoke(membershipId)
            ?: throw IllegalArgumentException("No membership dependency with type: ${T::class}")) as T
    }
}