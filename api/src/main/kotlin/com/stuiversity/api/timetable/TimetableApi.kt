package com.stuiversity.api.timetable

import com.stuiversity.api.common.ResponseResult
import com.stuiversity.api.common.toResult
import com.stuiversity.api.timetable.model.PutTimetableRequest
import com.stuiversity.api.timetable.model.TimetableResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.*

interface TimetableApi {
    suspend fun putTimetable(
        studyGroupId: UUID,
        weekOfYear: String,
        putTimetableRequest: PutTimetableRequest
    ): ResponseResult<TimetableResponse>
}

class TimetableApiImpl(private val client: HttpClient) : TimetableApi {
    override suspend fun putTimetable(
        studyGroupId: UUID,
        weekOfYear: String,
        putTimetableRequest: PutTimetableRequest
    ): ResponseResult<TimetableResponse> {
        return client.put("/studygroups/$studyGroupId/timetables/$weekOfYear") {
            contentType(ContentType.Application.Json)
            setBody(putTimetableRequest)
        }.toResult()
    }
}