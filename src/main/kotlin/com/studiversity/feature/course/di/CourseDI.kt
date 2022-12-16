package com.studiversity.feature.course.di

import com.studiversity.feature.course.repository.CourseRepository
import com.studiversity.feature.course.usecase.AddCourseUseCase
import com.studiversity.feature.course.usecase.FindCourseByIdUseCase
import com.studiversity.feature.course.usecase.UpdateCourseUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { AddCourseUseCase(get()) }
    single { FindCourseByIdUseCase(get()) }
    single { UpdateCourseUseCase(get()) }
}

private val repositoryModule = module { single { CourseRepository() } }

val courseModule = module { includes(useCaseModule, repositoryModule) }