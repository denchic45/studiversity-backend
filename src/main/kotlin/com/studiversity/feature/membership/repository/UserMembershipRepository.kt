package com.studiversity.feature.membership.repository

import com.studiversity.database.table.Memberships
import com.studiversity.database.table.UsersMemberships
import com.studiversity.feature.membership.model.Member
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class UserMembershipRepository {


    fun addMember(memberId: UUID, membershipId: UUID) {

    }

    fun findMembersByScope(scopeId: UUID) = flowOf(
        transaction {
            getMembershipsByScope(scopeId).flatMap { membershipRow ->
                UsersMemberships.select(UsersMemberships.membershipId eq membershipRow[Memberships.id])
                    .map { userMembershipRow ->
                        Member(
                            id = userMembershipRow[UsersMemberships.userId].value,
                            membershipId = membershipRow[Memberships.id].value
                        )
                    }
            }
        }
    )

    fun findMembersByScopeAndGreaterJoinTimestamp(scopeId: UUID, joinTimestamp: Instant) = flowOf(
        transaction {
            getMembershipsByScope(scopeId).flatMap { membershipRow ->
                UsersMemberships.select(
                    UsersMemberships.membershipId eq membershipRow[Memberships.id]
                            and (UsersMemberships.joinTimestamp greaterEq joinTimestamp)
                ).map { userMembershipRow ->
                    Member(
                        id = userMembershipRow[UsersMemberships.userId].value,
                        membershipId = membershipRow[Memberships.id].value
                    )
                }
            }
        }
    )

    fun findMembersByScopeAndGreaterLeaveTimestamp(scopeId: UUID, leaveTimestamp: Instant?) = flowOf(
        transaction {
            getMembershipsByScope(scopeId).flatMap { membershipRow ->
                UsersMemberships.select(
                    UsersMemberships.membershipId eq membershipRow[Memberships.id]
                            and (
                            if (leaveTimestamp != null)
                                UsersMemberships.leaveTimestamp greaterEq leaveTimestamp
                            else UsersMemberships.leaveTimestamp.isNotNull()
                            )
                ).map { userMembershipRow ->
                    Member(
                        id = userMembershipRow[UsersMemberships.userId].value,
                        membershipId = membershipRow[Memberships.id].value
                    )
                }
            }
        }
    )

    private fun getMembershipsByScope(scopeId: UUID): Query {
        return Memberships.select(Memberships.scopeId eq scopeId)
    }

    fun findJoinTimestampOfLastMemberByScope(scopeId: UUID) = flowOf(
        transaction {
            getMembershipsByScope(scopeId).mapNotNull { membershipRow ->
                getMembersByMembershipAndMaxJoinTimestamp(membershipRow)
            }.maxByOrNull { it }
        }
    )

    private fun getMembersByMembershipAndMaxJoinTimestamp(membershipRow: ResultRow): Instant? {
        return UsersMemberships
            .slice(UsersMemberships.id, UsersMemberships.membershipId, UsersMemberships.joinTimestamp.max())
            .select(UsersMemberships.membershipId eq membershipRow[Memberships.id])
            .singleOrNull()?.get(UsersMemberships.joinTimestamp)

    }

    fun findLeaveTimestampOfLastMemberByScope(scopeId: UUID) = flowOf(
        transaction {
            getMembershipsByScope(scopeId).mapNotNull { membershipRow ->
                getMembersByMembershipAndMaxLeaveTimestamp(membershipRow)
            }.maxByOrNull { it }
        }
    )

    private fun getMembersByMembershipAndMaxLeaveTimestamp(membershipRow: ResultRow): Instant? {
        return UsersMemberships
            .slice(UsersMemberships.id, UsersMemberships.membershipId, UsersMemberships.leaveTimestamp.max())
            .select(UsersMemberships.membershipId eq membershipRow[Memberships.id])
            .singleOrNull()?.get(UsersMemberships.leaveTimestamp)

    }
}