package com.studiversity.feature.course.submission.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

data class Submission(
    val id: UUID,
    val authorId: UUID,
    val courseWorkId: UUID,
    val content: SubmissionContent?
)

@Serializable
sealed interface SubmissionContent

@Serializable
data class AssignmentSubmission(
    val text: String?,
    val materialIds: List<@Serializable(UUIDSerializer::class) UUID>
) : SubmissionContent {
    companion object {
        val Empty = AssignmentSubmission(null, emptyList())
    }
}

enum class SubmissionState { NEW, CREATED, SENT, RETURNED, CANCELED_BY_AUTHOR }