package com.studiversity.feature.membership

import com.studiversity.logger.logger
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun Application.configureMembership() {
    val membershipService: MembershipService by inject()
    val coroutineScope: CoroutineScope by inject()

    val memberships = membershipService.getGroupExternalMemberships().toMutableList()
    logger.info { "started ${memberships.size} memberships" }
    memberships.forEach(StudyGroupExternalMembership::init)

    coroutineScope.launch {
        membershipService.observeAddMemberships().collect {
            logger.info { "Added membership: ${it.membershipId}" }
            memberships.add(it.apply { init() })
            logger.info { "After adding membership total count: ${memberships.size}" }
        }
    }

    coroutineScope.launch {
        membershipService.observeRemoveMemberships().collect { oldMembershipId ->
            logger.info { "Removed membership: $oldMembershipId" }
            memberships.removeIf { it.membershipId == oldMembershipId }
            logger.info { "After deleting membership total count: ${memberships.size}" }
        }
    }
}

object MembershipErrors {
    const val USER_NOT_EXIST_IN_SCOPE = "USER_NOT_EXIST_IN_SCOPE"
    const val MEMBER_ALREADY_EXIST_IN_MEMBERSHIP = "MEMBER_ALREADY_EXIST_IN_MEMBERSHIP"
}