package com.studiversity.feature.course.element.usecase

import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.transaction.TransactionWorker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

class FindCourseElementsByCourseIdUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseElementRepository: CourseElementRepository
) {
    operator fun invoke(courseId: UUID, sorting: List<SortingCourseElements>?) = transactionWorker {
        courseElementRepository.findElementsByCourseId(courseId, sorting)
    }
}

@Serializable
sealed class SortingCourseElements {

    abstract val field: String
    abstract val order: SortOrder

    override fun toString() = "$field:$order"

    @SerialName("topic_id")
    class TopicId(override val order: SortOrder = SortOrder.ASC) : SortingCourseElements() {
        override val field: String = "topic_id"
    }

    companion object {
        fun of(content: String): SortingCourseElements? {
            val (field, sort) = content.split(":")
            return when (field) {
                "topic_id" -> TopicId(SortOrder.valueOf(sort))
                else -> null
            }
        }
    }
}

enum class SortOrder { ASC, DESC }