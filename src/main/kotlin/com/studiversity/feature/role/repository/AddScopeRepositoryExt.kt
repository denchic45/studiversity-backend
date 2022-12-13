package com.studiversity.feature.role.repository

import com.studiversity.database.table.ScopeDao
import com.studiversity.database.table.ScopeTypeDao
import java.util.*

interface AddScopeRepoExt {
    fun addScope(scopeId: UUID, scopeTypeId: Long, parentScopeId: UUID) {
        val parentScope = ScopeDao.findById(parentScopeId)!!
        val parentType = parentScope.type
        val childScopeType = ScopeTypeDao.findById(scopeTypeId)!!
        if (childScopeType.parentId.value != parentType.id.value)
            throw IllegalArgumentException(
                "type of passed parentScopeId $parentScope is not parent type of passed scopeTypeId $scopeTypeId"
            )
        ScopeDao.new(scopeId) {
            type = ScopeTypeDao.findById(scopeTypeId)!!
            path = listOf(scopeId) + parentScope.path
        }
    }
}