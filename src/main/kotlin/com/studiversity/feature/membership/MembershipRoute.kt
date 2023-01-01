package com.studiversity.feature.membership

import com.studiversity.feature.membership.model.JoinMemberRequest
import com.studiversity.feature.membership.usecase.RemoveMemberFromScopeUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.usecase.*
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

fun Route.membershipRoute() {
    route("/membership") {
        post("/{type}") {

        }
    }
}

fun Route.membersRoute(
    readMembersCapability: Capability,
    writeMembersCapability: Capability,
    onBefore: ApplicationCall.() -> Unit
) {
    route("/members") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val findMembersInScope: FindMembersInScopeUseCase by inject()
        val membershipService: MembershipService by inject()

        get {
            onBefore(call)
            val scopeId = call.parameters["id"]!!.toUUID()
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()

            requireCapability(currentUserId, readMembersCapability, scopeId)

            findMembersInScope(scopeId).apply {
                call.respond(HttpStatusCode.OK, this)
            }
        }
        post {
            onBefore(call)
            val currentUserId = call.jwtPrincipal().payload.claimId
            val scopeId = call.parameters["id"]!!.toUUID()

            val result = when (call.request.queryParameters["action"]!!) {
                "manual" -> {
                    val body = call.receive<JoinMemberRequest>()

                    requireCapability(currentUserId, writeMembersCapability, scopeId)

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
        memberByIdRoute(readMembersCapability, writeMembersCapability,onBefore)
    }
}

private fun Route.memberByIdRoute(
    readMembersCapability: Capability,
    writeMembersCapability: Capability,
    onBefore: (ApplicationCall) -> Unit
) {
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
            onBefore(call)
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()
            val scopeId = call.parameters["id"]!!.toUUID()
            val memberId = call.parameters["memberId"]!!.toUUID()

            requireCapability(currentUserId, writeMembersCapability, scopeId)

            removeMemberFromScope(memberId, scopeId)
            call.respond(HttpStatusCode.NoContent, "Member deleted")

        }
        memberRolesRoute(readMembersCapability, writeMembersCapability,onBefore)
    }
}

private fun Route.memberRolesRoute(
    readMembersCapability: Capability,
    writeMembersCapability: Capability,
    onBefore: (ApplicationCall) -> Unit
) {
    route("/roles") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by inject()
        val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by inject()
        val findAssignedUserRolesInScope: FindAssignedUserRolesInScopeUseCase by inject()
        val updateUserRolesInScope: UpdateUserRolesInScopeUseCase by inject()

        get {
            onBefore(call)
            val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()
            val scopeId = call.parameters["id"]!!.toUUID()
            val memberId = call.parameters["memberId"]!!.toUUID()

            requireCapability(currentUserId, readMembersCapability, scopeId)

            call.respond(HttpStatusCode.OK, findAssignedUserRolesInScope(memberId, scopeId))
        }
        put {
            onBefore(call)
            val currentUserId = call.jwtPrincipal().payload.claimId
            val scopeId = call.parameters["id"]!!.toUUID()
            val memberId = call.parameters["memberId"]!!.toUUID()
            val body = call.receive<UpdateUserRolesRequest>()

            requireCapability(currentUserId, writeMembersCapability, scopeId)

            val assignableRoles = body.roleIds

            requireAvailableRolesInScope(assignableRoles, scopeId)
            requirePermissionToAssignRoles(currentUserId, assignableRoles, scopeId)

            val updatedMember = updateUserRolesInScope(memberId, scopeId, body)
            call.respond(HttpStatusCode.OK, updatedMember)
        }
    }
}