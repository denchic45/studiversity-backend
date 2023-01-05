package com.studiversity.feature.membership

import com.studiversity.feature.auth.model.LoginRequest
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
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals

class CourseWithStudyGroupMembershipTest {

    companion object {
        private lateinit var testApp: TestApplication
        lateinit var client: HttpClient
        lateinit var startData: StartData

        @JvmStatic
        @BeforeAll
        fun setup(): Unit = runBlocking {
            testApp = TestApplication {}
            client = testApp.createClient {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    })
                }
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

        @JvmStatic
        @AfterAll
        fun teardown() = runBlocking {
            testApp.stop()
        }
    }

    @Test
    fun testMembersOnAttachDetachStudyGroupsToCourse(): Unit = runBlocking {
        val (studyGroup1: StudyGroupResponse, studyGroup2: StudyGroupResponse, course: CourseResponse) = startData

        attachGroupsToCourse(client, course, studyGroup1, studyGroup2)

        client.get("/courses/${course.id}/studygroups")
            .body<List<String>>().apply {
                assertEquals(listOf(studyGroup1.id, studyGroup2.id).sorted(), map(String::toUUID).sorted())
            }

        val user1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
        val user2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()

        enrolStudentsToGroups(client, studyGroup1, studyGroup2, user2Id, user1Id)

        client.get("/courses/${course.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(listOf(user1Id, user2Id).sorted(), map(ScopeMember::userId).sorted())
        }

        // delete first group and check members of course
        client.delete("/courses/${course.id}/studygroups/${studyGroup1.id}")

        delay(8000)

        client.get("/courses/${course.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(listOf(user1Id), map(ScopeMember::userId))
        }

        // delete second group and check members of course
        client.delete("/courses/${course.id}/studygroups/${studyGroup2.id}")

        delay(8000)

        client.get("/courses/${course.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(emptyList(), map(ScopeMember::userId))
        }
    }

    @Test
    fun testMembersOnEnrolUnrollFromGroupsInCourse(): Unit = runBlocking {

        val (studyGroup1: StudyGroupResponse, studyGroup2: StudyGroupResponse, course: CourseResponse) = startData

        val user1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
        val user2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()

        enrolStudentsToGroups(client, studyGroup1, studyGroup2, user2Id, user1Id)
        attachGroupsToCourse(client, course, studyGroup1, studyGroup2)

        client.get("/courses/${course.id}/studygroups").body<List<String>>().apply {
            assertEquals(listOf(studyGroup1.id, studyGroup2.id).sorted(), map(String::toUUID).sorted())
        }
        client.get("/courses/${course.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(listOf(user1Id, user2Id).sorted(), map(ScopeMember::userId).sorted())
        }

        // delete first user from first group
        client.delete("/studygroups/${studyGroup1.id}/members/$user1Id")
        delay(8000)

        // assert only second member in first group
        client.get("/studygroups/${studyGroup1.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(listOf(user2Id), map(ScopeMember::userId))
        }
        // assert two members of course
        client.get("/courses/${course.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(listOf(user1Id, user2Id).sorted(), map(ScopeMember::userId).sorted())
        }

        // delete second user from first group
        client.delete("/studygroups/${studyGroup1.id}/members/$user2Id")
        delay(8000)

        // assert zero members in first group
        client.get("/studygroups/${studyGroup1.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(emptyList(), map(ScopeMember::userId))
        }
        // assert only first member of course
        client.get("/courses/${course.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(listOf(user1Id), map(ScopeMember::userId))
        }

        // delete first user from second group
        client.delete("/studygroups/${studyGroup2.id}/members/$user1Id")
        delay(8000)

        // assert zero members of course
        client.get("/courses/${course.id}/members").body<List<ScopeMember>>().apply {
            assertEquals(emptyList(), map(ScopeMember::userId))
        }
    }

    private suspend fun enrolStudentsToGroups(
        client: HttpClient,
        studyGroup1: StudyGroupResponse,
        studyGroup2: StudyGroupResponse,
        user2Id: UUID,
        user1Id: UUID
    ) {
        client.post("/studygroups/${studyGroup1.id}/members?action=manual") {
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user1Id, roleIds = listOf(3)))
        }.apply { assertEquals(HttpStatusCode.Created, status) }
        client.post("/studygroups/${studyGroup2.id}/members?action=manual") {
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user1Id, roleIds = listOf(3)))
        }.apply { assertEquals(HttpStatusCode.Created, status) }
        client.post("/studygroups/${studyGroup1.id}/members?action=manual") {
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user2Id, roleIds = listOf(3)))
        }.apply { assertEquals(HttpStatusCode.Created, status) }
        delay(10000)
    }

    private suspend fun attachGroupsToCourse(
        client: HttpClient,
        course: CourseResponse,
        studyGroup1: StudyGroupResponse,
        studyGroup2: StudyGroupResponse
    ) {
        client.put("/courses/${course.id}/studygroups/${studyGroup1.id}")
        client.put("/courses/${course.id}/studygroups/${studyGroup2.id}")
        delay(10000)
    }

    @BeforeEach
    fun initData(): Unit = runBlocking {
        val studyGroup1: StudyGroupResponse = client.post("/studygroups") {
            contentType(ContentType.Application.Json)
            setBody(CreateStudyGroupRequest("Test group 1", AcademicYear(2022, 2023)))
        }.body<StudyGroupResponse>().apply {
            assertEquals(name, "Test group 1")
        }

        val studyGroup2: StudyGroupResponse = client.post("/studygroups") {
            contentType(ContentType.Application.Json)
            setBody(CreateStudyGroupRequest("Test group 2", AcademicYear(2022, 2025)))
        }.body()

        val course: CourseResponse = client.post("/courses") {
            contentType(ContentType.Application.Json)
            setBody(CreateCourseRequest("Test course 1"))
        }.run {
            val body = body<CourseResponse>()
            assertEquals(body.name, "Test course 1")
            body
        }
        startData = StartData(studyGroup1, studyGroup2, course)
    }

    @AfterEach
    fun clearData(): Unit = runBlocking {
        val (studyGroup1: StudyGroupResponse, studyGroup2: StudyGroupResponse, course: CourseResponse) = startData
        // delete data
        assertEquals(
            HttpStatusCode.NoContent,
            client.delete("/studygroups/${studyGroup1.id}").status
        )
        assertEquals(
            HttpStatusCode.NoContent,
            client.delete("/studygroups/${studyGroup2.id}").status
        )
        assertEquals(
            HttpStatusCode.OK,
            client.put("/courses/${course.id}/archived").status
        )
        assertEquals(
            HttpStatusCode.NoContent,
            client.delete("/courses/${course.id}").status
        )
    }
}

data class StartData(
    val studyGroup1: StudyGroupResponse,
    val studyGroup2: StudyGroupResponse,
    val course: CourseResponse
)