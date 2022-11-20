package com.studiversity

import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import com.studiversity.plugin.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureFeatures()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}