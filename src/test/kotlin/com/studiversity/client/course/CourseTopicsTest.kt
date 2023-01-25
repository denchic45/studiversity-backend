package com.studiversity.client.course

import com.github.michaelbull.result.*
import com.studiversity.KtorClientTest
import com.studiversity.api.course.CoursesApi
import com.studiversity.api.course.element.CourseElementApi
import com.studiversity.api.course.topic.CourseTopicApi
import com.studiversity.api.course.topic.RelatedTopicElements
import com.studiversity.api.course.topic.model.CreateTopicRequest
import com.studiversity.api.course.topic.model.UpdateTopicRequest
import com.studiversity.api.course.work.CourseWorkApi
import com.studiversity.api.membership.MembershipsApi
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.UpdateCourseElementRequest
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.work.model.CreateCourseWorkRequest
import com.studiversity.feature.role.Role
import com.studiversity.util.OptionalProperty
import com.studiversity.util.toUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import java.util.*

class CourseTopicsTest : KtorClientTest() {

    private val student1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val teacher1Id = "02f00b3e-3a78-4431-87d4-34128ebbb04c".toUUID()

    private lateinit var course: CourseResponse

    private val studentClient by lazy { createAuthenticatedClient("slavik@gmail.com", "GHBO043g54gh") }
    private val teacherClient by lazy { createAuthenticatedClient("stefan@gmail.com", "FSg54g45dg") }

    private val coursesApi: CoursesApi by inject { parametersOf(client) }
    private val courseTopicApi: CourseTopicApi by inject { parametersOf(teacherClient) }
    private val courseWorkApi: CourseWorkApi by inject { parametersOf(teacherClient) }
    private val courseElementApi: CourseElementApi by inject { parametersOf(teacherClient) }
    private val membershipsApi: MembershipsApi by inject { parametersOf(client) }

    override fun setup(): Unit = runBlocking {

    }

    override fun cleanup(): Unit = runBlocking {

    }

    @BeforeEach
    fun init(): Unit = runBlocking {
        course = coursesApi.create(CreateCourseRequest("Test course for submissions")).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }.unwrap()
        enrolTeacher(teacher1Id)
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
//        unrollUser(teacher1Id)
        coursesApi.setArchive(course.id)
        coursesApi.delete(course.id)
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
    fun testAddUpdateRemoveTopic(): Unit = runBlocking {
        val topic = courseTopicApi.createTopic(course.id)

        assertEquals("My Topic", topic.name)

        courseTopicApi.getByCourseId(course.id).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
            assertEquals(1, unwrap().size)
            assertEquals(topic, unwrap()[0])
        }

        val updatedTopic = courseTopicApi.update(
            courseId = course.id,
            topicId = topic.id,
            updateTopicRequest = UpdateTopicRequest(OptionalProperty.Present("Updated Topic"))
        ).apply { assertNotNull(get()) { unwrapError().error.toString() } }.unwrap()

        assertEquals("Updated Topic", updatedTopic.name)

        removeTopic(topic.id, RelatedTopicElements.DELETE)
    }

    @Test
    fun testClearRemovedTopicOfCourseElements(): Unit = runBlocking {
        val topic = courseTopicApi.createTopic(course.id)

        val courseWork = courseWorkApi.create(
            course.id, CreateCourseWorkRequest(
                name = "Some Assignment",
                description = null,
                topicId = topic.id,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
            assertEquals(topic.id, unwrap().topicId)
        }.unwrap()

        removeTopic(topic.id, RelatedTopicElements.CLEAR_TOPIC)

        courseWorkApi.getById(course.id, courseWork.id).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
            assertNull(unwrap().topicId) { unwrap().toString() }
        }
    }

    @Test
    fun updatedElementOrdersAfterClearRemovedTopic(): Unit = runBlocking {
        val elemWithoutTopic1 = courseWorkApi.create(
            course.id, CreateCourseWorkRequest(
                name = "Some Assignment 1",
                description = null,
                topicId = null,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).apply { assertEquals(1, get()?.order) }.unwrap()
        val elemWithoutTopic2 = courseWorkApi.create(
            course.id, CreateCourseWorkRequest(
                name = "Some Assignment 2",
                description = null,
                topicId = null,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).apply { assertEquals(2, get()?.order) { unwrapError().error.toString() } }.unwrap()

        val topic = courseTopicApi.createTopic(course.id)

        // creating element immediately with a topic
        val elemWithTopic1 = courseWorkApi.create(
            course.id, CreateCourseWorkRequest(
                name = "Assignment in topic 1",
                description = null,
                topicId = topic.id,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).apply {
            assertEquals(topic.id, get()?.topicId) { unwrapError().error.toString() }
            assertEquals(1, get()?.order) { unwrapError().error.toString() }
        }.unwrap()

        // attach element to topic later
        val elemWithTopic2 = courseWorkApi.create(
            course.id, CreateCourseWorkRequest(
                name = "Assignment in topic 2",
                description = null,
                topicId = null,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).apply {
            assertEquals(null, get()?.topicId) { unwrapError().error.toString() }
        }.unwrap().let { element ->
            courseElementApi.update(
                course.id,
                element.id,
                UpdateCourseElementRequest(topicId = OptionalProperty.Present(topic.id))
            ).apply { assertEquals(topic.id, get()?.topicId) { unwrapError().error.toString() } }
        }.unwrap()

        removeTopic(topic.id, RelatedTopicElements.CLEAR_TOPIC)

        // Check updated order in two elements
        courseWorkApi.getById(course.id, elemWithTopic1.id).apply {
            assertEquals(3, get()?.order)
        }

        courseWorkApi.getById(course.id, elemWithTopic2.id).apply {
            assertEquals(4, get()?.order)
        }
    }

    @Test
    fun testUpdateCourseElementTopic(): Unit = runBlocking {
        val element = courseWorkApi.create(
            course.id, CreateCourseWorkRequest(
                name = "Assignment",
                description = null,
                topicId = null,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).unwrap()
        val topic = courseTopicApi.createTopic(course.id)
        courseElementApi.update(
            course.id, element.id,
            UpdateCourseElementRequest(OptionalProperty.Present(topic.id))
        ).unwrap().apply {
            assertEquals(topic.id, topicId)
            assertEquals(1, order)
        }

        courseElementApi.update(
            course.id, element.id, UpdateCourseElementRequest(OptionalProperty.Present(null))
        ).unwrap().apply {
            assertNull(topicId)
            assertEquals(1, order)
        }
    }

    //TODO finish the test
    @Test
    fun testUpdateOrderOnMoveBetweenTopics(): Unit = runBlocking {
        val topic1 = courseTopicApi.createTopic(course.id,"Topic 1")
        val topic2 = courseTopicApi.createTopic(course.id,"Topic 2")

        val firstElements = List(5) {
            courseWorkApi.create(
                course.id, CreateCourseWorkRequest(
                    name = "Assignment $it in topic 1",
                    description = null,
                    topicId = topic1.id,
                    workType = CourseWorkType.ASSIGNMENT,
                    maxGrade = 5
                )
            ).unwrap()
        }
        val secondElements = List(5) {
            courseWorkApi.create(
                course.id, CreateCourseWorkRequest(
                    name = "Assignment $it in topic 2",
                    description = null,
                    topicId = topic2.id,
                    workType = CourseWorkType.ASSIGNMENT,
                    maxGrade = 5
                )
            ).unwrap()
        }

        val updatedElement = courseElementApi.update(
            course.id, firstElements[2].id, UpdateCourseElementRequest(OptionalProperty.Present(topic2.id))
        ).unwrap().apply {
            assertEquals(topic2.id, this.topicId)
            assertEquals(6, this.order)
        }
    }

    private suspend fun removeTopic(topicId: UUID, relatedTopicElements: RelatedTopicElements) {
        courseTopicApi.delete(course.id, topicId, relatedTopicElements).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }
    }
}

suspend fun CourseTopicApi.createTopic(courseId: UUID, name: String = "My Topic") =
    create(courseId, CreateTopicRequest(name)).apply {
        assertNotNull(get()) { unwrapError().error.toString() }
    }.unwrap()