package com.studiversity.feature.course.subject.usecase

import com.studiversity.feature.course.subject.SubjectRepository
import com.studiversity.feature.course.subject.model.SubjectResponse

class FindAllSubjectsUseCase(private val subjectRepository: SubjectRepository) {

    operator fun invoke(): List<SubjectResponse> {
        return subjectRepository.findAll()
    }
}