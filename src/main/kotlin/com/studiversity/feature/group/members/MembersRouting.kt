package com.studiversity.feature.group.members

import io.ktor.server.routing.*

fun Route.groupMembersRoutes() {
    route("/members") {
        get { }
        route("/{id}") {
            get { }
            put { }
            delete { }
        }
    }
}