package com.studiversity.feature.membership

import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.membership.model.ManualJoinMemberRequest
import com.studiversity.feature.membership.model.ScopeMember
import com.studiversity.feature.studygroup.model.AcademicYear
import com.studiversity.feature.studygroup.model.CreateStudyGroupRequest
import com.studiversity.feature.studygroup.model.StudyGroupResponse
import com.studiversity.supabase.model.SignupResponse
import com.studiversity.util.toUUID
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CourseWithStudyGroupMembershipTest {

    var token = ""
    var refreshToken = ""

//    private var startData: StartData by Delegates.notNull()

    @Test
    fun testMembersOnAttachDetachStudyGroupsToCourse() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
//            install(Auth) {
//                basic {  }
//                bearer {
//                    loadTokens {
//                        // Load tokens from a local storage and return them as the 'BearerTokens' instance
//                        BearerTokens(token,refreshToken)
//                    }
//
//                    refreshTokens { this.client. }
//                }
//            }
        }

        token = client.post("/auth/token?grant_type=password") {
            setBody(
                """
                {
                  "email": "denchic150@gmail.com",
                  "password": "OBDIhi76534g33"
                }
            """.trimIndent()
            )
        }.body<SignupResponse>().accessToken

        val studyGroup1: StudyGroupResponse = client.post("/studygroups") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(CreateStudyGroupRequest("Test group 1", AcademicYear(2022, 2023)))
        }.body<StudyGroupResponse>().apply {
            assertEquals(name, "Test group 1")
        }

        val studyGroup2: StudyGroupResponse = client.post("/studygroups") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(CreateStudyGroupRequest("Test group 2", AcademicYear(2022, 2025)))
        }.body()

        val course: CourseResponse = client.post("/courses") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(CreateCourseRequest("Test course 1"))
        }.run {
            val body = body<CourseResponse>()
            assertEquals(body.name, "Test course 1")
            body
        }

//        startData = StartData(studyGroup1, studyGroup2, course)

        client.put("/courses/${course.id}/studygroups/${studyGroup1.id}") {
            authHeader()
        }
        client.put("/courses/${course.id}/studygroups/${studyGroup2.id}") {
            authHeader()
        }

        client.get("/courses/${course.id}/studygroups") {
            authHeader()
        }.body<List<String>>().apply {
            assertEquals(map(String::toUUID).toSet(), setOf(studyGroup1.id, studyGroup2.id))
        }

        val user1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
        val user2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()

        client.post("/studygroups/${studyGroup1.id}/members?action=manual") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user1Id, roleIds = listOf(3)))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        client.post("/studygroups/${studyGroup2.id}/members?action=manual") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user1Id, roleIds = listOf(3)))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }

        delay(15000)

        client.get("/courses/${course.id}/members") {
            authHeader()
            contentType(ContentType.Application.Json)
        }.body<List<ScopeMember>>()
            .apply {
                assertEquals(setOf(user1Id), map { it.userId }.toSet())
            }

        client.post("/studygroups/${studyGroup1.id}/members?action=manual") {
            authHeader()
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user2Id, roleIds = listOf(3)))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }

        delay(8000)

        client.get("/courses/${course.id}/members") {
            authHeader()
            contentType(ContentType.Application.Json)
        }.body<List<ScopeMember>>()
            .apply {
                assertEquals(setOf(user1Id, user2Id), map { it.userId }.toSet())
            }

        // delete first group and check members of course
        client.delete("/courses/${course.id}/studygroups/${studyGroup1.id}") {
            authHeader()
        }

        delay(8000)

        client.get("/courses/${course.id}/members") {
            authHeader()
            contentType(ContentType.Application.Json)
        }.body<List<ScopeMember>>()
            .apply {
                assertEquals(setOf(user1Id), map { it.userId }.toSet())
            }

        // delete second group and check members of course
        client.delete("/courses/${course.id}/studygroups/${studyGroup2.id}") {
            authHeader()
        }

        delay(8000)

        client.get("/courses/${course.id}/members") {
            authHeader()
            contentType(ContentType.Application.Json)
        }.body<List<ScopeMember>>()
            .apply {
                assertEquals(emptySet(), map { it.userId }.toSet())
            }
        clearData(client, studyGroup1, studyGroup2, course)
    }

    private suspend fun clearData(
        client: HttpClient,
        studyGroup1: StudyGroupResponse,
        studyGroup2: StudyGroupResponse,
        course: CourseResponse
    ) {
        // delete data
        assertEquals(
            HttpStatusCode.NoContent,
            client.delete("/studygroups/${studyGroup1.id}") { authHeader() }.status
        )
        assertEquals(
            HttpStatusCode.NoContent,
            client.delete("/studygroups/${studyGroup2.id}") { authHeader() }.status
        )
        assertEquals(
            HttpStatusCode.OK,
            client.put("/courses/${course.id}/archived") { authHeader() }.status
        )
        assertEquals(
            HttpStatusCode.NoContent,
            client.delete("/courses/${course.id}") { authHeader() }.status
        )
    }

    private fun HttpRequestBuilder.authHeader() {
        header("Authorization", "Bearer $token")
    }
}

private data class StartData(
    val studyGroup1: StudyGroupResponse,
    val studyGroup2: StudyGroupResponse,
    val course: CourseResponse
)