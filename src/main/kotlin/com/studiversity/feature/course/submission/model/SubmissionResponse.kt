package com.studiversity.feature.course.submission.model

import com.studiversity.feature.course.element.model.Attachment
import kotlinx.serialization.Serializable
import java.util.*

data class SubmissionResponse(
    val id: UUID,
    val authorId: UUID,
    val state: SubmissionState,
    val courseWorkId: UUID,
    val content: SubmissionContent?
)

@Serializable
sealed interface SubmissionContent

@Serializable
data class AssignmentSubmission(
    val attachments: List<Attachment>
) : SubmissionContent

enum class SubmissionState { NEW, CREATED, SUBMITTED, RETURNED, CANCELED_BY_AUTHOR }