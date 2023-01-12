package com.studiversity.feature.course.submission.model

import com.studiversity.feature.course.element.CourseElementType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.*

object SubmissionSerializer :
    JsonContentPolymorphicSerializer<SubmissionResponse>(SubmissionResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out SubmissionResponse> =
        when (Json.decodeFromJsonElement<CourseElementType>(element.jsonObject.getValue("type"))) {
            CourseElementType.Work -> AssignmentSubmissionResponse.serializer()
            CourseElementType.Material -> TODO()
        }
}