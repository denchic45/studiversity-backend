package com.studiversity.feature.membership.repository

import com.studiversity.database.DatabaseFactory
import com.studiversity.di.coroutineModule
import com.studiversity.di.supabaseClientModule
import com.studiversity.util.toUUID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

class UserMembershipRepositoryTest : KoinTest {

    private val userMembershipRepository: UserMembershipRepository by inject()

    companion object {
        @BeforeAll
        @JvmStatic
        fun start() {
            DatabaseFactory.database

            GlobalContext.startKoin {
                modules(
                    coroutineModule,
                    supabaseClientModule,
                    module {
                        single { UserMembershipRepository(get(), get()) }
                    })
            }
        }
    }

    @Test
    fun testFindUnrelatedMembersOfTargetBySourceMemberships() {
        val membershipTarget = "6beae1e2-2392-4586-82fa-280353d4fdba".toUUID()
        val membershipSources = listOf("aabbae18-438f-4ea4-be78-d8b051167619").map(String::toUUID)

        println("--- remove users who are in the target but not in the sources")
        userMembershipRepository.findAndRemoveUnrelatedMembersByOneToManyMemberships(
            membershipTarget,
            membershipSources
        )
            .forEach {
                println("removed member UUID: $it")
            }
    }


    @Test
    fun testFlow() {
        val rootFlow = flow {
            println("building root flow")
            delay(500)
            emit("Hello world!")
        }

        val childFlow1 = rootFlow.flatMapConcat { flow { emit(it.count()) } }

        val childFlow2 = rootFlow.map { it.repeat(5) }

        GlobalScope.launch {
            rootFlow.collect { println("collect root flow $it") }
        }

        GlobalScope.launch {
            childFlow1.collect {
                println("child flow 1 $it")
            }
        }

        GlobalScope.launch {
            childFlow2.collect {
                println("child flow 2 $it")
            }
        }
    }

    @Test
    fun testMissingStudentOfCourse(): Unit = runBlocking {
        userMembershipRepository.findMissingStudentsFromGroupsToCourse(
            courseMembershipId = "0b1bdae8-fe3b-45d8-91cc-663f11cefde9".toUUID(),
            groupIds = listOf("5bb2d369-1241-4079-bbe8-4649f26c1b83".toUUID())
        ).apply {
            print(this)
        }
    }

    @Test
    fun testRemainingStudentsOfCourse(): Unit = runBlocking {
        userMembershipRepository.findRemainingStudentsOfCourseFromGroups(
            groupIds = listOf("5bb2d369-1241-4079-bbe8-4649f26c1b83".toUUID()),
            courseMembershipId = "0b1bdae8-fe3b-45d8-91cc-663f11cefde9".toUUID()
        ).apply {
            print(this)
        }
    }

    @Test
    fun testFindUsersWhoHasRolesAndNotExistInAnyMembershipByScopeId(): Unit = transaction {
        userMembershipRepository.findUsersWhoHasRolesAndNotExistInAnyMembershipByScopeId(
            userIds = listOf(
                "02f00b3e-3a78-4431-87d4-34128ebbb04c".toUUID(),
                "77129e28-bf01-4dca-b19f-9fbcf576345e".toUUID(),
                "7a98cdcf-d404-4556-96bd-4ce9137c8cbe".toUUID()
            ),
            scopeId = "8f6ba645-ed8e-4f08-8a2e-103d6b3883ae".toUUID()
        ).apply {
                print(this)
            }
    }

    @Test
    fun testMembersInScope():Unit = transaction {
        userMembershipRepository.findMembersByScope("8f6ba645-ed8e-4f08-8a2e-103d6b3883ae".toUUID())
            .apply {
                print(this)
            }
    }
}