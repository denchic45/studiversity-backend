package com.studiversity.feature.membership.controller

import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.toUUID
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import java.util.*

class MembershipControllerFactory(
    private val call: ApplicationCall
) : KoinComponent {

    private val scopeId = call.parameters["id"]!!.toUUID()
    private val currentUserId = call.jwtPrincipal().payload.claimId

    fun create(type: String): MembershipController {
        return when (type) {
            "manual",
            "self" -> get(named(type)) { parametersOf(call, scopeId, currentUserId) }

            else -> throw BadRequestException("UNKNOWN_MEMBERSHIP_TYPE") //TODO добавить типы ошибок для membership
        }
    }
}

interface MembershipController {
    suspend operator fun invoke()

    val call: ApplicationCall
    val scopeId: UUID
    val currentUserId: UUID
}