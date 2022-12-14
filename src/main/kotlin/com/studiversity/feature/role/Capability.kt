package com.studiversity.feature.role

data class Capability(val resource: String) {

    override fun toString(): String = resource

    companion object {
        val CreateUser: Capability = Capability("user:create")
        val ViewUserConfidentialData: Capability = Capability("user:view_confidential_data")

        val ViewGroup: Capability = Capability("group:view")
        val EnrollMembersInGroup: Capability = Capability("group:enroll")

        val CreateCourse: Capability = Capability("course:create")
        val ViewCourse: Capability = Capability("course:view")
    }
}