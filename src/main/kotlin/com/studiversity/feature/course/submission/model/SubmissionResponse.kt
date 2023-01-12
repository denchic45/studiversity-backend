package com.studiversity.feature.course.submission.model

import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.feature.course.element.model.Attachment
import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable(SubmissionSerializer::class)
sealed class SubmissionResponse {
    abstract val id: UUID
    abstract val authorId: UUID
    abstract val state: SubmissionState
    abstract val courseWorkId: UUID
    abstract val type: CourseElementType
    abstract val content: SubmissionContent?
}

@Serializable
data class AssignmentSubmissionResponse(
    @Serializable(UUIDSerializer::class)
    override val id: UUID,
    @Serializable(UUIDSerializer::class)
    override val authorId: UUID,
    override val state: SubmissionState,
    @Serializable(UUIDSerializer::class)
    override val courseWorkId: UUID,
    override val content: AssignmentSubmission?
) : SubmissionResponse() {
    override val type: CourseElementType = CourseElementType.Work
}

@Serializable
sealed interface SubmissionContent

@Serializable
data class AssignmentSubmission(
    val attachments: List<Attachment>
) : SubmissionContent

enum class SubmissionState { NEW, CREATED, SUBMITTED, RETURNED, CANCELED_BY_AUTHOR }