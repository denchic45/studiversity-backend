package com.studiversity.feature.group

import com.studiversity.feature.group.members.groupMembersRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.groupRoutes() {
    routing {
        route("/group") {
            get {

            }
            post {

            }
            patch { }
            post("/archive") {

            }
            delete { }

            groupMembersRoutes()
        }
    }
}