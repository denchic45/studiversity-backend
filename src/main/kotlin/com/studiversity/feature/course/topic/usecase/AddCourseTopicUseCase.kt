package com.studiversity.feature.course.topic.usecase

import com.studiversity.api.course.topic.model.CreateTopicRequest
import com.studiversity.feature.course.topic.CourseTopicRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddCourseTopicUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseTopicRepository: CourseTopicRepository
) {

    operator fun invoke(courseId: UUID, createTopicRequest: CreateTopicRequest) = transactionWorker {
        courseTopicRepository.add(courseId, createTopicRequest)
    }
}