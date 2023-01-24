package com.studiversity.feature.course.element.model

import com.studiversity.ktor.UUIDSerializer
import com.studiversity.util.OptionalProperty
import com.studiversity.util.OptionalPropertySerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UpdateCourseElementRequest(
    @Serializable(OptionalPropertySerializer::class)
    val topicId: OptionalProperty<@Serializable(UUIDSerializer::class) UUID> = OptionalProperty.NotPresent
)