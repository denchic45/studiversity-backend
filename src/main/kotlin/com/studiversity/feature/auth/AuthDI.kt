package com.studiversity.feature.auth

import com.studiversity.SupabaseConstants
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val authModule = module {
    single {
        HttpClient(CIO) {
            defaultRequest {
                url(SupabaseConstants.url)
                header("apikey", SupabaseConstants.key)
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }
}