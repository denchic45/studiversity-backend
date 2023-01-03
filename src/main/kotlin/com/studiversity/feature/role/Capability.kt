package com.studiversity.feature.role

data class Capability(val resource: String) {

    override fun toString(): String = resource

    companion object {
        val WriteUser: Capability = Capability("user:write")
        val DeleteUser: Capability = Capability("user:delete")
        val ReadUserConfidentialData: Capability = Capability("user/confidential:read")

        val WriteAssignRoles: Capability = Capability("role/assignment:write")

        val WriteGroup: Capability = Capability("group:write")
        val ReadGroup: Capability = Capability("group:read")
        val DeleteGroup: Capability = Capability("group:delete")
        val WriteGroupMembers: Capability = Capability("group/members:write")

        val WriteCourses: Capability = Capability("course:write")
        val ReadCourse: Capability = Capability("course:read")
        val DeleteCourse: Capability = Capability("course:delete")
        val WriteCourseMembers: Capability = Capability("course/members:write")
        val ReadCourseMembers: Capability = Capability("course/members:read")

        val WriteSubject: Capability = Capability("subject:write")
        val ReadSubject: Capability = Capability("subject:read")
        val DeleteSubject: Capability = Capability("subject:delete")
    }
}