package com.studiversity.feature.course.work.submission.model

import io.ktor.server.plugins.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object SubmissionContentSerializer :
    JsonContentPolymorphicSerializer<SubmissionContent>(SubmissionContent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out SubmissionContent> =
        when {
            element.jsonObject["attachments"] != null -> AssignmentSubmission.serializer()
            else -> throw BadRequestException("UNKNOWN_CONTENT_TYPE")
        }
}