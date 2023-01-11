package com.studiversity.feature.course.submission

import io.ktor.server.routing.*

fun Route.courseSubmissionRoutes() {
    route("/submissions") {
        get { }
        post { }
        submissionByIdRoute()
    }
}

fun Route.submissionByIdRoute() {
    route("/{submissionId}") {

    }
}