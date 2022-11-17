package com.studiversity

import io.ktor.server.application.*
import com.studiversity.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureRouting()
}
