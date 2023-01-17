package com.studiversity.feature.course.work.submission.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GradeRequest(
    val value: Short,
)


@Serializable
data class Grade(
    val value: Int,
    @Serializable(UUIDSerializer::class)
    val courseId: UUID,
    @Serializable(UUIDSerializer::class)
    val studentId: UUID,
    @Serializable(UUIDSerializer::class)
    val gradedBy: UUID,
    @Serializable(UUIDSerializer::class)
    val submissionId: UUID?
)

@Serializable
data class SubmissionGrade(
    val value: Short,
    @Serializable(UUIDSerializer::class)
    val courseId: UUID,
    @Serializable(UUIDSerializer::class)
    val gradedBy: UUID,
    @Serializable(UUIDSerializer::class)
    val submissionId: UUID
)