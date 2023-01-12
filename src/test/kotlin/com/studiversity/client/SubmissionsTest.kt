package com.studiversity.client

import com.studiversity.KtorTest
import com.studiversity.feature.auth.model.LoginRequest
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CourseWork
import com.studiversity.feature.course.element.model.CreateCourseWorkRequest
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.submission.model.SubmissionResponse
import com.studiversity.feature.course.submission.model.SubmissionState
import com.studiversity.feature.membership.model.ManualJoinMemberRequest
import com.studiversity.supabase.model.SignupResponse
import com.studiversity.util.toUUID
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubmissionsTest : KtorTest() {

    private lateinit var course: CourseResponse
    private lateinit var courseWork: CourseElementResponse

    private val user1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val user2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()

    private val studentClient = testApp.createClient {
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
                        setBody(LoginRequest("slavik@gmail.com", "GHBO043g54gh"))
                    }.body<SignupResponse>().accessToken, "")
                }
            }
        }
    }

    @BeforeEach
    fun init(): Unit = runBlocking {
        course = client.post("/courses") {
            contentType(ContentType.Application.Json)
            setBody(CreateCourseRequest("Test course for submissions"))
        }.apply { assertEquals(HttpStatusCode.OK, status) }.body()
        courseWork = client.post("/courses/${course.id}/elements") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateCourseWorkRequest(
                    "Test Assignment",
                    "some desc",
                    null,
                    CourseWork(null, null, CourseWorkType.Assignment)
                )
            )
        }.apply { assertEquals(HttpStatusCode.OK, status) }.body()
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
        // delete course
        client.put("/courses/${course.id}/archived")
        client.delete("/courses/${course.id}").apply {
            assertEquals(HttpStatusCode.NoContent, status)
        }
    }

    private suspend fun enrolStudentsToCourse() {
        enrolStudent(user1Id)
        enrolStudent(user2Id)
    }

    private suspend fun enrolStudent(userId: UUID) {
        client.post("/scopes/${course.id}/members?action=manual") {
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(userId, roleIds = listOf(3)))
        }.apply { assertEquals(HttpStatusCode.Created, status) }
        delay(8000)
    }

    @Test
    fun testAddSubmissions(): Unit = runBlocking {
        enrolStudentsToCourse()
        getAllSubmissionsByWork().let { response ->
            assertEquals(2, response.size)
            assertAllStatesIsNew(response)
        }
    }

    private suspend fun getAllSubmissionsByWork(): List<SubmissionResponse> {
        return client.get("/courses/${course.id}/elements/${courseWork.id}/submissions")
            .apply { assertEquals(HttpStatusCode.OK, status) }.body()
    }

    @Test
    fun testUpdateStatusToCreatedAfterGettingSubmissionByStudent(): Unit = runBlocking {
        enrolStudentsToCourse()
        val submissions = getAllSubmissionsByWork().also { response ->
            assertEquals(2, response.size)
            assertAllStatesIsNew(response)
        }
        val ownSubmission = submissions.first { it.authorId == user1Id }

        // get submission by another user (maybe teacher)
        client.get("/courses/${course.id}/elements/${courseWork.id}/submissions/${ownSubmission.id}")
            .apply { assertEquals(HttpStatusCode.OK, status) }
            .body<SubmissionResponse>().also { response ->
                assertEquals(SubmissionState.NEW, response.state)
            }

        // get submission by owner student
        studentClient.get("/courses/${course.id}/elements/${courseWork.id}/submissions/${ownSubmission.id}")
            .apply { assertEquals(HttpStatusCode.OK, status) }
            .body<SubmissionResponse>().also { response ->
                assertAllStatesInCreated(response)
            }

        // get submission by another user again
        client.get("/courses/${course.id}/elements/${courseWork.id}/submissions/${ownSubmission.id}")
            .apply { assertEquals(HttpStatusCode.OK, status) }
            .body<SubmissionResponse>().also { response ->
                assertAllStatesInCreated(response)
            }
    }

    @Test
    fun testGetSubmissionsAfterAddNewStudentToCourse(): Unit = runBlocking {
        enrolStudent(user2Id)
        getAllSubmissionsByWork().also { response ->
            assertEquals(1, response.size)
        }

        enrolStudent(user1Id)
        val submissions = getAllSubmissionsByWork().also { response ->
            assertEquals(2, response.size)
            assertAllStatesIsNew(response)
        }
        val ownSubmission = submissions.first { it.authorId == user1Id }
        // get submission by owner student
        studentClient.get("/courses/${course.id}/elements/${courseWork.id}/submissions/${ownSubmission.id}")
            .apply { assertEquals(HttpStatusCode.OK, status) }
            .body<SubmissionResponse>().also { response ->
                assertAllStatesInCreated(response)
            }
    }

    private fun assertAllStatesInCreated(response: SubmissionResponse) {
        assertEquals(SubmissionState.CREATED, response.state)
    }

    private fun assertAllStatesIsNew(response: List<SubmissionResponse>) {
        assertTrue(response.all { it.state == SubmissionState.NEW })
    }
}