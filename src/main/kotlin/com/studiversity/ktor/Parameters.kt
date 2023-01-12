package com.studiversity.ktor

import com.studiversity.util.toUUID
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.util.*
import java.util.*

fun Parameters.getUuid(name: String): UUID = try {
    getOrFail(name).toUUID()
} catch (t: Throwable) {
    throw BadRequestException("INVALID_UUID")
}