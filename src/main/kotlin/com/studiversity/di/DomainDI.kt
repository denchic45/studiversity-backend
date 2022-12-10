package com.studiversity.di

import com.studiversity.feature.group.usecase.AddStudyGroupUseCase
import com.studiversity.feature.group.usecase.FindStudyGroupByIdUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { FindStudyGroupByIdUseCase(get()) }
    factory { AddStudyGroupUseCase(get()) }
}