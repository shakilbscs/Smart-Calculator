package com.example.engine

import androidx.compose.ui.graphics.Color
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object HealthCalcEngine {

    const val MEDICAL_DISCLAIMER =
        "Important Medical Disclaimer: All health, BMI, fertility, and pregnancy calculations are general statistical estimates and educational references. They are not medical diagnoses, ultrasound assessments, or clinical recommendations. Biological variation, cycle irregularity, and individual health factors significantly alter timelines. Never use fertile window estimates for contraception or family planning. Always consult a licensed healthcare professional for clinical advice, pregnancy care, or diagnostic testing."

    enum class BmiCategory(val label: String, val color: Color, val advice: String) {
        UNDERWEIGHT("Underweight", Color(0xFF3B82F6), "Below standard adult BMI range (< 18.5)"),
        NORMAL("Normal Weight", Color(0xFF10B981), "Within standard adult BMI range (18.5 – 24.9)"),
        OVERWEIGHT("Overweight", Color(0xFFF59E0B), "Above standard adult BMI range (25.0 – 29.9)"),
        OBESE("Obesity", Color(0xFFEF4444), "Significantly above standard adult range (≥ 30.0)")
    }

    data class BmiResult(
        val bmi: BigDecimal,
        val category: BmiCategory,
        val healthyWeightMinKg: BigDecimal,
        val healthyWeightMaxKg: BigDecimal,
        val primeDifferenceKg: BigDecimal
    )

    fun calculateBmi(weightKgStr: String, heightCmStr: String): BmiResult? {
        val weight = weightKgStr.trim().toBigDecimalOrNull() ?: return null
        val heightCm = heightCmStr.trim().toBigDecimalOrNull() ?: return null

        if (weight <= BigDecimal.ZERO || heightCm <= BigDecimal.ZERO) return null

        return try {
            val heightM = heightCm.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
            val heightM2 = heightM.multiply(heightM)

            val bmi = weight.divide(heightM2, 2, RoundingMode.HALF_UP)

            val category = when {
                bmi < BigDecimal("18.5") -> BmiCategory.UNDERWEIGHT
                bmi <= BigDecimal("24.9") -> BmiCategory.NORMAL
                bmi <= BigDecimal("29.9") -> BmiCategory.OVERWEIGHT
                else -> BmiCategory.OBESE
            }

            val healthyMin = BigDecimal("18.5").multiply(heightM2).setScale(1, RoundingMode.HALF_UP)
            val healthyMax = BigDecimal("24.9").multiply(heightM2).setScale(1, RoundingMode.HALF_UP)

            val diff = when {
                weight < healthyMin -> weight.subtract(healthyMin).setScale(1, RoundingMode.HALF_UP)
                weight > healthyMax -> weight.subtract(healthyMax).setScale(1, RoundingMode.HALF_UP)
                else -> BigDecimal.ZERO
            }

            BmiResult(
                bmi = bmi,
                category = category,
                healthyWeightMinKg = healthyMin,
                healthyWeightMaxKg = healthyMax,
                primeDifferenceKg = diff
            )
        } catch (e: Exception) {
            null
        }
    }

    data class PregnancyResult(
        val estimatedDueDate: LocalDate,
        val gestationalWeeks: Int,
        val gestationalDays: Int,
        val trimester: Int,
        val daysRemaining: Long,
        val progressPercent: Int
    )

    fun calculatePregnancyDueDate(
        lmpDate: LocalDate,
        cycleLengthDays: Int = 28,
        referenceDate: LocalDate = LocalDate.now()
    ): PregnancyResult? {
        if (cycleLengthDays < 20 || cycleLengthDays > 45) return null

        // Naegele's rule with cycle adjustment
        val cycleOffset = cycleLengthDays - 28
        val dueDate = lmpDate.plusDays(280L + cycleOffset)

        val totalDaysElapsed = ChronoUnit.DAYS.between(lmpDate, referenceDate)
        if (totalDaysElapsed < 0) return null

        val weeks = (totalDaysElapsed / 7).toInt()
        val days = (totalDaysElapsed % 7).toInt()

        val trimester = when {
            weeks < 13 -> 1
            weeks < 27 -> 2
            else -> 3
        }

        val daysRemaining = ChronoUnit.DAYS.between(referenceDate, dueDate).coerceAtLeast(0)
        val progress = ((totalDaysElapsed.toDouble() / 280.0) * 100.0).toInt().coerceIn(0, 100)

        return PregnancyResult(
            estimatedDueDate = dueDate,
            gestationalWeeks = weeks,
            gestationalDays = days,
            trimester = trimester,
            daysRemaining = daysRemaining,
            progressPercent = progress
        )
    }

    data class FertileWindowResult(
        val estimatedOvulationDate: LocalDate,
        val fertileWindowStart: LocalDate,
        val fertileWindowEnd: LocalDate,
        val nextPeriodEstimate: LocalDate
    )

    fun calculateFertileWindow(
        lmpDate: LocalDate,
        cycleLengthDays: Int = 28
    ): FertileWindowResult? {
        if (cycleLengthDays < 20 || cycleLengthDays > 45) return null

        // Luteal phase standard is ~14 days before next period
        val ovulationDayOffset = cycleLengthDays - 14
        val estimatedOvulation = lmpDate.plusDays(ovulationDayOffset.toLong())
        val fertileStart = estimatedOvulation.minusDays(5)
        val fertileEnd = estimatedOvulation.plusDays(1)
        val nextPeriod = lmpDate.plusDays(cycleLengthDays.toLong())

        return FertileWindowResult(
            estimatedOvulationDate = estimatedOvulation,
            fertileWindowStart = fertileStart,
            fertileWindowEnd = fertileEnd,
            nextPeriodEstimate = nextPeriod
        )
    }
}
