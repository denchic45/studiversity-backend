package com.studiversity.feature.course.element.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.*

object AttachmentSerializer : JsonContentPolymorphicSerializer<Attachment>(Attachment::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Attachment> {
        return when (Json.decodeFromJsonElement<AttachmentType>(element.jsonObject.getValue("type"))) {
            AttachmentType.File -> FileAttachment.serializer()
            AttachmentType.Link -> LinkAttachment.serializer()
        }
    }
}