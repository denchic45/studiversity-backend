package com.studiversity.feature.role

import com.studiversity.feature.role.usecase.*
import com.studiversity.ktor.currentUserId
import com.studiversity.ktor.getUuid
import com.studiversity.util.hasNotDuplicates
import com.studiversity.validation.buildValidationResult
import com.stuiversity.api.role.model.Capability
import com.stuiversity.api.role.model.UpdateUserRolesRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject

fun Application.rolesRoutes() {
    routing {
        authenticate("auth-jwt") {
            userAssignedRolesRoute()
            capabilitiesRoutes()
        }
    }
}

private fun Route.userAssignedRolesRoute() {
    route("/users/{id}/scopes/{scopeId}/roles") {
        install(RequestValidation) {
            validate<UpdateUserRolesRequest> {
                buildValidationResult {
                    condition(it.roleIds.isNotEmpty(), RoleErrors.NO_ROLE_ASSIGNMENT)
                    condition(it.roleIds.hasNotDuplicates(), RoleErrors.ROLES_DUPLICATION)
                }
            }
        }
        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val findAssignedUserRolesInScope: FindAssignedUserRolesInScopeUseCase by inject()
        val putRoleToUserInScope: PutRoleToUserInScopeUseCase by inject()
        val removeRoleFromUserInScope: RemoveRoleFromUserInScopeUseCase by inject()

        get {
            val userId = call.parameters.getUuid("id")
            val scopeId = call.parameters.getUuid("scopeId")
            call.respond(HttpStatusCode.OK, findAssignedUserRolesInScope(userId, scopeId))
        }

        route("/{roleId}") {
            put {
                val userId = call.parameters.getUuid("id")
                val roleId = call.parameters.getOrFail("roleId").toLong()
                val scopeId = call.parameters.getUuid("scopeId")
                val currentUserId = call.currentUserId()

                requireCapability(currentUserId, Capability.WriteAssignRoles, scopeId)

                requireAvailableRolesInScope(listOf(roleId), scopeId)
                requirePermissionToAssignRoles(currentUserId, listOf(roleId), scopeId)

                putRoleToUserInScope(userId, roleId, scopeId)
                call.respond(HttpStatusCode.OK)
            }
            delete {
                val userId = call.parameters.getUuid("id")
                val roleId = call.parameters.getOrFail("roleId").toLong()
                val scopeId = call.parameters.getUuid("scopeId")
                val currentUserId = call.currentUserId()

                requireCapability(currentUserId, Capability.WriteAssignRoles, scopeId)

                requirePermissionToAssignRoles(currentUserId, listOf(roleId), scopeId)

                removeRoleFromUserInScope(userId, roleId, scopeId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}