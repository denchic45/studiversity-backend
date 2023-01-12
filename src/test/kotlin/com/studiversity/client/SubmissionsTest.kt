package com.studiversity.client

import com.studiversity.KtorTest
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CreateCourseWorkRequest
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.submission.model.SubmissionResponse
import com.studiversity.feature.membership.model.ManualJoinMemberRequest
import com.studiversity.util.toUUID
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SubmissionsTest : KtorTest() {

    private lateinit var course: CourseResponse
    private lateinit var courseWork: CourseElementResponse

    private val user1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val user2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()

    @BeforeEach
    fun init(): Unit = runBlocking {
        course = client.post("/courses") {
            contentType(ContentType.Application.Json)
            setBody(CreateCourseRequest("Test course for submissions"))
        }.apply { assertEquals(HttpStatusCode.OK, status) }
            .body()

        courseWork = client.post("/courses/${course.id}/elements") {
            contentType(ContentType.Application.Json)
            setBody(CreateCourseWorkRequest("Test Assignment", "some desc", null))
        }.apply { assertEquals(HttpStatusCode.OK, status) }
            .body()

        enrolStudentsToCourse()
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
        // delete course
        client.delete("/courses/${course.id}").apply {
            assertEquals(HttpStatusCode.NoContent, status)
        }
    }

    private suspend fun enrolStudentsToCourse() {
        client.post("/scopes/${course.id}/members?action=manual") {
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user1Id, roleIds = listOf(3)))
        }.apply { assertEquals(HttpStatusCode.Created, status) }
        client.post("/scopes/${course.id}/members?action=manual") {
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(user2Id, roleIds = listOf(3)))
        }.apply { assertEquals(HttpStatusCode.Created, status) }
        delay(8000)
    }

    @Test
    fun testAddSubmissions(): Unit = runBlocking {
        // get submissions
        client.get("/courses/${course.id}/elements/${courseWork.id}/submissions")
            .apply { assertEquals(HttpStatusCode.OK, status) }
            .body<List<SubmissionResponse>>().let { response ->
                assertEquals(2, response.size)
            }
    }
}