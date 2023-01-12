package com.studiversity.feature.course.submission

import com.studiversity.feature.course.submission.usecase.FindSubmissionUseCase
import com.studiversity.feature.course.submission.usecase.FindSubmissionsByWorkUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { FindSubmissionUseCase(get(), get()) }
    single { FindSubmissionsByWorkUseCase(get(), get(), get()) }
}

private val repositoryModule = module {
    single { CourseSubmissionRepository() }
}

val courseSubmissionModule = module { includes(useCaseModule, repositoryModule) }