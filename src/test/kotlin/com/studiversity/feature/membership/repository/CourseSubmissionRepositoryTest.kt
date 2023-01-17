package com.studiversity.feature.membership.repository

import com.studiversity.database.DatabaseFactory
import com.studiversity.di.coroutineModule
import com.studiversity.di.supabaseClientModule
import com.studiversity.feature.course.work.submission.CourseSubmissionRepository
import com.studiversity.feature.course.work.submission.courseSubmissionModule
import com.studiversity.feature.role.Role
import com.studiversity.util.toUUID
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.test.KoinTest
import org.koin.test.inject

class CourseSubmissionRepositoryTest : KoinTest {

    private val courseSubmissionRepository: CourseSubmissionRepository by inject()
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
                    courseSubmissionModule
                )
            }
        }
    }

    @Test
    fun testFindSubmissionsByWorkId(): Unit = transaction {
        courseSubmissionRepository.findByWorkId(
            "8f6ba645-ed8e-4f08-8a2e-103d6b3883ae".toUUID(),
            "bc424b87-a484-459e-99ea-29efa7115fbe".toUUID(),
            userMembershipRepository.findMemberIdsByScopeAndRole(
                "8f6ba645-ed8e-4f08-8a2e-103d6b3883ae".toUUID(),
                Role.Student.id
            )
        ).apply {
            print(this)
        }
    }
}