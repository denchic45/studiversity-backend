package com.studiversity

import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*


class LocalDateTest {

    @Test
    fun testNow() {
        println("date: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_ww"))}")
    }

    @Test
    fun test() {
        val monday = LocalDate.parse(
            "2023_05",
            DateTimeFormatterBuilder()
                .appendPattern("YYYY_ww")
                .parseDefaulting(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.value.toLong())
                .toFormatter()
        )
        println("date: $monday")
    }

    @Test
    fun someTest() {
        val date = "2015-40"
        val formatter = DateTimeFormatterBuilder()
            .appendPattern("YYYY-ww")
            .parseDefaulting(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.value.toLong())
            .toFormatter(Locale.FRANCE)
        val ld = LocalDate.parse(date, formatter)

        println(ld)
    }
}