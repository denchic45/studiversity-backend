package com.studiversity.feature.course.submission

import com.studiversity.feature.course.submission.usecase.FindSubmissionUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { FindSubmissionUseCase(get(), get()) }
}

private val repositoryModule = module {
    single { CourseSubmissionRepository() }
}

val courseSubmissionModule = module { includes(useCaseModule, repositoryModule) }