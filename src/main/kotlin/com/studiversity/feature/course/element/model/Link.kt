package com.studiversity.feature.course.element.model

import kotlinx.serialization.Serializable

@Serializable
data class LinkRequest(val url: String)

@Serializable
data class Link(
    val url: String,
    val name: String,
    val thumbnailUrl: String?
)