package com.studiversity.feature.role

import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoleRepositoryTest : KoinTest {

    private val roleRepository: RoleRepository by inject()

    companion object {
        @BeforeAll
        @JvmStatic
        fun start() {
            Database.connect(
                url = "jdbc:postgresql://db.twmjqqkhwizjfmbebbxj.supabase.co:5432/postgres",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "4G4x#!nKhwexYgM"
            )

            startKoin {
                modules(
                    module {
                        single { RoleRepository() }
                    })
            }
        }
    }

    @Test
    fun testHasRole() {
        assertTrue(
            roleRepository.hasRole(
                userId = UUID.fromString("43c3c223-e8f7-4b84-99d6-7e67c2832b27"),
                roleId = 5,
                scopeId = UUID.fromString("43c3c223-e8f7-4b84-99d6-7e67c2832b27")
            )
        )
    }

    @Test
    fun testHasCapability() {
        assertTrue(
            roleRepository.hasCapability(
                userId = UUID.fromString("43c3c223-e8f7-4b84-99d6-7e67c2832b27"),
                capabilityResource = "user:view_confidential_data",
                scopeId = UUID.fromString("43c3c223-e8f7-4b84-99d6-7e67c2832b27")
            )
        )

        assertFalse(
            roleRepository.hasCapability(
                userId = UUID.fromString("02f00b3e-3a78-4431-87d4-34128ebbb04c"),
                capabilityResource = "user:view_confidential_data",
                scopeId = UUID.fromString("43c3c223-e8f7-4b84-99d6-7e67c2832b27")
            )
        )
    }
}