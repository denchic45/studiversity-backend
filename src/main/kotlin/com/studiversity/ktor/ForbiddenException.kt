package com.studiversity.ktor


class ForbiddenException(message: String? = "PERMISSION_DENIED") : Exception(message)