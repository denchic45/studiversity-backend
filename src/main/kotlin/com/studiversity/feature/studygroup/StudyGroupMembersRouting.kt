package com.studiversity.feature.studygroup

import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.usecase.*
import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.hasNotDuplicates
import com.studiversity.util.toUUID
import com.studiversity.validation.buildValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.studyGroupMembersRoute() {
    route("/members") {

        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val addUserToScope: AddUserToScopeUseCase by inject()
        val findUsersInScope: FindUsersInScopeUseCase by inject()

        get {
            val groupId = call.parameters["id"]!!.toUUID()
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()

            requireCapability(currentUserId, Capability.ReadGroup, groupId)

            findUsersInScope(groupId).apply {
                call.respond(HttpStatusCode.OK, this)
            }
        }
        post {
            val body = call.receive<EnrolStudyGroupMemberRequest>()
            val groupId = call.parameters["id"]!!.toUUID()
            val currentUserId = call.jwtPrincipal().payload.claimId

            requireCapability(currentUserId, Capability.WriteGroupMembers, groupId)

            val assignableRoles = body.roles

            requireAvailableRolesInScope(assignableRoles, groupId)
            requirePermissionToAssignRoles(currentUserId, assignableRoles, groupId)

            addUserToScope(body.userId, groupId, assignableRoles)
            call.respond(HttpStatusCode.OK, "User has enrolled to study group")
        }

        studyGroupMemberRoute()
    }
}

private fun Route.studyGroupMemberRoute() {
    route("/{memberId}") {
        install(RequestValidation) {
            validate<UpdateUserRolesRequest> {
                buildValidationResult {
                    condition(it.roles.isNotEmpty(), RoleErrors.NO_ROLE_ASSIGNMENT)
                    condition(it.roles.hasNotDuplicates(), RoleErrors.ROLES_DUPLICATION)
                }
            }
        }

        val requireCapability: RequireCapabilityUseCase by inject()
        val removeUserFromScope: RemoveUserFromScopeUseCase by inject()

        delete {
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()
            val groupId = call.parameters["id"]!!.toUUID()
            val memberId = call.parameters["memberId"]!!.toUUID()

            requireCapability(currentUserId, Capability.WriteGroupMembers, groupId)

            removeUserFromScope(memberId, groupId)
            call.respond(HttpStatusCode.NoContent, "Member deleted")
        }
        rolesRoute()
    }
}

private fun Route.rolesRoute() {
    route("/roles") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val findAssignedUserRolesInScope: FindAssignedUserRolesInScopeUseCase by inject()
        val updateUserRolesInScope: UpdateUserRolesInScopeUseCase by inject()

        get {
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()
            val groupId = call.parameters["id"]!!.toUUID()
            val memberId = call.parameters["memberId"]!!.toUUID()

            requireCapability(currentUserId, Capability.WriteGroupMembers, groupId)

            call.respond(HttpStatusCode.OK, findAssignedUserRolesInScope(memberId, groupId))
        }
        put {
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()
            val groupId = call.parameters["id"]!!.toUUID()
            val memberId = call.parameters["memberId"]!!.toUUID()
            val body = call.receive<UpdateUserRolesRequest>()

            requireCapability(currentUserId, Capability.WriteGroupMembers, groupId)

            val assignableRoles = body.roles

            requireAvailableRolesInScope(assignableRoles, groupId)
            requirePermissionToAssignRoles(currentUserId, assignableRoles, groupId)

            val updatedMember = updateUserRolesInScope(memberId, groupId, body)
            call.respond(HttpStatusCode.OK, updatedMember)
        }
    }
}