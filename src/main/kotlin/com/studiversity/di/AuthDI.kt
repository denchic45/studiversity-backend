package com.studiversity.di

import com.studiversity.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import org.koin.dsl.module

val authModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = Constants.url,
            supabaseKey = Constants.key
        ) {
            install(GoTrue)
        }
    }

    single { get<SupabaseClient>().gotrue }
}