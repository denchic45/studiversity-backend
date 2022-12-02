package com.studiversity.di

import com.studiversity.database.dao.UserDao
import org.koin.dsl.module

val daoModule = module {
    single { UserDao() }
}