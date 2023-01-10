package com.studiversity.feature.course.element

import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.feature.course.element.usecase.AddCourseElementUseCase
import com.studiversity.feature.course.element.usecase.FindCourseElementUseCase
import com.studiversity.feature.course.element.usecase.RemoveCourseElementUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { AddCourseElementUseCase(get(), get()) }
    single { FindCourseElementUseCase(get(), get()) }
    single { RemoveCourseElementUseCase(get(),get()) }
}

private val repositoryModule = module {
    single { CourseElementRepository() }
}

val courseElementModule = module {
    includes(useCaseModule, repositoryModule)
}