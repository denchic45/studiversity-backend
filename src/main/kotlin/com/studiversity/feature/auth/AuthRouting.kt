package com.studiversity.feature.auth

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.studiversity.di.OrganizationEnv
import com.studiversity.feature.auth.usecase.SignUpUseCase
import com.studiversity.ktor.ForbiddenException
import com.studiversity.supabase.model.SignUpGoTrueResponse
import com.studiversity.supabase.model.respondWithSupabaseError
import com.stuiversity.api.auth.model.SignupRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Route.signUpRoute() {
    val signup: SignUpUseCase by inject()
    val selfRegister: Boolean by inject(named(OrganizationEnv.ORG_SELF_REGISTER))

    post("/signup") {
        if (!selfRegister) throw ForbiddenException()
        val signupRequest = call.receive<SignupRequest>()

        signup(signupRequest)
            .onFailure { call.respond(HttpStatusCode.fromValue(it.code), it) }
            .onSuccess { call.respond(HttpStatusCode.OK, it) }
    }
}

fun Route.tokenRoute() {
    val supabaseClient by inject<HttpClient>()

    post("/token") {
        supabaseClient.post("/auth/v1/token") {
            setBody(call.receiveText())
            contentType(ContentType.Application.Json)
            parameter("grant_type", call.request.queryParameters["grant_type"])
        }.apply {
            if (status.isSuccess()) {
                val message = body<SignUpGoTrueResponse>()
                this@post.call.respond(message)
            } else {
                this@post.call.respondWithSupabaseError(this)
            }
        }
    }
}