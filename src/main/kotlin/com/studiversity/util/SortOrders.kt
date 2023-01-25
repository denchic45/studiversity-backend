package com.studiversity.util

import com.stuiversity.api.util.SortOrder


fun SortOrder.toSqlSortOrder() = when (this) {
    SortOrder.ASC -> org.jetbrains.exposed.sql.SortOrder.ASC
    SortOrder.DESC -> org.jetbrains.exposed.sql.SortOrder.DESC
}