package com.studiversity.feature.course.topic.usecase

import com.studiversity.api.course.topic.RelatedTopicElements
import com.studiversity.feature.course.topic.CourseTopicRepository
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class RemoveCourseTopicUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseTopicRepository: CourseTopicRepository
) {

    operator fun invoke(courseId: UUID, topicId: UUID, relatedTopicElements: RelatedTopicElements) = transactionWorker {
        courseTopicRepository.remove(courseId, topicId, relatedTopicElements) ?: throw NotFoundException()
    }
}