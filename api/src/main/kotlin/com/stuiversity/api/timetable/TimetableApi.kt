package com.stuiversity.api.timetable

import com.stuiversity.api.common.EmptyResponseResult
import com.stuiversity.api.common.ResponseResult
import com.stuiversity.api.common.toResult
import com.stuiversity.api.timetable.model.*
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

    suspend fun putTimetableOfDay(
        weekOfYear: String,
        dayOfWeek: Int,
        putTimetableOfDayRequest: PutTimetableOfDayRequest,
    ): ResponseResult<TimetableOfDayResponse>

    suspend fun getTimetableOfDay(
        weekOfYear: String,
        dayOfWeek: Int,
        studyGroupIds: List<UUID>? = null,
        courseIds: List<UUID>? = null,
        memberIds: List<UUID>? = null,
        roomIds: List<UUID>? = null,
        sorting: List<SortingPeriods> = listOf()
    ): ResponseResult<TimetableOfDayResponse>

    suspend fun getTimetableOfDayByStudyGroupId(
        weekOfYear: String,
        dayOfWeek: Int,
        studyGroupId: UUID,
        sorting: List<SortingPeriods> = listOf()
    ): ResponseResult<TimetableOfDayResponse> = getTimetableOfDay(
        weekOfYear = weekOfYear,
        dayOfWeek = dayOfWeek,
        studyGroupIds = listOf(studyGroupId),
        sorting = sorting
    )

    suspend fun deleteTimetable(
        weekOfYear: String,
        studyGroupId: UUID
    ): EmptyResponseResult

    suspend fun deleteTimetable(
        weekOfYear: String,
        dayOfWeek: Int,
        studyGroupId: UUID
    ): EmptyResponseResult
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

    override suspend fun putTimetableOfDay(
        weekOfYear: String,
        dayOfWeek: Int,
        putTimetableOfDayRequest: PutTimetableOfDayRequest
    ): ResponseResult<TimetableOfDayResponse> = client.put("/timetables/$weekOfYear/$dayOfWeek") {
        contentType(ContentType.Application.Json)
        setBody(putTimetableOfDayRequest)
    }.toResult()

    override suspend fun getTimetableOfDay(
        weekOfYear: String,
        dayOfWeek: Int,
        studyGroupIds: List<UUID>?,
        courseIds: List<UUID>?,
        memberIds: List<UUID>?,
        roomIds: List<UUID>?,
        sorting: List<SortingPeriods>
    ): ResponseResult<TimetableOfDayResponse> = client.get("/timetables/$weekOfYear/$dayOfWeek") {
        studyGroupIds?.forEach { parameter("studyGroupId", it) }
        courseIds?.forEach { parameter("courseId", it) }
        memberIds?.forEach { parameter("memberId", it) }
        roomIds?.forEach { parameter("roomId", it) }
        parametersOf(values = sorting)
    }.toResult()

    override suspend fun deleteTimetable(weekOfYear: String, studyGroupId: UUID): EmptyResponseResult {
        return client.delete("/timetables/$weekOfYear") {
            parameter("studyGroupId", studyGroupId)
        }.toResult()
    }

    override suspend fun deleteTimetable(weekOfYear: String, dayOfWeek: Int, studyGroupId: UUID): EmptyResponseResult {
        return client.delete("/timetables/$weekOfYear/$dayOfWeek") {
            parameter("studyGroupId", studyGroupId)
        }.toResult()
    }
}