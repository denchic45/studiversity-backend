package com.studiversity.feature.membership.controller

import com.studiversity.feature.membership.MembershipService
import com.studiversity.feature.membership.model.Member
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.membership.usecase.AddUserToMembershipUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.FindUsersInScopeUseCase
import com.studiversity.feature.role.usecase.RequireAvailableRolesInScopeUseCase
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.role.usecase.RequirePermissionToAssignRolesUseCase
import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import java.util.*

class ManualMembershipController(
    override val call: ApplicationCall,
    override val scopeId: UUID,
    override val currentUserId: UUID
) : MembershipController {

    val userMembershipRepository: UserMembershipRepository by call.inject()
    val requireCapability: RequireCapabilityUseCase by call.inject()
    val requireAvailableRolesInScope: RequireAvailableRolesInScopeUseCase by call.inject()
    val requirePermissionToAssignRoles: RequirePermissionToAssignRolesUseCase by call.inject()
    val addUserToMembershipUseCase: AddUserToMembershipUseCase by call.inject()
    val findUsersInScope: FindUsersInScopeUseCase by call.inject()
    val membershipService: MembershipService by call.inject()

    override suspend fun invoke() {
        val body = call.receive<EnrolStudyGroupMemberRequest>()

        requireCapability(currentUserId, Capability.WriteGroupMembers, scopeId)

        val assignableRoles = body.roles

        requireAvailableRolesInScope(assignableRoles, scopeId)
        requirePermissionToAssignRoles(currentUserId, assignableRoles, scopeId)

        addUserToMembershipUseCase(Member(body.userId /*membership id*/), assignableRoles, scopeId)
        call.respond(
            HttpStatusCode.OK,
            "User has enrolled to study group"
        ) //TODO вместо этого возращать роли участника в членстве и другую информацию
    }
}