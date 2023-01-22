package com.studiversity.feature.course.element

import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.feature.course.element.usecase.FindCourseElementUseCase
import com.studiversity.feature.course.element.usecase.RemoveCourseElementUseCase
import com.studiversity.feature.course.work.usecase.*
import org.koin.dsl.module

private val useCaseModule = module {
    single { AddCourseWorkUseCase(get(), get(), get(), get()) }
    single { FindCourseElementUseCase(get(), get()) }
    single { RemoveCourseElementUseCase(get(), get(), get()) }
    single { AddFileAttachmentOfCourseElementUseCase(get(), get()) }
    single { AddLinkAttachmentOfCourseElementUseCase(get(), get()) }
    single { FindCourseElementAttachmentsUseCase(get(), get()) }
    single { RemoveAttachmentOfCourseElementUseCase(get(), get()) }
}

private val repositoryModule = module {
    single { CourseElementRepository(get()) }
}

val courseElementModule = module {
    includes(useCaseModule, repositoryModule)
}