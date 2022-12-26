package com.studiversity.feature.course

import com.studiversity.feature.membership.model.EnrolMemberRequest
import com.studiversity.feature.membership.model.Member
import com.studiversity.feature.membership.usecase.AddUserToMembershipUseCase
import com.studiversity.feature.membership.usecase.RemoveUserFromMembershipUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.FindUsersInScopeUseCase
import com.studiversity.feature.role.usecase.RequireAvailableRolesInScopeUseCase
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.role.usecase.RequirePermissionToAssignRolesUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.toUUID
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.courseMembersRoute() {
    route("/members") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val addUserToMembership: AddUserToMembershipUseCase by inject()
        val removeUserFromMembershipUseCase: RemoveUserFromMembershipUseCase by inject()
        val findUsersInScope: FindUsersInScopeUseCase by inject()

        post {
            //TODO Заменить всю копипасту
            val body = call.receive<EnrolMemberRequest>()
            val courseId = call.parameters["id"]!!.toUUID()
            val currentUserId = call.jwtPrincipal().payload.claimId

            requireCapability(currentUserId, Capability.WriteGroupMembers, courseId)

            val assignableRoles = body.roles

            requireAvailableRolesInScope(assignableRoles, courseId)
            requirePermissionToAssignRoles(currentUserId, assignableRoles, courseId)

            addUserToMembership(
                Member(id = body.userId, membershipId = TODO("Получать membership курса для ручного зачисления")),
                assignableRoles,
                courseId
            )
            call.respond(HttpStatusCode.OK, "User has enrolled to study group")
        }
        get {

        }
    }
}