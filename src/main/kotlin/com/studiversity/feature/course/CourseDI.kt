package com.studiversity.feature.course

import com.studiversity.feature.course.repository.CourseRepository
import com.studiversity.feature.course.usecase.AddCourseUseCase
import com.studiversity.feature.course.usecase.FindCourseByIdUseCase
import com.studiversity.feature.course.usecase.RequireExistCourseUseCase
import com.studiversity.feature.course.usecase.UpdateCourseUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { AddCourseUseCase(get(), get(), get(), get()) }
    single { FindCourseByIdUseCase(get(), get()) }
    single { UpdateCourseUseCase(get()) }
    single { RequireExistCourseUseCase(get(), get()) }
}

private val repositoryModule = module { single { CourseRepository() } }

val courseModule = module { includes(useCaseModule, repositoryModule) }