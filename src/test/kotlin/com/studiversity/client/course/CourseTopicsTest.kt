package com.studiversity.client.course

import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import com.studiversity.KtorClientTest
import com.studiversity.api.course.CoursesApi
import com.studiversity.api.course.topic.CourseTopicApi
import com.studiversity.api.course.topic.RelatedTopicElements
import com.studiversity.api.course.topic.model.CreateTopicRequest
import com.studiversity.api.course.topic.model.UpdateTopicRequest
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.util.OptionalProperty
import com.studiversity.util.toUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject

class CourseTopicsTest : KtorClientTest() {

    private val student1Id = "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
    private val teacher1Id = "4c73fa98-2146-4688-ad05-22887c8d921d".toUUID()

    private lateinit var course: CourseResponse

    private val studentClient by lazy { createAuthenticatedClient("slavik@gmail.com", "GHBO043g54gh") }
    private val teacherClient by lazy { createAuthenticatedClient("denchic150@gmail.com", "OBDIhi76534g33") }

    private val coursesApi: CoursesApi by inject { parametersOf(client) }
    private val courseTopicApi: CourseTopicApi by inject { parametersOf(client) }

    override fun setup(): Unit = runBlocking {
        course = coursesApi.create(CreateCourseRequest("Test course for submissions")).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }.unwrap()
    }

    @Test
    fun testAddUpdateRemoveTopic(): Unit = runBlocking {
        val topic = courseTopicApi.create(course.id, CreateTopicRequest("My Topic")).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }.unwrap()

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

        courseTopicApi.delete(course.id, topic.id, RelatedTopicElements.DELETE).apply {
            assertNotNull(get()) { unwrapError().error.toString() }
        }
    }

    override fun cleanup(): Unit = runBlocking {
        coursesApi.setArchive(course.id)
        coursesApi.delete(course.id)
    }
}