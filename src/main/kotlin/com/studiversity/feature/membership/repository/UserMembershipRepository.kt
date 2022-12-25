package com.studiversity.feature.membership.repository

import com.studiversity.database.exists
import com.studiversity.database.table.Memberships
import com.studiversity.database.table.UsersMemberships
import com.studiversity.feature.membership.model.Member
import com.studiversity.logger.logger
import com.studiversity.util.toUUID
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class UserMembershipRepository(private val realtime: Realtime) {


    fun addMember(memberId: UUID, membershipId: UUID) = transaction {
        UsersMemberships.insert {
            it[UsersMemberships.memberId] = memberId
            it[UsersMemberships.membershipId] = membershipId
        }
    }

    fun removeMember(memberId: UUID, membershipId: UUID) = transaction {
        UsersMemberships.deleteWhere {
            UsersMemberships.memberId eq memberId and (UsersMemberships.membershipId eq membershipId)
        }
    }

    fun findMembersByScope(scopeId: UUID) = flowOf(
        transaction {
            getMembershipsByScope(scopeId).flatMap { membershipRow ->
                UsersMemberships.select(UsersMemberships.membershipId eq membershipRow[Memberships.id])
                    .map { userMembershipRow ->
                        Member(
                            id = userMembershipRow[UsersMemberships.memberId].value,
                            membershipId = membershipRow[Memberships.id].value
                        )
                    }
            }
        }
    )

    /**
     * Find members who are exist in one of the membership sources but not exist in target membership.
     *
     * Skips members who are in the target membership and in one of the membership sources.
     *
     * Usage example: find members of group who are still not enrolled in the course
     * @param membershipIdsSources membership sources where all desired members
     * @param membershipIdTarget membership target where can be unrelated members
     * @return user ids who exist in one of the membership sources but not exist in membership target
     */
    fun findUnrelatedMembersByManyToOneMemberships(
        membershipIdsSources: List<UUID>,
        membershipIdTarget: UUID
    ) = transaction {
        val sourceUm = UsersMemberships.alias("sourceUm")
        val targetUm = UsersMemberships.alias("targetUm")
        sourceUm.leftJoin(
            otherTable = targetUm,
            onColumn = { sourceUm[UsersMemberships.memberId] },
            otherColumn = { targetUm[UsersMemberships.memberId] },
            additionalConstraint = { targetUm[UsersMemberships.membershipId] eq membershipIdTarget }
        )
            .slice(sourceUm[UsersMemberships.memberId])
            .select(
                targetUm[UsersMemberships.membershipId].isNull()
                        and (sourceUm[UsersMemberships.membershipId] inList membershipIdsSources)
            ).map { it[sourceUm[UsersMemberships.memberId]].value }
    }

    /**
     * Find and add members to target membership who are exist in one of the membership sources but not exist in membership target.
     *
     * Finding members using [findUnrelatedMembersByManyToOneMemberships]
     *
     * Usage example: find members of group who are still not enrolled in the course and enroll them
     * @param membershipIdsSources membership sources where all desired members
     * @param membershipIdTarget membership target where can be unrelated members
     */
    fun findAndAddUnrelatedMembersByManyToOneMemberships(
        membershipIdsSources: List<UUID>,
        membershipIdTarget: UUID
    ) = transaction {
        findUnrelatedMembersByManyToOneMemberships(membershipIdsSources, membershipIdTarget)
            .apply {
                if (isNotEmpty())
                    addMembersToMembership(memberIds = this, membershipId = membershipIdTarget)
            }
    }

    /**
     * Find members who are exist in membership source but not exist in one of the membership targets.
     *
     * Skips members who are in the sources membership and in one of the membership targets
     *
     * Usage example: find course members enrolled by group who already removed in that group
     * @param membershipIdSource membership source where all desired members
     * @param membershipIdsTargets membership targets where can be unrelated members
     * @return user ids who exist membership in source but not exist in one of the membership targets
     */
    fun findUnrelatedMembersByOneToManyMemberships(
        membershipIdSource: UUID,
        membershipIdsTargets: List<UUID>
    ) = transaction {
        val sourceUm = UsersMemberships.alias("sourceUm")
        val targetUm = UsersMemberships.alias("targetUm")
        sourceUm.leftJoin(
            otherTable = targetUm,
            onColumn = { sourceUm[UsersMemberships.memberId] },
            otherColumn = { targetUm[UsersMemberships.memberId] },
            additionalConstraint = { targetUm[UsersMemberships.membershipId] inList membershipIdsTargets }
        )
            .slice(sourceUm[UsersMemberships.memberId])
            .select(
                targetUm[UsersMemberships.membershipId].isNull()
                        and (sourceUm[UsersMemberships.membershipId] eq membershipIdSource)
            ).map { it[sourceUm[UsersMemberships.memberId]].value }
    }

    /**
     * Find and remove members from source membership who are exist in membership source but not exist in one of the membership targets.
     *
     * Finding members using [findUnrelatedMembersByOneToManyMemberships]
     *
     * Usage example: find course members enrolled by group who already removed in that group and remove them from a course
     * @param membershipIdSource membership source where all desired members
     * @param membershipIdsTargets membership targets where can be unrelated members
     */
    fun findAndRemoveUnrelatedMembersByOneToManyMemberships(
        membershipIdSource: UUID,
        membershipIdsTargets: List<UUID>
    ) = transaction {
        findUnrelatedMembersByOneToManyMemberships(membershipIdSource, membershipIdsTargets)
            .apply {
                if (isNotEmpty())
                    removeMembersFromMembership(memberIds = this, membershipId = membershipIdSource)
            }
    }

    fun addMembersToMembership(memberIds: List<UUID>, membershipId: UUID) = transaction {
        UsersMemberships.batchInsert(memberIds) {
            this[UsersMemberships.memberId] = it
            this[UsersMemberships.membershipId] = membershipId
        }
    }

    fun removeMembersFromMembership(memberIds: List<UUID>, membershipId: UUID) = transaction {
        UsersMemberships.deleteWhere {
            memberId inList memberIds and (UsersMemberships.membershipId eq membershipId)
        }
    }

//    fun findMembersByScopeAndGreaterJoinTimestamp(scopeId: UUID, joinTimestamp: Instant) = flowOf(
//        transaction {
//            getMembershipsByScope(scopeId).flatMap { membershipRow ->
//                UsersMemberships.select(
//                    UsersMemberships.membershipId eq membershipRow[Memberships.id]
//                            and (UsersMemberships.joinAt greaterEq joinTimestamp)
//                ).map { userMembershipRow ->
//                    Member(
//                        id = userMembershipRow[UsersMemberships.memberId].value,
//                        membershipId = membershipRow[Memberships.id].value
//                    )
//                }
//            }
//        }
//    )

//    fun findMembersByScopeAndGreaterLeaveTimestamp(scopeId: UUID, leaveTimestamp: Instant?) = flowOf(
//        transaction {
//            getMembershipsByScope(scopeId).flatMap { membershipRow ->
//                UsersMemberships.select(
//                    UsersMemberships.membershipId eq membershipRow[Memberships.id]
//                            and (
//                            if (leaveTimestamp != null)
//                                UsersMemberships.leaveTimestamp greaterEq leaveTimestamp
//                            else UsersMemberships.leaveTimestamp.isNotNull()
//                            )
//                ).map { userMembershipRow ->
//                    Member(
//                        id = userMembershipRow[UsersMemberships.userId].value,
//                        membershipId = membershipRow[Memberships.id].value
//                    )
//                }
//            }
//        }
//    )

    private fun getMembershipsByScope(scopeId: UUID): Query {
        return Memberships.select(Memberships.scopeId eq scopeId)
    }

    private fun getJoinTimestampOfLastMemberByScope(scopeId: UUID) = transaction {
        getMembershipsByScope(scopeId).mapNotNull { membershipRow ->
            getMembersByMembershipAndMaxJoinTimestamp(membershipRow)
        }.maxByOrNull { it }
    }

    fun observeAddedMembersByMembershipId(membershipId: UUID): Flow<UUID> = flow {
        logger.info { "observing added members by membership = $membershipId" }
        val channel = realtime.createChannel("#membership_insert.$membershipId")
        val membershipsByScopeIdFlow: Flow<PostgresAction.Insert> = channel.postgresChangeFlow(schema = "public") {
            table = "user_membership"
            filter = "membership_id=eq.${membershipId}"
        }
        channel.join()
        emitAll(membershipsByScopeIdFlow.map {
            it.record.getValue("member_id").jsonPrimitive.content.toUUID()
        })
    }

    fun observeRemovedMembersByMembershipId(membershipId: UUID): Flow<UUID> = flow {
        logger.info { "observing removed members by membership = $membershipId" }
        val channel = realtime.createChannel("#membership_delete.$membershipId")
        val membershipsByScopeIdFlow: Flow<PostgresAction.Delete> = channel.postgresChangeFlow(schema = "public") {
            table = "user_membership"
//            filter = "membership_id=eq.${membershipId}" so far this does not work in the database
        }
        channel.join()
        emitAll(membershipsByScopeIdFlow.map {
            it.oldRecord.getValue("member_id").jsonPrimitive.content.toUUID()
        })
    }

    private fun getMembersByMembershipAndMaxJoinTimestamp(membershipRow: ResultRow): Instant? {
        return UsersMemberships
            .slice(UsersMemberships.id, UsersMemberships.membershipId, UsersMemberships.joinAt.max())
            .select(UsersMemberships.membershipId eq membershipRow[Memberships.id])
            .singleOrNull()?.get(UsersMemberships.joinAt)
    }

//    fun findLeaveTimestampOfLastMemberByScope(scopeId: UUID) = flowOf(
//        transaction {
//            getMembershipsByScope(scopeId).mapNotNull { membershipRow ->
//                getMembersByMembershipAndMaxLeaveTimestamp(membershipRow)
//            }.maxByOrNull { it }
//        }
//    )

//    private fun getMembersByMembershipAndMaxLeaveTimestamp(membershipRow: ResultRow): Instant? {
//        return UsersMemberships
//            .slice(UsersMemberships.id, UsersMemberships.membershipId, UsersMemberships.leaveTimestamp.max())
//            .select(UsersMemberships.membershipId eq membershipRow[Memberships.id])
//            .singleOrNull()?.get(UsersMemberships.leaveTimestamp)
//    }

    fun existMember(memberId: UUID, membershipId: UUID) = transaction {
        UsersMemberships.exists { UsersMemberships.memberId eq memberId and (UsersMemberships.membershipId eq membershipId) }
    }

    fun existMemberByScopeIds(memberId: UUID, scopeIds: List<UUID>) = transaction {
        UsersMemberships.innerJoin(
            otherTable = Memberships,
            onColumn = { Memberships.id },
            otherColumn = { UsersMemberships.membershipId }
        ).slice(UsersMemberships.memberId)
            .select(
                Memberships.scopeId inList scopeIds and (UsersMemberships.memberId eq memberId)
            ).count() > 0
    }

    fun existMemberByOneOfScopeIds(memberId: UUID, scopeIds: List<UUID>) = transaction {
        UsersMemberships.innerJoin(
            otherTable = Memberships,
            onColumn = { Memberships.id },
            otherColumn = { UsersMemberships.membershipId }
        ).slice(UsersMemberships.memberId)
            .select(
                Memberships.scopeId inList scopeIds and (UsersMemberships.memberId eq memberId)
            ).count() > 0
    }

    suspend fun listenTestRealtime() {
//        val channel = realtime.createChannel("#test")
//        val membershipsByScopeIdFlow: Flow<PostgresAction.Insert> = channel.postgresChangeFlow(schema = "public") {
//            table = "user_membership"
//            filter = "membership_id=eq.${"aabbae18-438f-4ea4-be78-d8b051167619"}"
//        }
//        channel.join()
//        membershipsByScopeIdFlow.collect { println("-- It's works!!! add user: $it") }
    }
}