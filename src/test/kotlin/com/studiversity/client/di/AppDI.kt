package com.studiversity.client.di

import com.studiversity.api.course.CoursesApi
import com.studiversity.api.course.CoursesApiImpl
import com.studiversity.api.course.element.CourseElementApi
import com.studiversity.api.course.element.CourseElementApiImpl
import com.studiversity.api.course.topic.CourseTopicApi
import com.studiversity.api.course.topic.CourseTopicApiImpl
import com.studiversity.api.course.work.CourseWorkApi
import com.studiversity.api.course.work.CourseWorkApiImpl
import com.studiversity.api.membership.MembershipsApi
import com.studiversity.api.membership.MembershipsApiImpl
import com.studiversity.api.submission.SubmissionsApi
import com.studiversity.api.submission.SubmissionsApiImpl
import org.koin.dsl.module

val apiModule = module {
    factory<CourseElementApi> { CourseElementApiImpl(it.get()) }
    factory<SubmissionsApi> { SubmissionsApiImpl(it.get()) }
    factory<CoursesApi> { CoursesApiImpl(it.get()) }
    factory<CourseWorkApi> { CourseWorkApiImpl(it.get()) }
    factory<CourseTopicApi> { CourseTopicApiImpl(it.get()) }

    factory<MembershipsApi> { MembershipsApiImpl(it.get()) }
}