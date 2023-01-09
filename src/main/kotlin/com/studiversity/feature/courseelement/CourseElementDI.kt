package com.studiversity.feature.courseelement

import com.studiversity.feature.courseelement.repository.CourseElementRepository
import com.studiversity.feature.courseelement.usecase.AddCourseElementUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { AddCourseElementUseCase(get(), get()) }
}

private val repositoryModule = module {
    single { CourseElementRepository() }
}

val courseElementModule = module {
    includes(useCaseModule, repositoryModule)
}