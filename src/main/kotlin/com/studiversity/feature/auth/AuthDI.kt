package com.studiversity.feature.auth

import com.studiversity.di.SupabaseEnv
import com.studiversity.feature.auth.usecase.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val useCaseModule = module {
    single { SignUpUseCase(get(), get()) }
    single { SignUpUserManuallyUseCase(get(), get(), get()) }
    single { SignInByEmailAndPasswordUseCase(get(), get()) }
    single { RefreshTokenUseCase(get(), get()) }
    single { RecoverPasswordUseCase(get(), get(), get()) }
    single { CheckMagicLinkTokenUseCase(get(), get()) }
}

val authModule = module {
    includes(useCaseModule)
    single {
        HttpClient(CIO) {
            defaultRequest {
                url(get<String>(named(SupabaseEnv.SUPABASE_URL)))
                header("apikey", get<String>(named(SupabaseEnv.SUPABASE_KEY)))
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                })
            }
        }
    }
}