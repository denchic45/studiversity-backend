package com.studiversity.feature.role

import com.studiversity.feature.role.usecase.CheckCapabilitiesByUserInScopeUseCase
import com.studiversity.ktor.getUserUuidByParamOrMe
import com.studiversity.ktor.getUuid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.capabilitiesRoutes() {
    route("/users/{userId}/scopes/{scopeId}/capabilities") {
        val checkCapabilitiesByUserInScope: CheckCapabilitiesByUserInScopeUseCase by inject()
        post("/check") {
            val userId = call.getUserUuidByParamOrMe("userId")
            val scopeId = call.parameters.getUuid("scopeId")
            val capabilities: List<String> = call.receive()

            call.respond(HttpStatusCode.OK, checkCapabilitiesByUserInScope(userId, scopeId, capabilities))
        }
    }
}