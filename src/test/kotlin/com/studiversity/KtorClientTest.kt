package com.studiversity

import com.studiversity.client.di.apiModule
import com.studiversity.util.unwrapAsserted
import com.stuiversity.api.auth.AuthApi
import com.stuiversity.api.auth.model.SignInByEmailPasswordRequest
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
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.loadKoinModules
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class KtorClientTest : KoinTest {

    private lateinit var testApp: TestApplication
    lateinit var client: HttpClient
    val authApiOfGuest: AuthApi by inject { parametersOf(createGuestClient()) }

    @BeforeAll
    fun beforeAll(): Unit = runBlocking {
        testApp = TestApplication {
            buildApp()
        }
        testApp.start()
        client = createAuthenticatedClient("denchic150@gmail.com", "OBDIhi76534g33")
        setup()
    }

    @AfterAll
    fun afterAll() = runBlocking {
        cleanup()
        testApp.stop()
    }

    open fun setup() {}

    open fun TestApplicationBuilder.buildApp() {
        application { loadKoinModules(apiModule) }
    }

    open fun cleanup() {}

    fun createGuestClient() = testApp.createClient {
        installContentNegotiation()
    }

    fun createAuthenticatedClient(email: String, password: String) = testApp.createClient {
        installContentNegotiation()
        install(Auth) {
            bearer {
                refreshTokens {
                    val response = authApiOfGuest.signInByEmailPassword(SignInByEmailPasswordRequest(email, password))
                        .unwrapAsserted()
                    BearerTokens(accessToken = response.token, response.refreshToken)
                }
            }
        }
    }
}

private fun HttpClientConfig<out HttpClientEngineConfig>.installContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}