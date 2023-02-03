package com.stuiversity.api.timetable

import com.stuiversity.api.common.ResponseResult
import com.stuiversity.api.common.toResult
import com.stuiversity.api.timetable.model.PutTimetableRequest
import com.stuiversity.api.timetable.model.SortingPeriods
import com.stuiversity.api.timetable.model.TimetableResponse
import com.stuiversity.util.parametersOf
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.*

interface TimetableApi {
    suspend fun putTimetable(
        weekOfYear: String,
        putTimetableRequest: PutTimetableRequest
    ): ResponseResult<TimetableResponse>

    suspend fun getTimetable(
        weekOfYear: String,
        studyGroupIds: List<UUID>? = null,
        courseIds: List<UUID>? = null,
        memberIds: List<UUID>? = null,
        roomIds: List<UUID>? = null,
        sorting: List<SortingPeriods> = listOf()
    ): ResponseResult<TimetableResponse>

    suspend fun getTimetableByStudyGroupId(
        weekOfYear: String,
        studyGroupId: UUID,
        sorting: List<SortingPeriods> = listOf()
    ): ResponseResult<TimetableResponse> = getTimetable(
        weekOfYear = weekOfYear,
        studyGroupIds = listOf(studyGroupId),
        sorting = sorting
    )
}

class TimetableApiImpl(private val client: HttpClient) : TimetableApi {
    override suspend fun putTimetable(
        weekOfYear: String,
        putTimetableRequest: PutTimetableRequest
    ): ResponseResult<TimetableResponse> {
        return client.put("/timetables/$weekOfYear") {
            contentType(ContentType.Application.Json)
            setBody(putTimetableRequest)
        }.toResult()
    }

    override suspend fun getTimetable(
        weekOfYear: String,
        studyGroupIds: List<UUID>?,
        courseIds: List<UUID>?,
        memberIds: List<UUID>?,
        roomIds: List<UUID>?,
        sorting: List<SortingPeriods>
    ): ResponseResult<TimetableResponse> = client.get("/timetables/$weekOfYear") {
        studyGroupIds?.forEach { parameter("studyGroupId", it) }
        courseIds?.forEach { parameter("courseId", it) }
        memberIds?.forEach { parameter("memberId", it) }
        roomIds?.forEach { parameter("roomId", it) }
        parametersOf(values = sorting)
    }.toResult()
}