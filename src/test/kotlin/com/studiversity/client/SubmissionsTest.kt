package com.studiversity.client

import com.studiversity.KtorTest
import com.studiversity.feature.auth.model.LoginRequest
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.*
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.submission.model.AssignmentSubmission
import com.studiversity.feature.course.submission.model.SubmissionResponse
import com.studiversity.feature.course.submission.model.SubmissionState
import com.studiversity.feature.course.submission.model.UpdateSubmissionRequest
import com.studiversity.feature.membership.model.ManualJoinMemberRequest
import com.studiversity.feature.role.Role
import com.studiversity.supabase.model.SignupResponse
import com.studiversity.util.OptionalProperty
import com.studiversity.util.requirePresent
import com.studiversity.util.toUUID
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubmissionsTest : KtorTest() {

    private lateinit var course: CourseResponse
    private lateinit var courseWork: CourseElementResponse

    private val student1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val student2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()
    private val teacher1Id = "4c73fa98-2146-4688-ad05-22887c8d921d".toUUID()

    private val studentClient = testApp.createClient {
        installContentNegotiation()
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

    private val teacherClient = testApp.createClient {
        installContentNegotiation()
        install(Auth) {
            bearer {
                refreshTokens {
                    BearerTokens(accessToken = client.post("/auth/token?grant_type=password") {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequest("denchic150@gmail.com", "OBDIhi76534g33"))
                    }.apply { println("bearer: ${bodyAsText()}") }.body<SignupResponse>().accessToken, "")
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
                    CourseWork(null, null, CourseWorkType.Assignment, 5)
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
        enrolStudent(student1Id)
        enrolStudent(student2Id)
    }

    private suspend fun enrolStudent(userId: UUID) {
        enrolUser(userId, Role.Student.id)
    }

    private suspend fun enrolTeacher(userId: UUID) {
        enrolUser(userId, Role.Teacher.id)
    }

    private suspend fun enrolUser(userId: UUID, roleId: Long) {
        client.post("/scopes/${course.id}/members?action=manual") {
            contentType(ContentType.Application.Json)
            setBody(ManualJoinMemberRequest(userId, roleIds = listOf(roleId)))
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
        val ownSubmission = submissions.first { it.authorId == student1Id }

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
        enrolStudent(student2Id)
        getAllSubmissionsByWork().also { response ->
            assertEquals(1, response.size)
        }

        enrolStudent(student1Id)
        val submissions = getAllSubmissionsByWork().also { response ->
            assertEquals(2, response.size)
            assertAllStatesIsNew(response)
        }
        val ownSubmission = submissions.first { it.authorId == student1Id }
        // get submission by owner student
        studentClient.get("/courses/${course.id}/elements/${courseWork.id}/submissions/${ownSubmission.id}")
            .apply { assertEquals(HttpStatusCode.OK, status) }
            .body<SubmissionResponse>().also { response ->
                assertAllStatesInCreated(response)
            }
    }

    @Test
    fun testOnStudentFirstGetSubmissionByStudentId(): Unit = runBlocking {
        enrolStudent(student1Id)
        // get submission by student
        studentClient.getSubmissionByStudent(student1Id).also { response ->
            assertEquals(SubmissionState.CREATED, response.state)
        }
        client.getSubmissionByStudent(student1Id).also { response ->
            assertEquals(SubmissionState.CREATED, response.state)
        }
    }

    @Test
    fun testOnTeacherFirstGetSubmissionByStudentId(): Unit = runBlocking {
        enrolStudent(student1Id)
        // get submission by another user (maybe teacher)
        val submission = client.getSubmissionByStudent(student1Id).also { response ->
            assertEquals(SubmissionState.NEW, response.state)
        }
        studentClient.getSubmissionByStudent(student1Id).also { response ->
            assertEquals(SubmissionState.CREATED, response.state)
            assertEquals(submission.id, response.id)
        }
        client.getSubmissionByStudent(student1Id).also { response ->
            assertEquals(SubmissionState.CREATED, response.state)
            assertEquals(submission.id, response.id)
        }
    }

    @Test
    fun testSubmitSubmission(): Unit = runBlocking {
        enrolStudent(student1Id)
        val submission = studentClient.getSubmissionByStudent(student1Id)
        val request = UpdateSubmissionRequest(
            OptionalProperty.Present(
                AssignmentSubmission(
                    listOf(
                        Attachment(
                            UUID.randomUUID(),
                            AttachmentType.Link,
                            "https://developers.google.com/classroom/reference/rest/v1/courses.courseWork.studentSubmissions#StudentSubmission"
                        )
                    )
                )
            )
        )
        client.submitSubmission(submission.id, request).apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
        studentClient.submitSubmission(submission.id, request).apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<SubmissionResponse>()
            assertEquals(SubmissionState.SUBMITTED, response.state)
            assertEquals(request.content.requirePresent(), response.content)
        }
    }

    @Test
    fun testGradeSubmission(): Unit = runBlocking {
        enrolStudent(student1Id)
        enrolTeacher(teacher1Id)
        val submission = studentClient.getSubmissionByStudent(student1Id)
        val request = UpdateSubmissionRequest(
            OptionalProperty.Present(
                AssignmentSubmission(
                    listOf(
                        Attachment(
                            UUID.randomUUID(),
                            AttachmentType.Link,
                            "https://developers.google.com/classroom/reference/rest/v1/courses.courseWork.studentSubmissions#StudentSubmission"
                        )
                    )
                )
            )
        )
        val response = studentClient.submitSubmission(submission.id, request).apply {
            assertEquals(HttpStatusCode.OK, status)
        }.body<SubmissionResponse>()

        studentClient.gradeSubmission(response.id, 6).apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }

        teacherClient.gradeSubmission(response.id, 6).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }

        teacherClient.gradeSubmission(response.id, 4).apply {
            assertEquals(HttpStatusCode.OK, status)
            body<SubmissionResponse>().apply {
                assertEquals(4, grade)
                assertEquals(teacher1Id, gradedBy)
            }
        }
    }

    private suspend fun HttpClient.getSubmissionByStudent(userId: UUID): SubmissionResponse {
        return get("/courses/${course.id}/elements/${courseWork.id}/submissionsByStudentId/${userId}")
            .apply { assertEquals(HttpStatusCode.OK, status) }.body()
    }

    private suspend fun HttpClient.submitSubmission(
        submissionId: UUID,
        updateSubmissionRequest: UpdateSubmissionRequest
    ): HttpResponse {
        return post("/courses/${course.id}/elements/${courseWork.id}/submissions/${submissionId}/submit") {
            contentType(ContentType.Application.Json)
            setBody(updateSubmissionRequest)
        }
    }

    private suspend fun HttpClient.gradeSubmission(
        submissionId: UUID,
        grade: Short,
    ): HttpResponse {
        return patch("/courses/${course.id}/elements/${courseWork.id}/submissions/${submissionId}") {
            contentType(ContentType.Application.Json)
            setBody(UpdateSubmissionRequest(grade = OptionalProperty.Present(grade)))
        }
    }

    private fun assertAllStatesInCreated(response: SubmissionResponse) {
        assertEquals(SubmissionState.CREATED, response.state)
    }

    private fun assertAllStatesIsNew(response: List<SubmissionResponse>) {
        assertTrue(response.all { it.state == SubmissionState.NEW })
    }
}