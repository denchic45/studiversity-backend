package com.studiversity.feature.role

import org.jetbrains.exposed.sql.Database
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.*
import kotlin.test.Test
import kotlin.test.assertTrue

class RoleRepositoryTest : KoinTest {

    val roleRepository: RoleRepository by inject()

    init {
        startKoin {
            modules(
                module {
                    single { RoleRepository() }
                })
        }
    }

    @Test
    fun test() {

        Database.connect(
            url = "jdbc:postgresql://db.twmjqqkhwizjfmbebbxj.supabase.co:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "4G4x#!nKhwexYgM"
        )

        assertTrue(
            roleRepository.isExistUserRoleByScopeOrParentScopes(
                userId = UUID.fromString("43c3c223-e8f7-4b84-99d6-7e67c2832b27"),
                roleId = 5,
                scopeId = UUID.fromString("43c3c223-e8f7-4b84-99d6-7e67c2832b27")
            )
        )

    }
}