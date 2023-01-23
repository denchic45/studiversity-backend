package com.studiversity.ktor

import io.ktor.server.application.*

fun ApplicationCall.currentUserId() = jwtPrincipal().payload.claimId