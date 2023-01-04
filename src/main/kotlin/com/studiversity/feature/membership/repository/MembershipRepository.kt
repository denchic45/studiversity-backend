package com.studiversity.feature.membership.repository

import com.studiversity.database.table.ExternalStudyGroupsMemberships
import com.studiversity.database.table.Memberships
import com.studiversity.feature.membership.model.CreateMembershipRequest
import com.studiversity.logger.logger
import com.studiversity.util.toUUID
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class MembershipRepository(private val realtime: Realtime, private val coroutineScope: CoroutineScope) {

    private val membershipChannel: RealtimeChannel = realtime.createChannel("#membership")
    private val insertExternalStudyGroupMembershipFlow =
        membershipChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "membership"
            filter = "type=eq.by_group"
        }.shareIn(coroutineScope, SharingStarted.Lazily)
    private val deleteExternalStudyGroupMembershipsFlow =
        membershipChannel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
            table = "membership"
        }.filter { it.oldRecord.getValue("type").jsonPrimitive.content == "by_group" }
            .shareIn(coroutineScope, SharingStarted.Lazily)

    init {
        coroutineScope.launch {
            membershipChannel.join()
        }
    }

    fun addManualMembership(createMembershipRequest: CreateMembershipRequest) {
        Memberships.insert {
            it[scopeId] = createMembershipRequest.scopeId
            it[active] = true
            it[type] = createMembershipRequest.type
        }
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
        }.map { it.record.getValue("study_group_id").jsonPrimitive.content.toUUID() }

        val deletedStudyGroupExternalMembershipFlow = channel.postgresChangeFlow<PostgresAction.Delete>("public") {
            table = "external_study_group_membership"
        }.filter {
            it.oldRecord.getValue("membership_id").jsonPrimitive.content.toUUID() == membershipId
        }
            .map { it.oldRecord.getValue("study_group_id").jsonPrimitive.content.toUUID() }

        coroutineScope.launch {
            channel.join()
            launch {
                insertedStudyGroupExternalMembershipFlow.collect { addedStudyGroupId ->
                    logger.info { "added group to course; studyGroupId: $addedStudyGroupId" }
                    stateFlow.update { it + addedStudyGroupId }
                }
            }
            launch {
                deletedStudyGroupExternalMembershipFlow.collect { removedStudyGroupId ->
                    logger.info { "removed group from course; studyGroupId: $removedStudyGroupId" }
                    stateFlow.update { it - removedStudyGroupId }
                }
            }
        }
        return stateFlow
    }

    fun findScopeIdByMembershipId(membershipId: UUID): UUID = transaction {
        Memberships.slice(Memberships.scopeId).select(Memberships.id eq membershipId).single()[Memberships.scopeId]
    }

    fun observeOnFirstAddExternalStudyGroupMembershipInMembership(): Flow<UUID> {
        return insertExternalStudyGroupMembershipFlow.map {
            // Check if attached study group is first, else ignore her
            it.record.getValue("membership_id").jsonPrimitive.content.toUUID()
        }
    }

    fun observeRemoveExternalStudyGroupMemberships(): Flow<UUID> {
        return deleteExternalStudyGroupMembershipsFlow.map {
            // Check that there are no more attached groups left, else we ignore the deletion
            it.oldRecord.getValue("membership_id").jsonPrimitive.content.toUUID()
        }
    }

    fun findMembershipIdByTypeAndScopeId(type: String, scopeId: UUID): UUID {
        return Memberships.slice(Memberships.id)
            .select(Memberships.type eq type and (Memberships.scopeId eq scopeId))
            .first()[Memberships.id].value
    }
}