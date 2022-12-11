package com.studiversity.feature.studygroup

import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireAllowRoleInScopeUseCase
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.groupMembersRoutes() {
    route("/members") {

        val requireCapability: RequireCapabilityUseCase by inject()
        val requireAllowRoleInScopeUseCase: RequireAllowRoleInScopeUseCase by inject()

        get { }
        post {
            val body = call.receive<EnrolStudyGroupMemberRequest>()
            val groupId = UUID.fromString(call.parameters["id"]!!)
            requireCapability(
                UUID.fromString(call.principal<JWTPrincipal>()!!.payload.getClaim("id").asString()),
                Capability.EnrollMembersInGroup,
                groupId
            )
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