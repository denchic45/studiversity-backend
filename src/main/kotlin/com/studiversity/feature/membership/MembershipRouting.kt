package com.studiversity.feature.membership

import com.studiversity.feature.membership.model.ManualJoinMemberRequest
import com.studiversity.feature.membership.usecase.RemoveMemberFromScopeUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.usecase.FindMembersInScopeUseCase
import com.studiversity.feature.role.usecase.RequireAvailableRolesInScopeUseCase
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.role.usecase.RequirePermissionToAssignRolesUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.hasNotDuplicates
import com.studiversity.util.toUUID
import com.studiversity.validation.buildValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.membershipRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/scopes/{scopeId}") {
                membersRoute()
            }
            route("/membership") {
                post("/{type}") {

                }
            }
        }
    }
}

fun Route.membersRoute() {
    route("/members") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val findMembersInScope: FindMembersInScopeUseCase by inject()
        val membershipService: MembershipService by inject()

        get {
            val scopeId = call.parameters["scopeId"]!!.toUUID()
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()

            requireCapability(currentUserId, Capability.ReadMembers, scopeId)

            findMembersInScope(scopeId).apply {
                call.respond(HttpStatusCode.OK, this)
            }
        }
        post {
            val currentUserId = call.jwtPrincipal().payload.claimId
            val scopeId = call.parameters["scopeId"]!!.toUUID()

            val result = when (call.request.queryParameters["action"]!!) {
                "manual" -> {
                    val body = call.receive<ManualJoinMemberRequest>()

                    requireCapability(currentUserId, Capability.WriteMembers, scopeId)

                    val assignableRoles = body.roleIds
                    requireAvailableRolesInScope(assignableRoles, scopeId)
                    requirePermissionToAssignRoles(currentUserId, assignableRoles, scopeId)

                    membershipService
                        .getMembershipByTypeAndScopeId<ManualMembership>("manual", scopeId)
                        .joinMember(body)
                }

//                "selfJoin" -> {}
                else -> throw BadRequestException("UNKNOWN_MEMBERSHIP_ACTION")
            }
            call.respond(HttpStatusCode.Created, result)
        }
        memberByIdRoute()
    }
}

private fun Route.memberByIdRoute() {
    route("/{memberId}") {
        install(RequestValidation) {
            validate<UpdateUserRolesRequest> {
                buildValidationResult {
                    condition(it.roleIds.isNotEmpty(), RoleErrors.NO_ROLE_ASSIGNMENT)
                    condition(it.roleIds.hasNotDuplicates(), RoleErrors.ROLES_DUPLICATION)
                }
            }
        }

        val requireCapability: RequireCapabilityUseCase by inject()
        val removeMemberFromScope: RemoveMemberFromScopeUseCase by inject()

        delete {
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()
            val scopeId = call.parameters["scopeId"]!!.toUUID()
            val memberId = call.parameters["memberId"]!!.toUUID()

            requireCapability(currentUserId, Capability.WriteMembers, scopeId)

            removeMemberFromScope(memberId, scopeId)
            call.respond(HttpStatusCode.NoContent, "Member deleted")

        }
    }
}