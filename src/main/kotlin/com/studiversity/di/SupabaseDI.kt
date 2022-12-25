package com.studiversity.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import org.koin.dsl.module

val supabaseClientModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://twmjqqkhwizjfmbebbxj.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR3bWpxcWtod2l6amZtYmViYnhqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NjgzMjg3MjYsImV4cCI6MTk4MzkwNDcyNn0.RN50A80UDWYr7MIlO5E2XPdGHyQgooG3hRVXyeFlRjE"
        ) {
            install(Realtime) {
                // settings
            }
        }
    }
    single { get<SupabaseClient>().realtime }
}