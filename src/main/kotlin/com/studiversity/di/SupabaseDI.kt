package com.studiversity.di

import com.studiversity.SupabaseConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import org.koin.dsl.module

val supabaseClientModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = SupabaseConstants.url,
            supabaseKey = SupabaseConstants.key
        ) {
            install(Realtime) {
                // settings
            }
        }
    }
    single { get<SupabaseClient>().realtime }
}