package com.studiversity.feature.studygroup

import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.usecase.*
import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import com.studiversity.feature.studygroup.usecase.FindStudyGroupMembersUseCase
import com.studiversity.util.hasNotDuplicates
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
import java.util.*

fun Route.groupMembersRoutes() {
    route("/members") {

        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val addUserToScope: AddUserToScopeUseCase by inject()
        val findStudyGroupMembers: FindStudyGroupMembersUseCase by inject()

        get {
            val groupId = UUID.fromString(call.parameters["id"]!!)
            val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())

            requireCapability(currentUserId, Capability.ViewGroup, groupId)

            findStudyGroupMembers(groupId).apply {
                call.respond(HttpStatusCode.OK, this)
            }
        }
        post {
            val body = call.receive<EnrolStudyGroupMemberRequest>()
            val groupId = UUID.fromString(call.parameters["id"]!!)
            val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())

            requireCapability(currentUserId, Capability.EnrollMembersInGroup, groupId)

            val assignableRoles = body.roles

            requireAvailableRolesInScope(assignableRoles, groupId)
            requirePermissionToAssignRoles(currentUserId, assignableRoles, groupId)

            addUserToScope(body.userId, groupId, assignableRoles)
            call.respond(HttpStatusCode.OK, "User has enrolled to study group")
        }

        groupMemberRoute()
    }
}

private fun Route.groupMemberRoute() {
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
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val updateStudyGroupMember: UpdateUserRolesInScopeUseCase by inject()
        val removeUserFromScope: RemoveUserFromScopeUseCase by inject()
        val findAssignedUserRolesInScope: FindAssignedUserRolesInScopeUseCase by inject()

        route("/roles") {
            get {
                val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())
                val groupId = UUID.fromString(call.parameters["id"]!!)
                val memberId = UUID.fromString(call.parameters["memberId"]!!)

                requireCapability(currentUserId, Capability.EnrollMembersInGroup, groupId)

                call.respond(HttpStatusCode.OK, findAssignedUserRolesInScope(memberId, groupId))
            }
            put {
                val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())
                val groupId = UUID.fromString(call.parameters["id"]!!)
                val memberId = UUID.fromString(call.parameters["memberId"]!!)
                val body = call.receive<UpdateUserRolesRequest>()

                requireCapability(currentUserId, Capability.EnrollMembersInGroup, groupId)

                val assignableRoles = body.roles

                requireAvailableRolesInScope(assignableRoles, groupId)
                requirePermissionToAssignRoles(currentUserId, assignableRoles, groupId)

                val updatedMember = updateStudyGroupMember.updateUserRolesInScope(memberId, groupId, body)
                call.respond(HttpStatusCode.OK, updatedMember)
            }
        }
        delete {
            val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())
            val groupId = UUID.fromString(call.parameters["id"]!!)
            val memberId = UUID.fromString(call.parameters["memberId"]!!)

            requireCapability(currentUserId, Capability.EnrollMembersInGroup, groupId)

            removeUserFromScope(memberId, groupId)
            call.respond(HttpStatusCode.NoContent, "Member deleted")
        }
    }
}