package com.stuiversity.util

import com.stuiversity.api.timetable.model.Lesson
import com.stuiversity.api.timetable.model.PeriodResponse
import com.stuiversity.api.timetable.model.PeriodType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.*

object PeriodSerializer:JsonContentPolymorphicSerializer<PeriodResponse>(PeriodResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out PeriodResponse> {
        TODO("Not yet implemented")
//        return when (Json.decodeFromJsonElement<PeriodType>(element.jsonObject.getValue("type"))) {
//            PeriodType.LESSON -> Lesson.serializer()
//            PeriodType.EVENT -> TODO()
//        }
    }
}