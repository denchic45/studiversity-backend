package com.studiversity.feature.studygroup

import com.studiversity.feature.studygroup.repository.StudyGroupRepository
import com.studiversity.feature.studygroup.usecase.*
import org.koin.dsl.module

val studyGroupModule = module {
    includes(useCaseModule, repositoryModule)
}

private val useCaseModule = module {
    single { FindStudyGroupByIdUseCase(get()) }
    single { AddStudyGroupUseCase(get()) }
    single { UpdateStudyGroupUseCase(get()) }
    single { RemoveStudyGroupUseCase(get()) }
    single {
        EnrollStudyGroupMemberUseCase(
            roleRepository = get(),
            studyGroupMemberRepository = get()
        )
    }
}

val repositoryModule = module {
    single { StudyGroupRepository() }
}