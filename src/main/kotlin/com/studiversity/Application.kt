package com.studiversity

import com.studiversity.di.configureDI
import com.studiversity.feature.auth.configureAuth
import com.studiversity.feature.membership.configureMembership
import com.studiversity.feature.role.configureRoles
import com.studiversity.plugin.*
import com.studiversity.supabase.configureSupabase
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.module() = runBlocking {
    Database.connect(
        url = "jdbc:postgresql://db.twmjqqkhwizjfmbebbxj.supabase.co:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "4G4x#!nKhwexYgM"
    )
    configureDI()
    configureSerialization()
    configureStatusPages()
    configureSupabase()
    configureAuth()
    configureRoles()
    configureMembership()
    configureRouting()
}