package com.studiversity.client.course

import com.github.michaelbull.result.*
import com.studiversity.KtorTest
import com.studiversity.api.course.CoursesApi
import com.studiversity.api.courseelement.CourseElementApi
import com.studiversity.api.coursework.CourseWorkApi
import com.studiversity.api.membership.MembershipsApi
import com.studiversity.api.submission.SubmissionsApi
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.*
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.work.submission.model.SubmissionResponse
import com.studiversity.feature.course.work.submission.model.SubmissionState
import com.studiversity.feature.role.Role
import com.studiversity.util.toUUID
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import java.io.File
import java.util.*


class SubmissionsTest : KtorTest() {

    private val student1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val student2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()
    private val teacher1Id = "4c73fa98-2146-4688-ad05-22887c8d921d".toUUID()

    private val linkUrl =
        "https://developers.google.com/classroom/reference/rest/v1/courses.courseWork.studentSubmissions#StudentSubmission"

    private lateinit var course: CourseResponse
    private lateinit var courseWork: CourseElementResponse

    private val studentClient by lazy { createAuthenticatedClient("slavik@gmail.com", "GHBO043g54gh") }
    private val teacherClient by lazy { createAuthenticatedClient("denchic150@gmail.com", "OBDIhi76534g33") }

    private val submissionsApiOfStudent: SubmissionsApi by inject { parametersOf(studentClient) }
    private val submissionsApiOfTeacher: SubmissionsApi by inject { parametersOf(teacherClient) }
    private val coursesApi: CoursesApi by inject { parametersOf(client) }
    private val courseElementApi: CourseElementApi by inject { parametersOf(client) }
    private val courseWorkApi: CourseWorkApi by inject { parametersOf(client) }
    private val membershipsApi: MembershipsApi by inject { parametersOf(client) }


    override fun setup(): Unit = runBlocking {
        course = coursesApi.create(CreateCourseRequest("Test course for submissions")).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }.unwrap()
    }

    override fun cleanup(): Unit = runBlocking {
        coursesApi.setArchive(course.id)
        coursesApi.delete(course.id)
    }

    @BeforeEach
    fun init(): Unit = runBlocking {
        courseWork = courseWorkApi.create(
            course.id,
            CreateCourseWorkRequest(
                name = "Test Assignment",
                description = "some desc",
                topicId = null,
                dueDate = null,
                dueTime = null,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).unwrap()
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
        // delete course element
        courseElementApi.delete(course.id, courseWork.id).apply { assertNotNull(get()) { unwrapError().toString() } }
        // unroll users
        unrollUser(student1Id)
        unrollUser(student2Id)
        unrollUser(teacher1Id)
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
        membershipsApi.joinToScopeManually(userId, course.id, listOf(roleId)).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }
    }

    private suspend fun unrollUser(userId: UUID) {
        membershipsApi.leaveFromScope(userId, course.id, "manual")
            .onSuccess { println("Success unroll user: $userId") }
            .onFailure { println("Failed unroll user: $userId. Status: ${it.code}. Body: ${it.error}") }
    }

    @Test
    fun testAddSubmissions(): Unit = runBlocking {
        enrolStudentsToCourse()
        submissionsApiOfStudent.getAllByCourseWorkId(course.id, courseWork.id).onSuccess { response ->
            assertEquals(2, response.size)
            assertAllStatesIsNew(response)
        }
    }

    @Test
    fun testUpdateStatusToCreatedAfterGettingSubmissionByStudent(): Unit = runBlocking {
        enrolStudentsToCourse()
        val submissions = submissionsApiOfTeacher.getAllByCourseWorkId(course.id, courseWork.id)
            .onFailure { throw AssertionError("getAllByCourseWorkId should be getting. Status: ${it.code}. Message: ${it.error}") }
            .unwrap()
            .also { response ->
                assertEquals(2, response.size)
                assertAllStatesIsNew(response)
            }
        val ownSubmission = submissions.first { it.authorId == student1Id }

        // get submission by another user (maybe teacher)
        submissionsApiOfTeacher.getById(course.id, courseWork.id, ownSubmission.id).apply {
            assertNotNull(get()) { unwrapError().toString() }
            assertEquals(SubmissionState.NEW, unwrap().state)
        }
        // twice get submission by another user (maybe teacher)
        submissionsApiOfTeacher.getById(course.id, courseWork.id, ownSubmission.id).apply {
            assertNotNull(get()) { unwrapError().toString() }
            assertEquals(SubmissionState.NEW, unwrap().state)
        }

        // get submission by owner student
        submissionsApiOfStudent.getById(course.id, courseWork.id, ownSubmission.id).apply {
            assertNotNull(get()) { unwrapError().toString() }
            assertAllStatesInCreated(unwrap())
        }

        // get submission by another user again
        submissionsApiOfTeacher.getById(course.id, courseWork.id, ownSubmission.id).apply {
            assertNotNull(get()) { unwrapError().toString() }
            assertAllStatesInCreated(unwrap())
        }
    }

    @Test
    fun testGetSubmissionsAfterAddNewStudentToCourse(): Unit = runBlocking {
        enrolStudent(student2Id)
        submissionsApiOfTeacher.getAllByCourseWorkId(course.id, courseWork.id)
            .unwrap().also { response ->
                assertEquals(1, response.size)
            }
        enrolStudent(student1Id)
        val submissions = submissionsApiOfTeacher.getAllByCourseWorkId(course.id, courseWork.id)
            .unwrap().also { response ->
                assertEquals(2, response.size)
                assertAllStatesIsNew(response)
            }
        val ownSubmission = submissions.first { it.authorId == student1Id }
        // get submission by owner student
        submissionsApiOfStudent.getById(course.id, courseWork.id, ownSubmission.id)
            .apply {
                assertNotNull(get()) { unwrapError().toString() }
                assertAllStatesInCreated(unwrap())
            }
    }

    @Test
    fun testOnStudentFirstGetSubmissionByStudentId(): Unit = runBlocking {
        enrolStudent(student1Id)
        // get submission by student
        submissionsApiOfStudent.getByStudent(course.id, courseWork.id, student1Id).unwrap().also { response ->
            assertEquals(SubmissionState.CREATED, response.state)
        }
    }

    @Test
    fun testOnTeacherFirstGetSubmissionByStudentId(): Unit = runBlocking {
        enrolStudent(student1Id)
        // get submission by another user (maybe teacher)
        val submission = submissionsApiOfTeacher.getByStudent(course.id, courseWork.id, student1Id)
            .unwrap().also { response ->
                assertEquals(SubmissionState.NEW, response.state)
            }
        submissionsApiOfStudent.getByStudent(course.id, courseWork.id, student1Id).unwrap().also { response ->
            assertEquals(SubmissionState.CREATED, response.state)
            assertEquals(submission.id, response.id)
        }
    }

    @Test
    fun testSubmitSubmission(): Unit = runBlocking {
        enrolStudent(student1Id)
        enrolTeacher(teacher1Id)
        val submission = submissionsApiOfStudent.getByStudent(course.id, courseWork.id, student1Id).unwrap()
        submissionsApiOfStudent.uploadFileToSubmission(
            course.id,
            courseWork.id,
            submission.id,
            File("data.txt").apply { writeText("Hello, Reader!") })

        submissionsApiOfTeacher.submitSubmission(course.id, courseWork.id, submission.id).apply {
            assertEquals(HttpStatusCode.Forbidden.value, unwrapError().code)
        }
        submissionsApiOfStudent.submitSubmission(course.id, courseWork.id, submission.id).apply {
            assertNotNull(get()) { unwrapError().toString() }
            assertEquals(SubmissionState.SUBMITTED, unwrap().state)
        }
    }

    @Test
    fun testGradeSubmission(): Unit = runBlocking {
        enrolStudent(student1Id)
        enrolTeacher(teacher1Id)
        val submission = submissionsApiOfStudent.getByStudent(course.id, courseWork.id, student1Id).unwrap()

        submissionsApiOfStudent.addLinkToSubmission(
            course.id,
            courseWork.id,
            submission.id,
            CreateLinkRequest("https://developers.google.com/classroom/reference/rest/v1/courses.courseWork.studentSubmissions#StudentSubmission")
        ).apply { assertNotNull(get()) { unwrapError().error.toString() } }

        submissionsApiOfStudent.gradeSubmission(course.id, courseWork.id, submission.id, 5).apply {
            assertEquals(HttpStatusCode.Forbidden.value, unwrapError().code)
        }

        submissionsApiOfTeacher.gradeSubmission(course.id, courseWork.id, submission.id, 6).apply {
            assertEquals(HttpStatusCode.BadRequest.value, unwrapError().code)
        }

        submissionsApiOfTeacher.gradeSubmission(course.id, courseWork.id, submission.id, 5).apply {
            val gradedSubmission = get()
            assertNotNull(gradedSubmission) { getError().toString() }
            gradedSubmission?.apply {
                assertEquals(5, grade)
                assertEquals(teacher1Id, gradedBy)
            }
        }
    }

    @Test
    fun testAddRemoveAttachment(): Unit = runBlocking {
        enrolStudent(student1Id)
        val submission = submissionsApiOfStudent.getByStudent(course.id, courseWork.id, student1Id).unwrap()
        val file: File = File("data.txt").apply {
            writeText("Hello, Reader!")
        }
        submissionsApiOfStudent.uploadFileToSubmission(course.id, courseWork.id, submission.id, file).apply {
            assertNotNull(get(), getError().toString())
        }

        submissionsApiOfStudent.getAttachments(course.id, courseWork.id, submission.id).unwrap().apply {
            assertEquals(1, size)
        }

        submissionsApiOfStudent.addLinkToSubmission(
            course.id,
            courseWork.id,
            submission.id,
            CreateLinkRequest(linkUrl)
        ).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
            assertEquals(
                linkUrl,
                unwrap().link.url
            )
        }

        val attachments = submissionsApiOfStudent.getAttachments(course.id, courseWork.id, submission.id)
            .unwrap().apply {
                assertEquals(2, size)
                kotlin.test.assertTrue(any { it is FileAttachmentHeader && it.fileItem.name == "data.txt" })
                kotlin.test.assertTrue(any { it is LinkAttachmentHeader && it.link.url == linkUrl })
            }

        submissionsApiOfStudent.deleteAttachmentOfSubmission(course.id, courseWork.id, submission.id, attachments[0].id)
            .apply { assertNotNull(get()) { unwrapError().toString() } }

        submissionsApiOfStudent.getAttachments(course.id, courseWork.id, submission.id).unwrap().apply {
            assertEquals(1, size)
        }
        submissionsApiOfStudent.deleteAttachmentOfSubmission(course.id, courseWork.id, submission.id, attachments[1].id)
            .apply { assertNotNull(get()) { unwrapError().toString() } }

        submissionsApiOfStudent.getAttachments(course.id, courseWork.id, submission.id).unwrap().apply {
            assertTrue { isEmpty() }
        }
    }

    private fun assertAllStatesInCreated(response: SubmissionResponse) {
        assertEquals(SubmissionState.CREATED, response.state)
    }

    private fun assertAllStatesIsNew(response: List<SubmissionResponse>) {
        assertTrue(response.all { it.state == SubmissionState.NEW })
    }
}