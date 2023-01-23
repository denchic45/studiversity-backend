package com.studiversity.feature.course.topic

import com.studiversity.api.course.topic.RelatedTopicElements
import com.studiversity.api.course.topic.model.CreateTopicRequest
import com.studiversity.api.course.topic.model.TopicResponse
import com.studiversity.api.course.topic.model.UpdateTopicRequest
import com.studiversity.database.table.CourseElements
import com.studiversity.database.table.CourseTopicDao
import com.studiversity.database.table.CourseTopics
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.*

class CourseTopicRepository {

    fun add(courseId: UUID, createTopicRequest: CreateTopicRequest): TopicResponse {
        return CourseTopicDao.new {
            this.courseId = courseId
            this.name = createTopicRequest.name
            this.order = generateOrderByCourseId(courseId)
        }.toResponse()
    }

    fun update(courseId: UUID, topicId: UUID, updateTopicRequest: UpdateTopicRequest): TopicResponse? {
        return getById(topicId, courseId)?.apply {
            updateTopicRequest.name.ifPresent {
                name = it
            }
        }?.toResponse()
    }

    private fun getById(
        topicId: UUID,
        courseId: UUID
    ) = CourseTopicDao.find(CourseTopics.id eq topicId and (CourseTopics.courseId eq courseId))
        .singleOrNull()

    fun remove(courseId: UUID, topicId: UUID, relatedTopicElements: RelatedTopicElements): Unit? {
        when (relatedTopicElements) {
            RelatedTopicElements.DELETE -> {}
            RelatedTopicElements.CLEAR_TOPIC -> {
                val maxOrderWithoutTopic = getMaxOrderByCourseId(courseId)
                CourseElements.update(where = { CourseElements.topicId eq topicId }) {
                    it[CourseElements.topicId] = null
                    it[order] = order + maxOrderWithoutTopic
                }
            }
        }
        return getById(topicId, courseId)?.delete()
    }

    private fun generateOrderByCourseId(courseId: UUID) = getMaxOrderByCourseId(courseId) + 1

    private fun getMaxOrderByCourseId(courseId: UUID): Int {
        return CourseTopics.slice(CourseTopics.order.max())
            .select(CourseTopics.courseId eq courseId)
            .single().let { it[CourseTopics.order.max()] ?: 0 }
    }

    fun findById(topicId: UUID, courseId: UUID): TopicResponse? {
        return getById(topicId, courseId)?.toResponse()
    }

    fun findByCourseId(courseId: UUID): List<TopicResponse> {
        return CourseTopicDao.find(CourseTopics.courseId eq courseId)
            .map(CourseTopicDao::toResponse)
    }
}