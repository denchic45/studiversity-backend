package com.studiversity.util

import java.util.*

fun String.toUUID(): UUID = UUID.fromString(this)