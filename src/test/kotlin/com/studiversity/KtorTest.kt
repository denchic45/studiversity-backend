package com.studiversity

import com.studiversity.feature.auth.model.LoginRequest
import com.studiversity.supabase.model.SignupResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

open class KtorTest {

    companion object {
        lateinit var testApp: TestApplication
        lateinit var client: HttpClient

        @JvmStatic
        @BeforeAll
        fun setup(): Unit = runBlocking {
            testApp = TestApplication {}
            client = testApp.createClient {
                installContentNegotiation()
                install(Auth) {
                    bearer {
                        refreshTokens {
                            BearerTokens(accessToken = client.post("/auth/token?grant_type=password") {
                                contentType(ContentType.Application.Json)
                                setBody(LoginRequest("denchic150@gmail.com", "OBDIhi76534g33"))
                            }.body<SignupResponse>().accessToken, "")
                        }
                    }
                }
            }
        }

        fun HttpClientConfig<out HttpClientEngineConfig>.installContentNegotiation() {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }


        @JvmStatic
        @AfterAll
        fun teardown() = runBlocking {
            testApp.stop()
        }
    }
}