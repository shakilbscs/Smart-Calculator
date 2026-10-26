package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

object FinanceCalcEngine {

    data class ProfitMarginResult(
        val profitAmount: BigDecimal,
        val marginPercent: BigDecimal,
        val markupPercent: BigDecimal,
        val isProfitable: Boolean
    )

    fun calculateProfitMargin(
        costStr: String,
        sellingPriceStr: String
    ): ProfitMarginResult? {
        val cost = costStr.trim().toBigDecimalOrNull() ?: return null
        val selling = sellingPriceStr.trim().toBigDecimalOrNull() ?: return null
        if (cost < BigDecimal.ZERO || selling < BigDecimal.ZERO) return null

        return try {
            val profit = selling.subtract(cost).setScale(2, RoundingMode.HALF_UP)
            val margin = if (selling.compareTo(BigDecimal.ZERO) > 0) {
                profit.multiply(BigDecimal(100)).divide(selling, 2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            val markup = if (cost.compareTo(BigDecimal.ZERO) > 0) {
                profit.multiply(BigDecimal(100)).divide(cost, 2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            ProfitMarginResult(
                profitAmount = profit,
                marginPercent = margin,
                markupPercent = markup,
                isProfitable = profit >= BigDecimal.ZERO
            )
        } catch (e: Exception) {
            null
        }
    }

    data class LoanResult(
        val monthlyPayment: BigDecimal,
        val totalPayment: BigDecimal,
        val totalInterest: BigDecimal,
        val totalMonths: Int
    )

    fun calculateLoan(
        amountStr: String,
        annualRatePercentStr: String,
        termYearsStr: String
    ): LoanResult? {
        val principal = amountStr.trim().toBigDecimalOrNull() ?: return null
        val annualRate = annualRatePercentStr.trim().toBigDecimalOrNull() ?: return null
        val termYears = termYearsStr.trim().toIntOrNull() ?: return null

        if (principal <= BigDecimal.ZERO || annualRate < BigDecimal.ZERO || termYears <= 0) return null

        return try {
            val totalMonths = termYears * 12
            if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
                val monthly = principal.divide(BigDecimal(totalMonths), 2, RoundingMode.HALF_UP)
                LoanResult(
                    monthlyPayment = monthly,
                    totalPayment = principal.setScale(2, RoundingMode.HALF_UP),
                    totalInterest = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    totalMonths = totalMonths
                )
            } else {
                val r = annualRate.toDouble() / (12.0 * 100.0)
                val n = totalMonths.toDouble()
                val p = principal.toDouble()

                val numerator = p * r * (1.0 + r).pow(n)
                val denominator = (1.0 + r).pow(n) - 1.0
                val monthlyPaymentDouble = numerator / denominator

                val monthlyPaymentBD = BigDecimal(monthlyPaymentDouble).setScale(2, RoundingMode.HALF_UP)
                val totalPaymentBD = monthlyPaymentBD.multiply(BigDecimal(totalMonths)).setScale(2, RoundingMode.HALF_UP)
                val totalInterestBD = totalPaymentBD.subtract(principal).setScale(2, RoundingMode.HALF_UP)

                LoanResult(
                    monthlyPayment = monthlyPaymentBD,
                    totalPayment = totalPaymentBD,
                    totalInterest = totalInterestBD,
                    totalMonths = totalMonths
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    data class Currency(
        val code: String,
        val name: String,
        val symbol: String,
        val rateToUsd: Double // 1 USD = rate units
    )

    val supportedCurrencies = listOf(
        Currency("USD", "US Dollar", "$", 1.0),
        Currency("EUR", "Euro", "€", 0.9245),
        Currency("GBP", "British Pound", "£", 0.7850),
        Currency("JPY", "Japanese Yen", "¥", 152.40),
        Currency("CAD", "Canadian Dollar", "CA$", 1.3850),
        Currency("AUD", "Australian Dollar", "AU$", 1.5120),
        Currency("INR", "Indian Rupee", "₹", 84.10),
        Currency("CNY", "Chinese Yuan", "¥", 7.2350),
        Currency("CHF", "Swiss Franc", "CHF", 0.8820),
        Currency("BRL", "Brazilian Real", "R$", 5.7100)
    )

    data class CurrencyConversionResult(
        val convertedAmount: BigDecimal,
        val exchangeRate: BigDecimal,
        val fromCode: String,
        val toCode: String
    )

    fun convertCurrency(
        amountStr: String,
        fromCode: String,
        toCode: String
    ): CurrencyConversionResult? {
        val amount = amountStr.trim().toBigDecimalOrNull() ?: return null
        if (amount < BigDecimal.ZERO) return null

        val from = supportedCurrencies.find { it.code == fromCode } ?: return null
        val to = supportedCurrencies.find { it.code == toCode } ?: return null

        return try {
            // amount in USD = amount / from.rateToUsd
            // amount in target = (amount / from.rateToUsd) * to.rateToUsd
            val rate = BigDecimal(to.rateToUsd / from.rateToUsd)
            val converted = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP)

            CurrencyConversionResult(
                convertedAmount = converted,
                exchangeRate = rate.setScale(4, RoundingMode.HALF_UP),
                fromCode = fromCode,
                toCode = toCode
            )
        } catch (e: Exception) {
            null
        }
    }
}
