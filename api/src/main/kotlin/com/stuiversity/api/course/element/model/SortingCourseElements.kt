package com.stuiversity.api.course.element.model

import com.stuiversity.api.util.SortOrder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SortingCourseElements {

    abstract val field: String
    abstract val order: SortOrder

    override fun toString() = "$field:$order"

    @SerialName("topic_id")
    class TopicId(override val order: SortOrder = SortOrder.ASC) : SortingCourseElements() {
        override val field: String = "topic_id"
    }

    companion object {
        fun of(content: String): SortingCourseElements? {
            val (field, sort) = content.split(":")
            return when (field) {
                "topic_id" -> TopicId(SortOrder.valueOf(sort))
                else -> null
            }
        }
    }
}