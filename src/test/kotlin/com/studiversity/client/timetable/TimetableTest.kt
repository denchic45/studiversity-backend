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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val UNKNOWN_ID = -1L

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

    private fun timetableForStudyGroup2(): PutTimetableRequest = PutTimetableRequest(
        studyGroupId = studyGroup2.id,
        monday = listOf(
            LessonRequest(1, null, listOf(teacher2Id), LessonDetails(mathCourse.id)),
            LessonRequest(2, null, listOf(teacher2Id), LessonDetails(engCourse.id)),
            LessonRequest(3, null, listOf(teacher1Id), LessonDetails(mathCourse.id))
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
            CreateStudyGroupRequest("Test group PKS 4.1", AcademicYear(2019, 2023), null, null)
        ).also(::assertResultOk).unwrap()

        studyGroup2 = studyGroupApi.create(
            CreateStudyGroupRequest("Test group PKS 4.2", AcademicYear(2023, 2027), null, null)
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

        assertIterableEquals(
            request.toFlatPeriods().map(PeriodRequest::details),
            response.toFlatPeriods().map(PeriodResponse::details)
        )
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

        assertIterableEquals(
            request.toFlatPeriods().map(PeriodRequest::details),
            response.toFlatPeriods().map(PeriodResponse::details)
        )
    }

    @Test
    fun testGetTimetableByMember(): Unit = runBlocking {
        val request1 = timetableForStudyGroup1()
        putTimetable(request1)
        val request2 = timetableForStudyGroup2()
        putTimetable(request2)

        val response = timetableApi.getTimetable(weekOfYear, memberIds = listOf(teacher1Id))
            .also(::assertResultOk).unwrap()

        val expectedTimetableResponse = TimetableResponse(
            monday = listOf(
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 30),
                    1,
                    null,
                    studyGroup1.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 30),
                    2,
                    null,
                    studyGroup1.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 30),
                    3,
                    null,
                    studyGroup2.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                )
            ),
            tuesday = listOf(
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    0,
                    null,
                    studyGroup1.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    1,
                    null,
                    studyGroup1.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    2,
                    null,
                    studyGroup2.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    3,
                    null,
                    studyGroup2.id,
                    listOf(teacher1Id),
                    LessonDetails(engCourse.id)
                )
            ),
            wednesday = emptyList(),
            thursday = emptyList(),
            friday = emptyList(),
            saturday = listOf()
        )

        expectedTimetableResponse.toFlatPeriods().zip(response.toFlatPeriods())
            .forEach { (expected, actual) ->
                assertEquals(expected.date, actual.date)
                assertEquals(expected.order, actual.order)
                assertEquals(expected.studyGroupId, actual.studyGroupId)
                assertEquals(expected.details, actual.details) { expected.toString() + actual.toString() }
                assertEquals(expected.roomId, actual.roomId)
            }
    }

    @Test
    fun testGetTimetableByCourseId(): Unit = runBlocking {
        val request1 = timetableForStudyGroup1()
        putTimetable(request1)
        val request2 = timetableForStudyGroup2()
        putTimetable(request2)

        val response = timetableApi.getTimetable(
            weekOfYear = weekOfYear,
            courseIds = listOf(mathCourse.id),
            sorting = arrayOf(SortingPeriods.StudyGroup())
        ).also(::assertResultOk).unwrap()

        val expectedTimetableResponse = TimetableResponse(
            monday = listOf(
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 30),
                    1,
                    null,
                    studyGroup1.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 30),
                    2,
                    null,
                    studyGroup1.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 30),
                    1,
                    null,
                    studyGroup2.id,
                    listOf(teacher2Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 30),
                    3,
                    null,
                    studyGroup2.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                )
            ),
            tuesday = listOf(
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    0,
                    null,
                    studyGroup1.id,
                    listOf(teacher2Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    1,
                    null,
                    studyGroup1.id,
                    listOf(teacher2Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    2,
                    null,
                    studyGroup1.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    0,
                    null,
                    studyGroup2.id,
                    listOf(teacher2Id),
                    LessonDetails(mathCourse.id)
                ),
                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    1,
                    null,
                    studyGroup2.id,
                    listOf(teacher2Id),
                    LessonDetails(mathCourse.id)
                ),

                LessonResponse(
                    UNKNOWN_ID,
                    LocalDate.of(2023, 1, 31),
                    2,
                    null,
                    studyGroup2.id,
                    listOf(teacher1Id),
                    LessonDetails(mathCourse.id)
                ),
            ),
            wednesday = emptyList(),
            thursday = emptyList(),
            friday = emptyList(),
            saturday = emptyList()
        )

        expectedTimetableResponse.toFlatPeriods().zip(response.toFlatPeriods())
            .forEach { (expected, actual) ->
                assertEquals(expected.studyGroupId, actual.studyGroupId)
                assertEquals(expected.date, actual.date)
                assertEquals(expected.details, actual.details) { expected.toString() + actual.toString() }
                assertEquals(expected.order, actual.order)
                assertEquals(expected.roomId, actual.roomId)
            }
    }

    @Test
    fun testGetTimetableByRoom(): Unit = runBlocking {
        val request1 = timetableForStudyGroup1()
        putTimetable(request1)
        val request2 = timetableForStudyGroup2()
        putTimetable(request2)
    }

    private fun PutTimetableRequest.toFlatPeriods() = run {
        monday + tuesday + wednesday + thursday + friday + saturday
    }

    private fun TimetableResponse.toFlatPeriods() = run {
        monday + tuesday + wednesday + thursday + friday + saturday
    }
}