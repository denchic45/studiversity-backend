package com.studiversity.feature.studygroup

import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.usecase.RequireAvailableRolesInScopeUseCase
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.role.usecase.RequirePermissionToAssignRolesUseCase
import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import com.studiversity.feature.studygroup.model.UpdateStudyGroupMemberRequest
import com.studiversity.feature.studygroup.usecase.EnrollStudyGroupMemberUseCase
import com.studiversity.feature.studygroup.usecase.FindStudyGroupMembersUseCase
import com.studiversity.feature.studygroup.usecase.RemoveStudyGroupMemberUseCase
import com.studiversity.feature.studygroup.usecase.UpdateStudyGroupMemberUseCase
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
        val enrollStudyGroupMember: EnrollStudyGroupMemberUseCase by inject()
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

            enrollStudyGroupMember(groupId, body.userId, assignableRoles)
            call.respond(HttpStatusCode.OK, "User has enrolled to study group")
        }

        groupMemberRoute()
    }
}

private fun Route.groupMemberRoute() {
    route("/{memberId}") {

        install(RequestValidation) {
            validate<UpdateStudyGroupMemberRequest> {
                buildValidationResult {
                    it.roles.ifPresent {
                        condition(it.isNotEmpty(), RoleErrors.NO_ROLE_ASSIGNMENT)
                    }
                }
            }
        }

        val requireCapability: RequireCapabilityUseCase by inject()
        val updateStudyGroupMember: UpdateStudyGroupMemberUseCase by inject()
        val removeStudyGroupMember: RemoveStudyGroupMemberUseCase by inject()

        patch {
            val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())
            val groupId = UUID.fromString(call.parameters["id"]!!)
            val memberId = UUID.fromString(call.parameters["memberId"]!!)
            val body = call.receive<UpdateStudyGroupMemberRequest>()

            requireCapability(currentUserId, Capability.EnrollMembersInGroup, groupId)

            updateStudyGroupMember(groupId, memberId, body)
        }
        delete {
            val currentUserId = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString())
            val groupId = UUID.fromString(call.parameters["id"]!!)
            val memberId = UUID.fromString(call.parameters["memberId"]!!)

            requireCapability(currentUserId, Capability.EnrollMembersInGroup, groupId)

            removeStudyGroupMember(groupId, memberId)
            call.respond(HttpStatusCode.NoContent, "Member deleted")
        }
    }
}