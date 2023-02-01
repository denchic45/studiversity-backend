package com.studiversity.client.timetable

import com.github.michaelbull.result.unwrap
import com.studiversity.KtorClientTest
import com.studiversity.util.assertResultOk
import com.studiversity.util.toUUID
import com.stuiversity.api.course.CoursesApi
import com.stuiversity.api.course.model.CourseResponse
import com.stuiversity.api.course.model.CreateCourseRequest
import com.stuiversity.api.studygroup.StudyGroupApi
import com.stuiversity.api.studygroup.model.AcademicYear
import com.stuiversity.api.studygroup.model.CreateStudyGroupRequest
import com.stuiversity.api.studygroup.model.StudyGroupResponse
import com.stuiversity.api.timetable.TimetableApi
import com.stuiversity.api.timetable.model.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TimetableTest : KtorClientTest() {

    private val studyGroupApi: StudyGroupApi by inject { parametersOf(client) }
    private val courseApi: CoursesApi by inject { parametersOf(client) }
    private val timetableApi: TimetableApi by inject { parametersOf(client) }

    private val weekOfYear = LocalDate.of(2023, 2, 1).format(DateTimeFormatter.ofPattern("yyyy_ww"))

    private val teacher1Id = "02f00b3e-3a78-4431-87d4-34128ebbb04c".toUUID()
    private val teacher2Id = "96c42f53-2994-4845-9677-40bea853e56b".toUUID()

    private lateinit var studyGroup1: StudyGroupResponse
    private lateinit var studyGroup2: StudyGroupResponse
    private lateinit var mathCourse: CourseResponse
    private lateinit var engCourse: CourseResponse
    private lateinit var physicsCourse: CourseResponse

    private fun timetableForStudyGroup1() = PutTimetableRequest(
                studyGroupId = studyGroup1.id,
                monday = listOf(
                    LessonRequest(1, null, listOf(teacher1Id), LessonDetails(mathCourse.id)),
                    LessonRequest(2, null, listOf(teacher1Id), LessonDetails(mathCourse.id)),
                    LessonRequest(3, null, listOf(teacher2Id), LessonDetails(engCourse.id))
                ),
                tuesday = listOf(
                    LessonRequest(0, null, listOf(teacher1Id), LessonDetails(mathCourse.id)),
                    LessonRequest(1, null, listOf(teacher1Id), LessonDetails(mathCourse.id)),
                    LessonRequest(2, null, listOf(teacher2Id), LessonDetails(mathCourse.id)),
                    LessonRequest(3, null, listOf(teacher2Id), LessonDetails(engCourse.id))
                ),
                wednesday = emptyList(),
                thursday = emptyList(),
                friday = emptyList()
            )

    fun timetableForStudyGroup2(): PutTimetableRequest = PutTimetableRequest(
          studyGroupId = studyGroup2.id,
          monday = listOf(
              LessonRequest(1, null, listOf(teacher2Id), LessonDetails(mathCourse.id)),
              LessonRequest(2, null, listOf(teacher2Id), LessonDetails(mathCourse.id)),
              LessonRequest(3, null, listOf(teacher1Id), LessonDetails(engCourse.id))
          ),
          tuesday = listOf(
              LessonRequest(0, null, listOf(teacher2Id), LessonDetails(mathCourse.id)),
              LessonRequest(1, null, listOf(teacher2Id), LessonDetails(mathCourse.id)),
              LessonRequest(2, null, listOf(teacher1Id), LessonDetails(mathCourse.id)),
              LessonRequest(3, null, listOf(teacher1Id), LessonDetails(engCourse.id))
          ),
          wednesday = emptyList(),
          thursday = emptyList(),
          friday = emptyList()
      )

    @BeforeEach
    fun init(): Unit = runBlocking {
        studyGroup1 = studyGroupApi.create(
            CreateStudyGroupRequest("Test group PKS 4", AcademicYear(2019, 2023), null, null)
        ).also(::assertResultOk).unwrap()

        studyGroup2 = studyGroupApi.create(
            CreateStudyGroupRequest("Test group SSA 4", AcademicYear(2023, 2027), null, null)
        ).also(::assertResultOk).unwrap()

        mathCourse = courseApi.create(CreateCourseRequest("Math")).also(::assertResultOk).unwrap()
        engCourse = courseApi.create(CreateCourseRequest("English")).also(::assertResultOk).unwrap()
        physicsCourse = courseApi.create(CreateCourseRequest("Physics")).also(::assertResultOk).unwrap()
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
        studyGroupApi.delete(studyGroup1.id).also(::assertResultOk)
        studyGroupApi.delete(studyGroup2.id).also(::assertResultOk)

        courseApi.setArchive(mathCourse.id).also(::assertResultOk)
        courseApi.setArchive(engCourse.id).also(::assertResultOk)
        courseApi.setArchive(physicsCourse.id).also(::assertResultOk)

        courseApi.delete(mathCourse.id).also(::assertResultOk)
        courseApi.delete(engCourse.id).also(::assertResultOk)
        courseApi.delete(physicsCourse.id).also(::assertResultOk)
    }

    @Test
    fun testCreateTimetable(): Unit = runBlocking {
        val request = timetableForStudyGroup1()
        val response = putTimetable(request)

        assertIterableEquals(combineAllPeriods(request).map(PeriodRequest::details), combineAllPeriods(response).map(PeriodResponse::details))
    }

    private suspend fun putTimetable(request: PutTimetableRequest): TimetableResponse {
        return timetableApi.putTimetable(weekOfYear, request).also(::assertResultOk).unwrap()
    }

    @Test
    fun testGetTimetableByStudyGroup(): Unit = runBlocking {
        val request = timetableForStudyGroup1()
        putTimetable(request)

        val response = timetableApi.getTimetableByStudyGroupId(weekOfYear, studyGroup1.id)
            .also(::assertResultOk).unwrap()

        assertIterableEquals(combineAllPeriods(request).map(PeriodRequest::details), combineAllPeriods(response).map(PeriodResponse::details))
    }

    @Test
    fun testGetTimetableByMember(): Unit = runBlocking {
        val request1 = timetableForStudyGroup1()
        putTimetable(request1)
        val request2 = timetableForStudyGroup2()
        putTimetable(request2)

        val response = timetableApi.getTimetable(weekOfYear, memberIds = listOf(teacher1Id))
            .also(::assertResultOk).unwrap()

        val expectedPeriodDetails = listOf(
            LessonDetails(mathCourse.id),
            LessonDetails(mathCourse.id),
            LessonDetails(engCourse.id),

            LessonDetails(mathCourse.id),
            LessonDetails(mathCourse.id),
            LessonDetails(mathCourse.id),
            LessonDetails(engCourse.id)
        )


        val periodResponseDetailsByMemberId = combineAllPeriods(response).map(PeriodResponse::details)
        assertIterableEquals(expectedPeriodDetails, periodResponseDetailsByMemberId)
    }

    private fun combineAllPeriods(request: PutTimetableRequest) = with(request) {
        monday + tuesday + wednesday + thursday + friday + saturday
    }

    private fun combineAllPeriods(response: TimetableResponse) = with(response) {
        monday + tuesday + wednesday + thursday + friday + saturday
    }
}