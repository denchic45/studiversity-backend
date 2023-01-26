package com.studiversity.feature.auth

import org.junit.jupiter.api.Test

class PasswordGeneratorTest {

    @Test
    fun testPassword() {
        val password = PasswordGenerator().generate()
        println("Password: $password")
    }
}