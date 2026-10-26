package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode

object ShoppingCalcEngine {

    enum class PercentageMode {
        WHAT_IS_PERCENT_OF,   // What is X% of Y?
        X_IS_WHAT_PERCENT_OF, // X is what % of Y?
        PERCENT_INCREASE_DECREASE // Change from X to Y
    }

    data class PercentageResult(
        val mainResult: String,
        val formattedDetails: String,
        val secondaryDetails: List<Pair<String, String>> = emptyList()
    )

    fun calculatePercentage(
        mode: PercentageMode,
        val1Str: String,
        val2Str: String
    ): PercentageResult? {
        val v1 = val1Str.trim().toBigDecimalOrNull() ?: return null
        val v2 = val2Str.trim().toBigDecimalOrNull() ?: return null

        return try {
            when (mode) {
                PercentageMode.WHAT_IS_PERCENT_OF -> {
                    // v1% of v2
                    val result = v2.multiply(v1).divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
                    val valuePlus = v2.add(result).setScale(2, RoundingMode.HALF_UP)
                    val valueMinus = v2.subtract(result).setScale(2, RoundingMode.HALF_UP)
                    PercentageResult(
                        mainResult = result.stripTrailingZeros().toPlainString(),
                        formattedDetails = "$v1% of $v2",
                        secondaryDetails = listOf(
                            "Value + $v1%" to valuePlus.toPlainString(),
                            "Value - $v1%" to valueMinus.toPlainString()
                        )
                    )
                }
                PercentageMode.X_IS_WHAT_PERCENT_OF -> {
                    // v1 is what % of v2
                    if (v2.compareTo(BigDecimal.ZERO) == 0) return null
                    val pct = v1.multiply(BigDecimal(100)).divide(v2, 2, RoundingMode.HALF_UP)
                    PercentageResult(
                        mainResult = "${pct.stripTrailingZeros().toPlainString()}%",
                        formattedDetails = "$v1 of $v2",
                        secondaryDetails = listOf(
                            "Fraction" to "$v1 / $v2",
                            "Ratio" to "${v1.divide(v2, 4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()}"
                        )
                    )
                }
                PercentageMode.PERCENT_INCREASE_DECREASE -> {
                    // change from v1 to v2
                    if (v1.compareTo(BigDecimal.ZERO) == 0) return null
                    val diff = v2.subtract(v1)
                    val pct = diff.multiply(BigDecimal(100)).divide(v1, 2, RoundingMode.HALF_UP)
                    val isIncrease = pct >= BigDecimal.ZERO
                    PercentageResult(
                        mainResult = "${if (isIncrease) "+" else ""}${pct.stripTrailingZeros().toPlainString()}%",
                        formattedDetails = if (isIncrease) "Increase of ${diff.abs()}" else "Decrease of ${diff.abs()}",
                        secondaryDetails = listOf(
                            "Difference" to diff.toPlainString(),
                            "Original Value" to v1.toPlainString(),
                            "New Value" to v2.toPlainString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    data class DiscountResult(
        val salePrice: BigDecimal,
        val discountAmount: BigDecimal,
        val totalSavingsPercent: BigDecimal,
        val taxAmount: BigDecimal,
        val finalWithTax: BigDecimal
    )

    fun calculateDiscount(
        originalPriceStr: String,
        discountPercentStr: String,
        extraDiscountPercentStr: String = "0",
        taxPercentStr: String = "0"
    ): DiscountResult? {
        val originalPrice = originalPriceStr.trim().toBigDecimalOrNull() ?: return null
        val discountPercent = discountPercentStr.trim().toBigDecimalOrNull() ?: return null
        val extraDiscount = (if (extraDiscountPercentStr.isBlank()) "0" else extraDiscountPercentStr)
            .trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
        val taxPercent = (if (taxPercentStr.isBlank()) "0" else taxPercentStr)
            .trim().toBigDecimalOrNull() ?: BigDecimal.ZERO

        if (originalPrice < BigDecimal.ZERO || discountPercent < BigDecimal.ZERO) return null

        return try {
            val discountFactor = discountPercent.divide(BigDecimal(100), 6, RoundingMode.HALF_UP)
            val primaryDiscount = originalPrice.multiply(discountFactor)
            val priceAfterFirstDiscount = originalPrice.subtract(primaryDiscount)

            val extraFactor = extraDiscount.divide(BigDecimal(100), 6, RoundingMode.HALF_UP)
            val secondaryDiscount = priceAfterFirstDiscount.multiply(extraFactor)
            val priceBeforeTax = priceAfterFirstDiscount.subtract(secondaryDiscount)
                .setScale(2, RoundingMode.HALF_UP)

            val totalDiscountAmount = originalPrice.subtract(priceBeforeTax)
                .setScale(2, RoundingMode.HALF_UP)

            val savingsPercent = if (originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                totalDiscountAmount.multiply(BigDecimal(100)).divide(originalPrice, 1, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            val taxAmount = if (taxPercent > BigDecimal.ZERO) {
                priceBeforeTax.multiply(taxPercent.divide(BigDecimal(100), 6, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            val finalWithTax = priceBeforeTax.add(taxAmount).setScale(2, RoundingMode.HALF_UP)

            DiscountResult(
                salePrice = priceBeforeTax,
                discountAmount = totalDiscountAmount,
                totalSavingsPercent = savingsPercent,
                taxAmount = taxAmount,
                finalWithTax = finalWithTax
            )
        } catch (e: Exception) {
            null
        }
    }

    data class TaxResult(
        val baseAmount: BigDecimal,
        val taxAmount: BigDecimal,
        val totalAmount: BigDecimal,
        val isInclusive: Boolean
    )

    fun calculateTax(
        amountStr: String,
        taxPercentStr: String,
        isInclusive: Boolean
    ): TaxResult? {
        val amount = amountStr.trim().toBigDecimalOrNull() ?: return null
        val taxPercent = taxPercentStr.trim().toBigDecimalOrNull() ?: return null
        if (amount < BigDecimal.ZERO || taxPercent < BigDecimal.ZERO) return null

        return try {
            if (isInclusive) {
                // Gross = Base * (1 + rate/100) => Base = Gross / (1 + rate/100)
                val divisor = BigDecimal.ONE.add(taxPercent.divide(BigDecimal(100), 6, RoundingMode.HALF_UP))
                val base = amount.divide(divisor, 2, RoundingMode.HALF_UP)
                val tax = amount.subtract(base).setScale(2, RoundingMode.HALF_UP)
                TaxResult(
                    baseAmount = base,
                    taxAmount = tax,
                    totalAmount = amount.setScale(2, RoundingMode.HALF_UP),
                    isInclusive = true
                )
            } else {
                // Base given, add tax
                val tax = amount.multiply(taxPercent.divide(BigDecimal(100), 6, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP)
                val total = amount.add(tax).setScale(2, RoundingMode.HALF_UP)
                TaxResult(
                    baseAmount = amount.setScale(2, RoundingMode.HALF_UP),
                    taxAmount = tax,
                    totalAmount = total,
                    isInclusive = false
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    data class TipResult(
        val tipAmount: BigDecimal,
        val totalBill: BigDecimal,
        val perPersonAmount: BigDecimal,
        val perPersonTip: BigDecimal,
        val splitCount: Int
    )

    fun calculateTip(
        billAmountStr: String,
        tipPercentStr: String,
        splitCountStr: String = "1"
    ): TipResult? {
        val bill = billAmountStr.trim().toBigDecimalOrNull() ?: return null
        val tipPercent = tipPercentStr.trim().toBigDecimalOrNull() ?: return null
        val splitCount = splitCountStr.trim().toIntOrNull() ?: 1

        if (bill < BigDecimal.ZERO || tipPercent < BigDecimal.ZERO || splitCount <= 0) return null

        return try {
            val tip = bill.multiply(tipPercent.divide(BigDecimal(100), 6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP)
            val total = bill.add(tip).setScale(2, RoundingMode.HALF_UP)
            val countBD = BigDecimal(splitCount)
            val perPerson = total.divide(countBD, 2, RoundingMode.HALF_UP)
            val perPersonTip = tip.divide(countBD, 2, RoundingMode.HALF_UP)

            TipResult(
                tipAmount = tip,
                totalBill = total,
                perPersonAmount = perPerson,
                perPersonTip = perPersonTip,
                splitCount = splitCount
            )
        } catch (e: Exception) {
            null
        }
    }
}
