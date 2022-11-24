package com.studiversity.di

import com.studiversity.di.appModule
import com.studiversity.di.daoModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    // Install Ktor features
    install(Koin) {
        slf4jLogger()
        modules(appModule(), authModule,daoModule)
    }
}