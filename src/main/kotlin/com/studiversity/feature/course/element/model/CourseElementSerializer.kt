package com.studiversity.feature.course.element.model

import com.studiversity.feature.course.element.CourseElementType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.*

object CourseElementSerializer :
    JsonContentPolymorphicSerializer<CreateCourseElementRequest>(CreateCourseElementRequest::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out CreateCourseElementRequest> =
        when (Json.decodeFromJsonElement<CourseElementType>(element.jsonObject.getValue("type"))) {
            CourseElementType.Work -> CreateCourseWorkRequest.serializer()
            CourseElementType.Material -> TODO()
        }


}