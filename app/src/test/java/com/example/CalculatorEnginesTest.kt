package com.example

import com.example.engine.DateCalcEngine
import com.example.engine.FinanceCalcEngine
import com.example.engine.HealthCalcEngine
import com.example.engine.ShoppingCalcEngine
import com.example.engine.StandardCalcEngine
import com.example.engine.TravelCalcEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class CalculatorEnginesTest {

    @Test
    fun testStandardCalculatorArithmetic() {
        val res1 = StandardCalcEngine.evaluateExpression("1250+350")
        assertEquals("1600", res1)

        val res2 = StandardCalcEngine.evaluateExpression("100-25*2")
        assertEquals("50", res2)

        val res3 = StandardCalcEngine.evaluateExpression("10/4")
        assertEquals("2.5", res3)

        // Divide by zero check
        val resZero = StandardCalcEngine.evaluateExpression("10/0")
        assertEquals("Error (Div by 0)", resZero)
    }

    @Test
    fun testPercentageCalculations() {
        // 15% of 200 = 30
        val res1 = ShoppingCalcEngine.calculatePercentage(
            ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF,
            "15",
            "200"
        )
        assertNotNull(res1)
        assertEquals("30", res1?.mainResult)

        // 50 is what % of 200 = 25%
        val res2 = ShoppingCalcEngine.calculatePercentage(
            ShoppingCalcEngine.PercentageMode.X_IS_WHAT_PERCENT_OF,
            "50",
            "200"
        )
        assertNotNull(res2)
        assertEquals("25%", res2?.mainResult)

        // Change from 100 to 125 = +25%
        val res3 = ShoppingCalcEngine.calculatePercentage(
            ShoppingCalcEngine.PercentageMode.PERCENT_INCREASE_DECREASE,
            "100",
            "125"
        )
        assertNotNull(res3)
        assertEquals("+25%", res3?.mainResult)
    }

    @Test
    fun testDiscountAndSalePrice() {
        // Original 100, 25% off -> Sale Price 75.00, Savings 25.00
        val res = ShoppingCalcEngine.calculateDiscount("100", "25", "0", "0")
        assertNotNull(res)
        assertEquals(BigDecimal("75.00"), res?.salePrice)
        assertEquals(BigDecimal("25.00"), res?.discountAmount)
    }

    @Test
    fun testTaxCalculations() {
        // Base 100, 10% tax exclusive -> Tax 10.00, Total 110.00
        val resExclusive = ShoppingCalcEngine.calculateTax("100", "10", false)
        assertNotNull(resExclusive)
        assertEquals(BigDecimal("10.00"), resExclusive?.taxAmount)
        assertEquals(BigDecimal("110.00"), resExclusive?.totalAmount)

        // Gross 110, 10% tax inclusive -> Base 100.00, Tax 10.00
        val resInclusive = ShoppingCalcEngine.calculateTax("110", "10", true)
        assertNotNull(resInclusive)
        assertEquals(BigDecimal("100.00"), resInclusive?.baseAmount)
        assertEquals(BigDecimal("10.00"), resInclusive?.taxAmount)
    }

    @Test
    fun testTipAndBillSplitter() {
        // Bill 50, 15% tip -> Tip 7.50, Total 57.50, 2 people -> 28.75 per person
        val res = ShoppingCalcEngine.calculateTip("50", "15", "2")
        assertNotNull(res)
        assertEquals(BigDecimal("7.50"), res?.tipAmount)
        assertEquals(BigDecimal("57.50"), res?.totalBill)
        assertEquals(BigDecimal("28.75"), res?.perPersonAmount)
    }

    @Test
    fun testProfitMarginAndMarkup() {
        // Cost 60, Selling 100 -> Profit 40.00, Margin 40.00%, Markup 66.67%
        val res = FinanceCalcEngine.calculateProfitMargin("60", "100")
        assertNotNull(res)
        assertEquals(BigDecimal("40.00"), res?.profitAmount)
        assertEquals(BigDecimal("40.00"), res?.marginPercent)
        assertEquals(BigDecimal("66.67"), res?.markupPercent)
    }

    @Test
    fun testLoanCalculation() {
        // Loan 10000, 5% annual interest, 3 years (36 months) -> ~299.71/mo
        val res = FinanceCalcEngine.calculateLoan("10000", "5", "3")
        assertNotNull(res)
        assertEquals(BigDecimal("299.71"), res?.monthlyPayment)
        assertEquals(BigDecimal("10789.56"), res?.totalPayment)
    }

    @Test
    fun testAgeAndDateDifferencesIncludingLeapYear() {
        // Born on 1990-01-15, reference date 2024-01-15
        val birth = LocalDate.of(1990, 1, 15)
        val ref = LocalDate.of(2024, 1, 15)
        val ageRes = DateCalcEngine.calculateAge(birth, ref)
        assertNotNull(ageRes)
        assertEquals(34, ageRes?.years)
        assertEquals(0, ageRes?.months)
        assertEquals(0, ageRes?.days)

        // Leap year span: 2024-02-28 to 2024-03-01 is 2 days in leap year 2024
        val leapStart = LocalDate.of(2024, 2, 28)
        val leapEnd = LocalDate.of(2024, 3, 1)
        val diffRes = DateCalcEngine.calculateDateDifference(leapStart, leapEnd)
        assertEquals(2L, diffRes.totalDays)
    }

    @Test
    fun testFuelEconomyAndCost() {
        // 500 km at 15 km/L, fuel price $1.50 -> 33.33 L needed, Total cost $50.00
        val res = TravelCalcEngine.calculateFuel("500", "15", "1.50")
        assertNotNull(res)
        assertEquals(BigDecimal("33.33"), res?.totalFuelNeeded)
        assertEquals(BigDecimal("50.00"), res?.totalFuelCost)
    }

    @Test
    fun testUnitConversions() {
        // 5 km to miles
        val res = TravelCalcEngine.convert(TravelCalcEngine.UnitCategory.LENGTH, "km", "mi", "5")
        assertNotNull(res)
        assertTrue(res!!.formattedResult.contains("mi"))

        // Temperature: 0 C = 32 F
        val tempRes = TravelCalcEngine.convert(TravelCalcEngine.UnitCategory.TEMPERATURE, "c", "f", "0")
        assertNotNull(tempRes)
        assertEquals(BigDecimal("32.00"), tempRes?.convertedValue)
    }

    @Test
    fun testBmiCalculation() {
        // 70 kg, 175 cm -> BMI = 70 / (1.75^2) = 22.86 (Normal weight)
        val res = HealthCalcEngine.calculateBmi("70", "175")
        assertNotNull(res)
        assertEquals(BigDecimal("22.86"), res?.bmi)
        assertEquals(HealthCalcEngine.BmiCategory.NORMAL, res?.category)
    }

    @Test
    fun testPregnancyDueDateCalculation() {
        // LMP: Jan 1, 2025, Cycle 28 days -> Due Date Oct 8, 2025 (280 days later)
        val lmp = LocalDate.of(2025, 1, 1)
        val res = HealthCalcEngine.calculatePregnancyDueDate(lmp, 28, lmp.plusDays(171))
        assertNotNull(res)
        assertEquals(LocalDate.of(2025, 10, 8), res?.estimatedDueDate)
        assertEquals(24, res?.gestationalWeeks)
        assertEquals(3, res?.gestationalDays)
    }
}
