package com.stuiversity.api.course.work.submission.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object SubmissionContentSerializer :
    JsonContentPolymorphicSerializer<SubmissionContent>(SubmissionContent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out SubmissionContent> {
        // TODO: Rewrite serialization
        return when {
            element.jsonObject["attachments"] != null -> AssignmentSubmission.serializer()
            else -> throw SerializationException("UNKNOWN_CONTENT_TYPE")
        }
    }
}