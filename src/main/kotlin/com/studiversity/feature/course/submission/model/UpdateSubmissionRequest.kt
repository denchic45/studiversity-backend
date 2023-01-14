package com.studiversity.feature.course.submission.model

import com.studiversity.util.OptionalProperty
import com.studiversity.util.OptionalPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
data class UpdateSubmissionRequest(
    @Serializable(OptionalPropertySerializer::class)
    val content: OptionalProperty<SubmissionContent?> = OptionalProperty.NotPresent,
    @Serializable(OptionalPropertySerializer::class)
    val grade: OptionalProperty<Short> = OptionalProperty.NotPresent
)