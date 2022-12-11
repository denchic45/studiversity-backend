package com.studiversity.feature.role

data class Capability(val resource: String) {

    override fun toString(): String = resource

    companion object {
        val CreateUser: Capability = Capability("user:create")

        val EnrollMembersInGroup: Capability = Capability("group:enroll")
    }
}