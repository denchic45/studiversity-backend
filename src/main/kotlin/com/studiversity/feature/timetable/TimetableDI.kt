package com.studiversity.feature.timetable

import com.studiversity.feature.timetable.usecase.FindTimetableByStudyGroupUseCase
import com.studiversity.feature.timetable.usecase.PutTimetableUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { PutTimetableUseCase(get(), get()) }
    single { FindTimetableByStudyGroupUseCase(get(), get()) }
}

private val repositoryModule = module {
    single { TimetableRepository() }
}

val timetableModule = module {
    includes(useCaseModule, repositoryModule)
}