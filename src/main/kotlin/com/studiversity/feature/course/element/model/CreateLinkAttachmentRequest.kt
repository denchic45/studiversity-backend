package com.studiversity.feature.course.element.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkAttachmentRequest(
    val url: String
)