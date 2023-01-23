package com.studiversity.api.course.topic.model

import com.studiversity.util.OptionalProperty
import com.studiversity.util.OptionalPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTopicRequest(
    @Serializable(OptionalPropertySerializer::class)
    val name: OptionalProperty<String> = OptionalProperty.NotPresent
)
