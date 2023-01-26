package com.studiversity.feature.auth

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.studiversity.feature.auth.model.SignupRequest
import com.studiversity.feature.auth.usecase.SignupUseCase
import com.studiversity.supabase.model.SignupGoTrueResponse
import com.studiversity.supabase.model.respondWithSupabaseError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.signupRoute() {
//    val userRepository: UserRepository by inject()
//    val supabaseClient by inject<HttpClient>()
    val signupUseCase: SignupUseCase by inject()

    post("/signup") {
        val signupRequest = call.receive<SignupRequest>()

        signupUseCase(signupRequest)
            .onFailure { call.respond(HttpStatusCode.fromValue(it.code), it) }
            .onSuccess { call.respond(HttpStatusCode.OK, it) }

//        supabaseClient.post("/auth/v1/signup") {
//            setBody(SignupGoTrueRequest(signupRequest.email, signupRequest.password))
//            contentType(ContentType.Application.Json)
//        }.apply {
//            if (status.isSuccess()) {
//                val body = body<SignupGoTrueResponse>()
//
//                userRepository.add(with(signupRequest) {
//                    User(body.userGoTrue.id, firstName, surname, patronymic, Account(email))
//                })
//
//                this@post.call.respond(
//                    TokenResponse(
//                        body.accessToken,
//                        body.refreshToken
//                    )
//                )
//            } else {
//                this@post.call.respondWithSupabaseError(this)
//            }
//        }
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
                val message = body<SignupGoTrueResponse>()
                this@post.call.respond(message)
            } else {
                this@post.call.respondWithSupabaseError(this)
            }
        }
    }
}