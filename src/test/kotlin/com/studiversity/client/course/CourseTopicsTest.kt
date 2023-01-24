package com.studiversity.client.course

import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import com.studiversity.KtorClientTest
import com.studiversity.api.course.CoursesApi
import com.studiversity.api.course.element.CourseElementApi
import com.studiversity.api.course.topic.CourseTopicApi
import com.studiversity.api.course.topic.RelatedTopicElements
import com.studiversity.api.course.topic.model.CreateTopicRequest
import com.studiversity.api.course.topic.model.UpdateTopicRequest
import com.studiversity.api.course.work.CourseWorkApi
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.UpdateCourseElementRequest
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.work.model.CreateCourseWorkRequest
import com.studiversity.util.OptionalProperty
import com.studiversity.util.toUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import java.util.*

class CourseTopicsTest : KtorClientTest() {

    private val student1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val teacher1Id = "4c73fa98-2146-4688-ad05-22887c8d921d".toUUID()

    private lateinit var course: CourseResponse

    private val studentClient by lazy { createAuthenticatedClient("slavik@gmail.com", "GHBO043g54gh") }
    private val teacherClient by lazy { createAuthenticatedClient("denchic150@gmail.com", "OBDIhi76534g33") }

    private val coursesApi: CoursesApi by inject { parametersOf(client) }
    private val courseTopicApi: CourseTopicApi by inject { parametersOf(client) }
    private val courseWorkApi: CourseWorkApi by inject { parametersOf(teacherClient) }
    private val courseElementApi: CourseElementApi by inject { parametersOf(teacherClient) }

    override fun setup(): Unit = runBlocking {
        course = coursesApi.create(CreateCourseRequest("Test course for submissions")).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }.unwrap()
    }

    @Test
    fun testAddUpdateRemoveTopic(): Unit = runBlocking {
        val topic = createTopic()

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
        val topic = createTopic()

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
        ).apply { assertEquals(1, get()?.order) { unwrapError().error.toString() } }.unwrap()
        val elemWithoutTopic2 = courseWorkApi.create(
            course.id, CreateCourseWorkRequest(
                name = "Some Assignment 2",
                description = null,
                topicId = null,
                workType = CourseWorkType.ASSIGNMENT,
                maxGrade = 5
            )
        ).apply { assertEquals(2, get()?.order) { unwrapError().error.toString() } }.unwrap()

        val topic = createTopic()

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
            ).apply { assertEquals(get()?.topicId, topic.id) { unwrapError().error.toString() } }
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

    private suspend fun removeTopic(topicId: UUID, relatedTopicElements: RelatedTopicElements) {
        courseTopicApi.delete(course.id, topicId, relatedTopicElements).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }
    }

    private suspend fun createTopic() = courseTopicApi.create(course.id, CreateTopicRequest("My Topic")).apply {
        assertNotNull(get()) { unwrapError().error.toString() }
    }.unwrap()

    override fun cleanup(): Unit = runBlocking {
        coursesApi.setArchive(course.id)
        coursesApi.delete(course.id)
    }
}