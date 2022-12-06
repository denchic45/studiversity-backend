package com.studiversity.feature.auth

import com.studiversity.feature.auth.model.CreateUserRequest
import com.studiversity.feature.auth.model.TokenResponse
import com.studiversity.feature.role.UserRepository
import com.studiversity.feature.user.User
import com.studiversity.supabase.model.SignupResponse
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
import java.util.*

fun Route.signupRoute() {
    val userRepository: UserRepository by inject()
    val supabaseClient by inject<HttpClient>()

    post("/signup") {
        println()
        val createUserRequest = call.receive<CreateUserRequest>()

        supabaseClient.post("/auth/v1/signup") {
            setBody(createUserRequest)
            contentType(ContentType.Application.Json)
        }.apply {
            if (status.isSuccess()) {
                val body = body<SignupResponse>()

                userRepository.add(with(createUserRequest) {
                    User(UUID.fromString(body.user.id), firstName, surname, patronymic, email)
                })

                this@post.call.respond(
                    TokenResponse(
                        body.accessToken,
                        body.refreshToken
                    )
                )
            } else {
                this@post.call.respondWithSupabaseError(this)
            }
        }
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
                val message = body<SignupResponse>()
                this@post.call.respond(message)
            } else {
                this@post.call.respondWithSupabaseError(this)
            }
        }
    }
}