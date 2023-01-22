package com.studiversity

import com.studiversity.client.di.apiModule
import com.studiversity.client.di.installContentNegotiation
import com.studiversity.feature.auth.model.LoginRequest
import com.studiversity.supabase.model.SignupResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class KtorTest : KoinTest {

    private lateinit var testApp: TestApplication
    lateinit var client: HttpClient

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

    fun createAuthenticatedClient(email: String, password: String) = testApp.createClient {
        installContentNegotiation()
        install(Auth) {
            bearer {
                refreshTokens {
                    BearerTokens(accessToken = client.post("/auth/token?grant_type=password") {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequest(email, password))
                    }.body<SignupResponse>().accessToken, "")
                }
            }
        }
    }

    @BeforeEach
    fun beforeEach() {
//        loadKoinModules(apiModule)
    }

}