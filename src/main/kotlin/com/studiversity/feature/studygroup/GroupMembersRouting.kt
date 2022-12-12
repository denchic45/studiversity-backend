package com.studiversity.feature.studygroup

import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.FindRolesByNamesUseCase
import com.studiversity.feature.role.usecase.RequireAvailableRolesInScopeUseCase
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.role.usecase.RequirePermissionToAssignRolesUseCase
import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import com.studiversity.feature.studygroup.usecase.EnrollStudyGroupMemberUseCase
import com.studiversity.feature.studygroup.usecase.FindStudyGroupMembersUseCase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.groupMembersRoutes() {
    route("/members") {

        val requireCapability: RequireCapabilityUseCase by inject()
        val findRolesByNames: FindRolesByNamesUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val enrollStudyGroupMember: EnrollStudyGroupMemberUseCase by inject()
        val findStudyGroupMembers: FindStudyGroupMembersUseCase by inject()

        get {
            val groupId = UUID.fromString(call.parameters["id"]!!)
            val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())

            requireCapability(
                currentUserId,
                Capability.ViewGroup,
                groupId
            )

            findStudyGroupMembers(groupId).apply {
                call.respond(HttpStatusCode.OK, this)
            }
        }
        post {
            val body = call.receive<EnrolStudyGroupMemberRequest>()
            val groupId = UUID.fromString(call.parameters["id"]!!)
            val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())

            requireCapability(
                currentUserId,
                Capability.EnrollMembersInGroup,
                groupId
            )

            val assignableRoles = findRolesByNames(body.roles)

            requireAvailableRolesInScope(assignableRoles, groupId)
            requirePermissionToAssignRoles(currentUserId, assignableRoles, groupId)

            enrollStudyGroupMember(groupId, body.userId, assignableRoles)
            call.respond(HttpStatusCode.OK, "User has enrolled to study group")
        }

        groupMemberRoute()
    }
}

private fun Route.groupMemberRoute() {
    route("/{id}") {
        get { }
        put { }
        delete { }
    }
}