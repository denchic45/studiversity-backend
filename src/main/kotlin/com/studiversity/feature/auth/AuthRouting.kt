package com.studiversity.feature.auth

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.studiversity.di.OrganizationEnv
import com.studiversity.feature.auth.usecase.SignUpUseCase
import com.studiversity.ktor.ForbiddenException
import com.studiversity.supabase.model.SignUpGoTrueResponse
import com.studiversity.supabase.model.respondWithSupabaseError
import com.stuiversity.api.auth.model.ResetPasswordRequest
import com.stuiversity.api.auth.model.SignupRequest
import com.stuiversity.api.auth.model.TokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Route.signupRoute() {
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
            contentType(ContentType.Application.Json)
            setBody(call.receiveText())
            parameter("grant_type", call.request.queryParameters["grant_type"])
        }.apply {
            if (status.isSuccess()) {
                val message = body<SignUpGoTrueResponse>()
                this@post.call.respond(TokenResponse(message.accessToken, message.refreshToken))
            } else {
                this@post.call.respondWithSupabaseError(this)
            }
        }
    }
}

fun Route.recoverRoute() {
    val supabaseClient by inject<HttpClient>()
    post("/recover") {
        supabaseClient.post("/auth/v1/recover") {
            contentType(ContentType.Application.Json)
            setBody(call.receive<ResetPasswordRequest>())
        }.apply {
            if (status.isSuccess())
                this@post.call.respond(HttpStatusCode.OK,bodyAsText())
            else this@post.call.respond(status, bodyAsText())
        }
    }
}