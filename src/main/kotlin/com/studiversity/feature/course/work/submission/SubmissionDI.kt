package com.studiversity.feature.course.work.submission

import com.studiversity.feature.course.work.submission.usecase.*
import org.koin.dsl.module

private val useCaseModule = module {
    single { FindSubmissionUseCase(get(), get()) }
    single { FindSubmissionsByWorkUseCase(get(), get(), get()) }
    single { FindSubmissionByStudentUseCase(get(), get(), get(), get()) }
    single { UpdateSubmissionContentUseCase(get(), get()) }
    single { SubmitSubmissionUseCase(get(), get()) }
    single { GradeSubmissionUseCase(get(), get(), get()) }
}

private val repositoryModule = module {
    single { CourseSubmissionRepository(get()) }
}

val courseSubmissionModule = module { includes(useCaseModule, repositoryModule) }