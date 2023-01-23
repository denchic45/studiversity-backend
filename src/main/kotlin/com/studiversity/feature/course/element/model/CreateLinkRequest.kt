package com.studiversity.feature.course.element.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkRequest(val url: String) : AttachmentRequest