package com.studiversity.util

import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrapError
import com.studiversity.api.util.ResponseResult
import org.junit.jupiter.api.Assertions.assertNotEquals

fun assertResultSuccess(result: ResponseResult<*>) {
    assertNotEquals(result.get()) { result.unwrapError().error.toString() }
}