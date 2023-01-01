package com.studiversity.feature.studygroup

import com.studiversity.feature.studygroup.repository.StudyGroupMemberRepository
import com.studiversity.feature.studygroup.repository.StudyGroupRepository
import com.studiversity.feature.studygroup.usecase.AddStudyGroupUseCase
import com.studiversity.feature.studygroup.usecase.FindStudyGroupByIdUseCase
import com.studiversity.feature.studygroup.usecase.RemoveStudyGroupUseCase
import com.studiversity.feature.studygroup.usecase.UpdateStudyGroupUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { FindStudyGroupByIdUseCase(get(), get()) }
    single { AddStudyGroupUseCase(get(), get(), get(), get()) }
    single { UpdateStudyGroupUseCase(get()) }
    single { RemoveStudyGroupUseCase(get(), get(), get()) }
}

private val repositoryModule = module {
    single { StudyGroupRepository() }
    single { StudyGroupMemberRepository() }
}

val studyGroupModule = module {
    includes(useCaseModule, repositoryModule)
}