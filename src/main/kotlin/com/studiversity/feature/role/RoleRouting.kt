package com.studiversity.feature.role

import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.usecase.*
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.hasNotDuplicates
import com.studiversity.util.toUUID
import com.studiversity.validation.buildValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userAssignedRolesRoute() {
    route("users/{id}/roles") {
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
        val updateUserRolesInScope: UpdateUserRolesInScopeUseCase by inject()

        get {
            val userId = call.parameters["id"]!!.toUUID()
            val scopeId = call.request.queryParameters["scopeId"]?.toUUID()
                ?: throw BadRequestException("SCOPE_ID_IS_REQUIRED")
            //TODO add capability to read user roles
            call.respond(HttpStatusCode.OK, findAssignedUserRolesInScope(userId, scopeId))
        }

        put {
            val userId = call.parameters["id"]!!.toUUID()
            val scopeId = call.request.queryParameters["scopeId"]?.toUUID()
                ?: throw BadRequestException("SCOPE_ID_IS_REQUIRED")
            val body = call.receive<UpdateUserRolesRequest>()

            val currentUserId = call.jwtPrincipal().payload.claimId
            requireCapability(currentUserId, Capability.WriteAssignRoles, scopeId)
            val assignableRoles = body.roleIds

            requireAvailableRolesInScope(assignableRoles, scopeId)
            requirePermissionToAssignRoles(currentUserId, assignableRoles, scopeId)

            updateUserRolesInScope(userId, body, scopeId).let { response ->
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}