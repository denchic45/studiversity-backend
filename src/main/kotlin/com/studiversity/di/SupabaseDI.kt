package com.studiversity.di

import com.studiversity.SupabaseConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
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
            install(Storage)
        }
    }
    single { get<SupabaseClient>().realtime }
    single { get<SupabaseClient>().storage }
    single { get<Storage>()["main"] }
}