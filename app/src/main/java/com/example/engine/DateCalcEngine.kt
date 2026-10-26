package com.example.engine

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

object DateCalcEngine {

    data class AgeResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val nextBirthdayInDays: Long,
        val dayOfWeekBorn: String
    )

    fun calculateAge(birthDate: LocalDate, referenceDate: LocalDate = LocalDate.now()): AgeResult? {
        if (birthDate.isAfter(referenceDate)) return null

        val period = Period.between(birthDate, referenceDate)
        val totalDays = ChronoUnit.DAYS.between(birthDate, referenceDate)

        // Calculate next birthday
        var nextBirthday = birthDate.withYear(referenceDate.year)
        if (nextBirthday.isBefore(referenceDate) || nextBirthday.isEqual(referenceDate)) {
            nextBirthday = birthDate.withYear(referenceDate.year + 1)
        }
        val daysUntilBirthday = ChronoUnit.DAYS.between(referenceDate, nextBirthday)

        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            nextBirthdayInDays = daysUntilBirthday,
            dayOfWeekBorn = birthDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        )
    }

    data class DateDiffResult(
        val totalDays: Long,
        val years: Int,
        val months: Int,
        val days: Int,
        val totalWeeks: Long,
        val remainingDays: Long,
        val isSameDay: Boolean
    )

    fun calculateDateDifference(startDate: LocalDate, endDate: LocalDate): DateDiffResult {
        val (first, second) = if (startDate.isAfter(endDate)) Pair(endDate, startDate) else Pair(startDate, endDate)
        val period = Period.between(first, second)
        val totalDays = ChronoUnit.DAYS.between(first, second)
        val totalWeeks = totalDays / 7
        val remainingDays = totalDays % 7

        return DateDiffResult(
            totalDays = totalDays,
            years = period.years,
            months = period.months,
            days = period.days,
            totalWeeks = totalWeeks,
            remainingDays = remainingDays,
            isSameDay = totalDays == 0L
        )
    }
}
