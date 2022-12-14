package com.studiversity.feature.studygroup

import com.studiversity.feature.studygroup.repository.StudyGroupMemberRepository
import com.studiversity.feature.studygroup.repository.StudyGroupRepository
import com.studiversity.feature.studygroup.usecase.*
import org.koin.dsl.module

private val useCaseModule = module {
    single { FindStudyGroupByIdUseCase(get()) }
    single { AddStudyGroupUseCase(get()) }
    single { UpdateStudyGroupUseCase(get()) }
    single { RemoveStudyGroupUseCase(get()) }
}

private val repositoryModule = module {
    single { StudyGroupRepository() }
    single { StudyGroupMemberRepository() }
}

val studyGroupModule = module {
    includes(useCaseModule, repositoryModule)
}