package com.studiversity.util

import com.studiversity.feature.course.element.usecase.SortOrder

fun SortOrder.toSqlSortOrder() = when (this) {
    SortOrder.ASC -> org.jetbrains.exposed.sql.SortOrder.ASC
    SortOrder.DESC -> org.jetbrains.exposed.sql.SortOrder.DESC
}