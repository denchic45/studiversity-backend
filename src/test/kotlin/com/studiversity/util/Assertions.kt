package com.studiversity.util

import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import com.stuiversity.api.util.ResponseResult
import org.junit.jupiter.api.Assertions.assertNotNull

fun assertResultOk(result: ResponseResult<*>) {
    assertNotNull(result.get()) { "status: " + result.unwrapError().code.toString() + " reason: " + result.unwrapError().error.toString() }
}

fun assertResultErr(result: ResponseResult<*>) {
    assertNotNull(result.getError()) { result.unwrap().toString() }
}