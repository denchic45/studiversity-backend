package com.studiversity.client.course

import com.github.michaelbull.result.*
import com.studiversity.KtorTest
import com.studiversity.api.course.CoursesApi
import com.studiversity.api.courseelement.CourseElementApi
import com.studiversity.api.coursework.CourseWorkApi
import com.studiversity.api.membership.MembershipsApi
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.*
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.role.Role
import com.studiversity.util.toUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.inject
import java.io.File
import java.util.*
import kotlin.test.assertTrue

class CourseElementsTest : KtorTest() {

    private val student1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val student2Id = "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID()
    private val teacher1Id = "4c73fa98-2146-4688-ad05-22887c8d921d".toUUID()

    private val linkUrl =
        "https://developers.google.com/classroom/reference/rest/v1/courses.courseWork.studentSubmissions#StudentSubmission"

    private val file: File = File("data.txt").apply {
        writeText("Hello, Reader!")
    }

    private val teacherClient by lazy { createAuthenticatedClient("denchic150@gmail.com", "OBDIhi76534g33") }

    private val coursesApi: CoursesApi by inject { org.koin.core.parameter.parametersOf(client) }
    private val courseElementApi: CourseElementApi by inject { org.koin.core.parameter.parametersOf(client) }
    private val courseWorkApi: CourseWorkApi by inject { org.koin.core.parameter.parametersOf(teacherClient) }
    private val membershipsApi: MembershipsApi by inject { org.koin.core.parameter.parametersOf(client) }

    private lateinit var course: CourseResponse
    private lateinit var courseWork: CourseElementResponse

    override fun setup(): Unit = runBlocking {
        course = coursesApi.create(CreateCourseRequest("Test course for submissions")).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }.unwrap()
        enrolTeacher(teacher1Id)
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
        ).apply {
            assertNotNull(get()) { "Message is here: " + unwrapError().error.toString() }
        }.unwrap()
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
        // delete course element
        courseElementApi.delete(course.id, courseWork.id)
            .apply { assertNotNull(get()) { unwrapError().toString() } }
        // unroll users
        unrollUser(student1Id)
        unrollUser(student2Id)
        unrollUser(teacher1Id)
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
    fun testAddRemoveAttachment(): Unit = runBlocking {

        courseWorkApi.uploadFileToSubmission(course.id, courseWork.id, file).apply {
            assertNotNull(get(), getError().toString())
            assertEquals("data.txt", unwrap().fileItem.name)
        }

        courseWorkApi.getAttachments(course.id, courseWork.id).unwrap().apply {
            assertEquals(1, size)
        }

        courseWorkApi.addLinkToSubmission(
            course.id,
            courseWork.id,
            CreateLinkRequest(linkUrl)
        ).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
            assertEquals(
                linkUrl,
                unwrap().link.url
            )
        }

        val attachments = courseWorkApi.getAttachments(course.id, courseWork.id).unwrap().apply {
            assertEquals(2, size)
            assertTrue(any { it is FileAttachmentHeader && it.fileItem.name == "data.txt" })
            assertTrue(any { it is LinkAttachmentHeader && it.link.url == linkUrl })
        }

        deleteAttachment(attachments[0].id)

        courseWorkApi.getAttachments(course.id, courseWork.id).unwrap().apply {
            assertEquals(1, size)
        }
    }

    @Test
    fun testDownloadAttachments(): Unit = runBlocking {
        val fileAttachmentId = courseWorkApi.uploadFileToSubmission(course.id, courseWork.id, file).apply {
            assertNotNull(get(), getError().toString())
            assertEquals("data.txt", unwrap().fileItem.name)
        }.unwrap().id

        courseWorkApi.getAttachment(course.id, courseWork.id, fileAttachmentId).apply {
            assertEquals("data.txt", (unwrap() as FileAttachment).name)
        }

        val linkAttachmentId = courseWorkApi.addLinkToSubmission(
            course.id,
            courseWork.id,
            CreateLinkRequest(linkUrl)
        ).unwrap().id

        courseWorkApi.getAttachment(course.id, courseWork.id, linkAttachmentId).apply {
            assertEquals(linkUrl, (unwrap() as Link).url)
        }
    }

    private suspend fun deleteAttachment(attachmentId: UUID) {
        courseWorkApi.deleteAttachmentOfSubmission(course.id, courseWork.id, attachmentId).apply {
            assertNotNull(get()) { unwrapError().toString() }
        }
    }

}