package com.studiversity.feature.role

data class Capability(val resource: String) {

    override fun toString(): String = resource

    companion object {
        val ReadUserConfidentialData: Capability = Capability("user/confidential:read")
        val WriteUser: Capability = Capability("user:write")
        val DeleteUser: Capability = Capability("user:delete")

        val WriteAssignRoles: Capability = Capability("role/assignment:write")

        val ReadMembers: Capability = Capability("membership/members:read")
        val WriteMembers: Capability = Capability("membership/members:write")

        val WriteMembership: Capability = Capability("membership:write")

        val ReadStudyGroup: Capability = Capability("study_group:read")
        val WriteStudyGroup: Capability = Capability("study_group:write")
        val DeleteStudyGroup: Capability = Capability("study_group:delete")

        val ReadCourse: Capability = Capability("course:read")
        val WriteCourse: Capability = Capability("course:write")
        val DeleteCourse: Capability = Capability("course:delete")
        val WriteCourseStudyGroups: Capability = Capability("course/study_group:write")

        val ReadCourseElements: Capability = Capability("course/elements:read")
        val DeleteCourseElements: Capability = Capability("course/elements:delete")
        val WriteCourseAssignment: Capability = Capability("course/assignment:write")
        val WriteCoursePost: Capability = Capability("course/post:write")

        val ReadSubmissions: Capability = Capability("course/submissions:read")
        val WriteSubmissions: Capability = Capability("course/submissions:write")
        val SubmitSubmission: Capability = Capability("course/submission:submit")

        val WriteSubject: Capability = Capability("subject:write")
        val ReadSubject: Capability = Capability("subject:read")
        val DeleteSubject: Capability = Capability("subject:delete")
    }
}