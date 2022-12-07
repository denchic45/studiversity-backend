package com.studiversity.feature.scope

import com.studiversity.database.table.ScopeEntity
import com.studiversity.database.table.ScopeTypeEntity
import java.util.*

interface AddScopeRepoExt {
    fun addScope(scopeId: UUID, scopeTypeId: Long, parentScopeId: UUID) {
        val parentScope = ScopeEntity.findById(parentScopeId)!!
        val parentType = parentScope.type
        val childScopeType = ScopeTypeEntity.findById(scopeTypeId)!!
        if (childScopeType.parentId.value != parentType.id.value)
            throw IllegalArgumentException(
                "type of passed parentScopeId $parentScope is not parent type of passed scopeTypeId $scopeTypeId"
            )
        ScopeEntity.new(scopeId) {
            type = ScopeTypeEntity.findById(scopeTypeId)!!
            path = listOf(scopeId.toString()) + parentScope.path
        }
    }
}