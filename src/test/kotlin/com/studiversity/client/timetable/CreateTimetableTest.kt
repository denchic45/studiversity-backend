package com.studiversity.client.timetable

import com.github.michaelbull.result.unwrap
import com.studiversity.KtorClientTest
import com.studiversity.util.assertResultOk
import com.stuiversity.api.course.CoursesApi
import com.stuiversity.api.course.model.CourseResponse
import com.stuiversity.api.course.model.CreateCourseRequest
import com.stuiversity.api.studygroup.StudyGroupApi
import com.stuiversity.api.studygroup.model.AcademicYear
import com.stuiversity.api.studygroup.model.CreateStudyGroupRequest
import com.stuiversity.api.studygroup.model.StudyGroupResponse
import com.stuiversity.api.timetable.TimetableApi
import com.stuiversity.api.timetable.model.LessonDetails
import com.stuiversity.api.timetable.model.LessonRequest
import com.stuiversity.api.timetable.model.PeriodRequest
import com.stuiversity.api.timetable.model.PutTimetableRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateTimetableTest : KtorClientTest() {

    private val studyGroupApi: StudyGroupApi by inject { parametersOf(client) }
    private val courseApi: CoursesApi by inject { parametersOf(client) }
    private val timetableApi: TimetableApi by inject { parametersOf(client) }

    private val weekOfYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_ww"))

    private lateinit var studyGroup: StudyGroupResponse
    private lateinit var mathCourse: CourseResponse
    private lateinit var engCourse: CourseResponse
    private lateinit var physicsCourse: CourseResponse

    override fun setup(): Unit = runBlocking {
        studyGroup = studyGroupApi.create(CreateStudyGroupRequest("Test group", AcademicYear(2023, 2027), null, null))
            .also(::assertResultOk).unwrap()

        mathCourse = courseApi.create(CreateCourseRequest("Math")).also(::assertResultOk).unwrap()
        engCourse = courseApi.create(CreateCourseRequest("English")).also(::assertResultOk).unwrap()
        physicsCourse = courseApi.create(CreateCourseRequest("Physics")).also(::assertResultOk).unwrap()
    }

    override fun cleanup(): Unit = runBlocking {
        studyGroupApi.delete(studyGroup.id)
        courseApi.delete(mathCourse.id)
    }

    @Test
    fun testCreateTimetable(): Unit = runBlocking {
        val monday = listOf(
            LessonRequest(1, null, LessonDetails(mathCourse.id)),
            LessonRequest(2, null, LessonDetails(mathCourse.id)),
            LessonRequest(3, null, LessonDetails(engCourse.id))
        )
        val tuesday = listOf(
            LessonRequest(0, null, LessonDetails(mathCourse.id)),
            LessonRequest(1, null, LessonDetails(mathCourse.id)),
            LessonRequest(2, null, LessonDetails(mathCourse.id)),
            LessonRequest(3, null, LessonDetails(engCourse.id))
        )
        val wednesday = listOf<PeriodRequest>()
        val thursday = listOf(
            LessonRequest(2, null, LessonDetails(mathCourse.id)),
            LessonRequest(3, null, LessonDetails(engCourse.id))
        )
        val friday = listOf<PeriodRequest>()
        val response = timetableApi.putTimetable(
            studyGroup.id, weekOfYear,
            PutTimetableRequest(
                monday = monday,
                tuesday = tuesday,
                wednesday = wednesday,
                thursday = thursday,
                friday = friday
            ),
        ).also(::assertResultOk).unwrap()


        Assertions.assertIterableEquals(monday.map { it.details }, response.monday.map { it.details })
        Assertions.assertIterableEquals(tuesday.map { it.details }, response.tuesday.map { it.details })
        Assertions.assertIterableEquals(wednesday.map { it.details }, response.wednesday.map { it.details })
        Assertions.assertIterableEquals(thursday.map { it.details }, response.thursday.map { it.details })
        Assertions.assertIterableEquals(friday.map { it.details }, response.friday.map { it.details })
    }
}