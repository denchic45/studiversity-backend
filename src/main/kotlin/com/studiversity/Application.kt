package com.studiversity

import com.studiversity.database.DatabaseFactory
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

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.module() = runBlocking {
    DatabaseFactory.database
    configureDI()
    configureSerialization()
    configureStatusPages()
    configureSupabase()
    configureAuth()
    configureRoles()
    configureMembership()
    configureRouting()
}