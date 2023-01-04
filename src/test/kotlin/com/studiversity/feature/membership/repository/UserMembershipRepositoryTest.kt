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
                        single { UserMembershipRepository(get(),get()) }
                    })
            }
        }
    }

    @Test
    fun testFindUnrelatedMembersOfTargetBySourceMemberships() {
        val membershipTarget = "6beae1e2-2392-4586-82fa-280353d4fdba".toUUID()
        val membershipSources = listOf("aabbae18-438f-4ea4-be78-d8b051167619").map(String::toUUID)

//        println("--- add users who are in the sources but not in the target")
//        userMembershipRepository.findAndAddUnrelatedMembersByManyToOneMemberships(membershipSources, membershipTarget)
//            .forEach {
//                println("added member UUID: $it")
//            }

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
}