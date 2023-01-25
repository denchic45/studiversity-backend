package com.studiversity.client.di

import com.stuiversity.api.course.CoursesApi
import com.stuiversity.api.course.CoursesApiImpl
import com.stuiversity.api.course.element.CourseElementApi
import com.stuiversity.api.course.element.CourseElementApiImpl
import com.stuiversity.api.course.topic.CourseTopicApi
import com.stuiversity.api.course.topic.CourseTopicApiImpl
import com.stuiversity.api.course.work.CourseWorkApi
import com.stuiversity.api.course.work.CourseWorkApiImpl
import com.stuiversity.api.membership.MembershipsApi
import com.stuiversity.api.membership.MembershipsApiImpl
import com.stuiversity.api.submission.SubmissionsApi
import com.stuiversity.api.submission.SubmissionsApiImpl
import org.koin.dsl.module

val apiModule = module {
    factory<CourseElementApi> { CourseElementApiImpl(it.get()) }
    factory<SubmissionsApi> { SubmissionsApiImpl(it.get()) }
    factory<CoursesApi> { CoursesApiImpl(it.get()) }
    factory<CourseWorkApi> { CourseWorkApiImpl(it.get()) }
    factory<CourseTopicApi> { CourseTopicApiImpl(it.get()) }

    factory<MembershipsApi> { MembershipsApiImpl(it.get()) }
}