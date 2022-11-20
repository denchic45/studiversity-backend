package com.studiversity

import com.studiversity.feature.auth.configureAuth
import com.studiversity.plugin.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    Database.connect(
        url = "jdbc:postgresql://db.twmjqqkhwizjfmbebbxj.supabase.co:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "4G4x#!nKhwexYgM"
    )
    configureDI()
    configureSerialization()
    configureAuth()
    configureFeatures()
}
