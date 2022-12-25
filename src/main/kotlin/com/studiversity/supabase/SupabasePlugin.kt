package com.studiversity.supabase

import io.github.jan.supabase.realtime.Realtime
import io.ktor.server.application.*
import org.koin.ktor.ext.inject

suspend fun Application.configureSupabase() {
    val realtime by inject<Realtime>()
    realtime.connect()
}