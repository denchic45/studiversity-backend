package com.studiversity.client.di

import com.stuiversity.api.auth.AuthApi
import com.stuiversity.api.auth.AuthApiImpl
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
import com.stuiversity.api.user.UserApi
import com.stuiversity.api.user.UserApiImpl
import org.koin.dsl.module

val apiModule = module {
    factory<AuthApi> { AuthApiImpl(it.get()) }
    factory<UserApi> { UserApiImpl(it.get()) }
    factory<CourseElementApi> { CourseElementApiImpl(it.get()) }
    factory<SubmissionsApi> { SubmissionsApiImpl(it.get()) }
    factory<CoursesApi> { CoursesApiImpl(it.get()) }
    factory<CourseWorkApi> { CourseWorkApiImpl(it.get()) }
    factory<CourseTopicApi> { CourseTopicApiImpl(it.get()) }
    factory<MembershipsApi> { MembershipsApiImpl(it.get()) }
}