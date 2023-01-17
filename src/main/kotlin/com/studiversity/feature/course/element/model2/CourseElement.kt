package com.studiversity.feature.course.element.model2

import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.CourseWorkDetails
import com.studiversity.ktor.LocalDateSerializer
import com.studiversity.ktor.LocalTimeSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
sealed class CourseElement {
    abstract val type: CourseElementType
    abstract val details: CourseElementDetails
}

@Serializable
data class CourseWork @OptIn(ExperimentalSerializationApi::class) constructor(
    @Serializable(LocalDateSerializer::class)
    val dueDate: LocalDate?,
    @Serializable(LocalTimeSerializer::class)
    val dueTime: LocalTime?,
    val workType: CourseWorkType,
    val maxGrade: Short,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    override val details: CourseElementDetails,
    override val type: CourseElementType
) : CourseElement()

@Serializable
data class CourseMaterial(
    val text: String,
    override val details: CourseElementDetails,
    override val type: CourseElementType
) : CourseElement()