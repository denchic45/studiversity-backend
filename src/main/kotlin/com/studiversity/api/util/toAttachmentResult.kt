package com.studiversity.api.util

import com.studiversity.feature.course.element.model.Attachment
import com.studiversity.feature.course.element.model.FileAttachment
import com.studiversity.feature.course.element.model.Link
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun HttpResponse.toAttachmentResult(): ResponseResult<Attachment> = toResult { response ->
    if (response.headers.contains(HttpHeaders.ContentDisposition)) {
        FileAttachment(
            response.body(),
            ContentDisposition.parse(response.headers[HttpHeaders.ContentDisposition]!!)
                .parameter(ContentDisposition.Parameters.FileName)!!
        )
    } else {
        response.body<Link>()
    }
}