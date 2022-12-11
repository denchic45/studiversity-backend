package com.studiversity.feature.role

data class Role(val resource: String) {

    override fun toString(): String = resource

    companion object {
        val Guest: Role = Role("guest")
        val User: Role = Role("user")
        val Student: Role = Role("student")
        val Teacher: Role = Role("teacher")
        val Moderator: Role = Role("moderator")
        val Headman: Role = Role("headman")
        val Curator: Role = Role("curator")
    }
}