package com.studiversity.di

import com.studiversity.db.dao.UserDao
import org.koin.dsl.module

val daoModule = module {
    single { UserDao() }
}