package com.studiversity.feature.membership.repository

import com.studiversity.database.table.ExternalStudyGroupsMemberships
import com.studiversity.database.table.Memberships
import com.studiversity.logger.logger
import com.studiversity.util.toUUID
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class MembershipRepository(private val realtime: Realtime, private val coroutineScope: CoroutineScope) {

    private val membershipChannel: RealtimeChannel = realtime.createChannel("#membership1")
    private val insertExternalStudyGroupMembershipsFlow =
        membershipChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "membership"
            filter = "type=eq.by_group"
        }.shareIn(coroutineScope, SharingStarted.Lazily)
    private val deleteExternalStudyGroupMembershipsFlow =
        membershipChannel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
            table = "membership"
//            filter = "type=eq.by_group"
        }.filter { it.oldRecord.getValue("type").jsonPrimitive.content == "by_group" }
            .shareIn(coroutineScope, SharingStarted.Lazily)

    init {
        coroutineScope.launch {
            membershipChannel.join()
        }
    }

    fun addMembership() = transaction {
        //TODO
    }

    private suspend fun listenMembershipsByScopeIds(scopeIds: List<UUID>): Flow<PostgresAction> {
        println("--Listening updates in memberships by scope_ids: $scopeIds")
        return scopeIds.map { scopeId ->
            val channel = realtime.createChannel("#studygroup_members")
            val membershipsByScopeIdFlow: Flow<PostgresAction> = channel.postgresChangeFlow(schema = "public") {
                table = "membership"
                filter = "scope_id=eq.${scopeId}"
            }
            channel.join()
            membershipsByScopeIdFlow
        }.merge().distinctUntilChanged()
    }

    fun observeMembershipsByScopeIds(scopeIds: List<UUID>) = flow {
        emit(getMembershipsByScopes(scopeIds))
        emitAll(
            listenMembershipsByScopeIds(scopeIds)
                .map { getMembershipsByScopes(scopeIds) }
        )
    }

    private fun getMembershipsByScopes(scopeIds: List<UUID>) = transaction {
        Memberships.select(Memberships.scopeId inList scopeIds)
            .map { it[Memberships.id].value }
    }

    fun findIdsByType(type: String): List<UUID> = transaction {
        Memberships.slice(Memberships.id).select(Memberships.type eq type).map { it[Memberships.id].value }
    }

    // TODO ВОЗМОЖНО ПОЛЕ SCOPE_ID НЕ НУЖНО И ДОСТАТОЧНО ПРОСЛУШИВАТЬ ПОЛЕ MEMBERSHIP_ID
    fun observeStudyGroupIdsOfExternalMembershipsByMembershipId(membershipId: UUID): StateFlow<List<UUID>> {
        val stateFlow = MutableStateFlow(transaction {
            ExternalStudyGroupsMemberships.select(ExternalStudyGroupsMemberships.membershipId eq membershipId)
                .map { it[ExternalStudyGroupsMemberships.studyGroupId].value }
        })
        val channel = realtime.createChannel("external-study-group-by-$membershipId")
        val insertedStudyGroupExternalMembershipFlow = channel.postgresChangeFlow<PostgresAction.Insert>("public") {
            table = "external_study_group_membership"
            filter = "membership_id=eq.$membershipId"
        }.map { it.record.getValue("membership_id").jsonPrimitive.content.toUUID() }
        val deletedStudyGroupExternalMembershipFlow = channel.postgresChangeFlow<PostgresAction.Delete>("public") {
            table = "external_study_group_membership"
        }.map { it.oldRecord.getValue("membership_id").jsonPrimitive.content.toUUID() }
            .filter { it == membershipId }

        coroutineScope.launch {
            channel.join()
            launch {
                insertedStudyGroupExternalMembershipFlow.collect { addedMembershipId ->
                    logger.info { "added group to course; membershipId: $addedMembershipId" }
                    stateFlow.update { it + addedMembershipId }
                }
            }
            launch {
                deletedStudyGroupExternalMembershipFlow.collect { removedMembershipId ->
                    logger.info { "removed group from course; membershipId: $removedMembershipId" }
                    stateFlow.update { it - removedMembershipId }
                }
            }
        }
        return stateFlow
    }

    fun findScopeIdByMembershipId(membershipId: UUID): UUID = transaction {
        Memberships.slice(Memberships.scopeId).select(Memberships.id eq membershipId).single()[Memberships.scopeId]
    }

    fun observeAddExternalStudyGroupMemberships(): Flow<UUID> {
        return insertExternalStudyGroupMembershipsFlow.map {
            it.record.getValue("membership_id").jsonPrimitive.content.toUUID()
        }
    }

    fun observeRemoveExternalStudyGroupMemberships(): Flow<UUID> {
        return deleteExternalStudyGroupMembershipsFlow.map {
            it.oldRecord.getValue("membership_id").jsonPrimitive.content.toUUID()
        }
    }
}