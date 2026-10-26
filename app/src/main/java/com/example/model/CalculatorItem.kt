package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentTeal

enum class CalculatorCategory(
    val title: String,
    val subtitle: String,
    val primaryColor: Color
) {
    DAILY_SHOPPING("Daily & Shopping", "Everyday math, shopping & tips", AccentGreen),
    FINANCE_BUSINESS("Finance & Business", "Profit, loans & commercial math", AccentOrange),
    HEALTH_PREGNANCY("Health & Pregnancy", "Carefully scoped clinical estimates", AccentPink),
    FITNESS_WELLNESS("Fitness & Wellness", "BMI, energy & health estimates", AccentCyan),
    TRAVEL_VEHICLE("Travel & Units", "Fuel, trips & unit conversions", AccentEmerald)
}

enum class CalculatorId {
    STANDARD,
    PERCENTAGE,
    DISCOUNT,
    TAX_VAT,
    TIP_SPLIT,
    PROFIT_MARGIN,
    LOAN_ESTIMATE,
    AGE,
    DATE_DIFFERENCE,
    FUEL_COST,
    UNIT_CONVERTER,
    BMI,
    PREGNANCY_DUE_DATE,
    FERTILE_WINDOW,
    CURRENCY_CONVERTER
}

data class CalculatorInfo(
    val id: CalculatorId,
    val title: String,
    val subtitle: String,
    val category: CalculatorCategory,
    val accentColor: Color,
    val isHealth: Boolean = false,
    val keywords: List<String> = emptyList()
)

object CalculatorRegistry {
    val allCalculators: List<CalculatorInfo> = listOf(
        CalculatorInfo(
            id = CalculatorId.STANDARD,
            title = "Standard Calculator",
            subtitle = "Basic & scientific arithmetic",
            category = CalculatorCategory.DAILY_SHOPPING,
            accentColor = AccentBlue,
            keywords = listOf("calculator", "math", "add", "subtract", "multiply", "divide", "plus", "minus")
        ),
        CalculatorInfo(
            id = CalculatorId.PERCENTAGE,
            title = "Percentage Calculator",
            subtitle = "Find percentages, ratios & changes",
            category = CalculatorCategory.DAILY_SHOPPING,
            accentColor = AccentGreen,
            keywords = listOf("percentage", "percent", "%", "ratio", "proportion", "increase", "decrease")
        ),
        CalculatorInfo(
            id = CalculatorId.DISCOUNT,
            title = "Discount Calculator",
            subtitle = "Calculate sale price & total savings",
            category = CalculatorCategory.DAILY_SHOPPING,
            accentColor = AccentOrange,
            keywords = listOf("discount", "sale", "price", "savings", "coupon", "off", "bargain")
        ),
        CalculatorInfo(
            id = CalculatorId.TAX_VAT,
            title = "Tax / VAT Calculator",
            subtitle = "Add or extract sales tax and VAT",
            category = CalculatorCategory.DAILY_SHOPPING,
            accentColor = AccentAmber,
            keywords = listOf("tax", "vat", "gst", "sales tax", "inclusive", "exclusive")
        ),
        CalculatorInfo(
            id = CalculatorId.TIP_SPLIT,
            title = "Tip & Bill Splitter",
            subtitle = "Split dinner bills and gratuity fairly",
            category = CalculatorCategory.DAILY_SHOPPING,
            accentColor = AccentTeal,
            keywords = listOf("tip", "bill", "split", "restaurant", "dining", "gratuity", "people")
        ),
        CalculatorInfo(
            id = CalculatorId.PROFIT_MARGIN,
            title = "Profit Margin & Markup",
            subtitle = "Gross profit margin & cost markup",
            category = CalculatorCategory.FINANCE_BUSINESS,
            accentColor = AccentOrange,
            keywords = listOf("profit", "margin", "markup", "cost", "revenue", "business", "selling price")
        ),
        CalculatorInfo(
            id = CalculatorId.LOAN_ESTIMATE,
            title = "Loan Calculator",
            subtitle = "Estimate monthly payments & interest",
            category = CalculatorCategory.FINANCE_BUSINESS,
            accentColor = AccentBlue,
            keywords = listOf("loan", "mortgage", "emi", "interest", "payment", "repayment", "bank")
        ),
        CalculatorInfo(
            id = CalculatorId.AGE,
            title = "Age Calculator",
            subtitle = "Exact age in years, months & days",
            category = CalculatorCategory.DAILY_SHOPPING,
            accentColor = AccentPurple,
            keywords = listOf("age", "birthday", "birth date", "years", "born", "lifespan")
        ),
        CalculatorInfo(
            id = CalculatorId.DATE_DIFFERENCE,
            title = "Date Difference",
            subtitle = "Find days and time between dates",
            category = CalculatorCategory.DAILY_SHOPPING,
            accentColor = AccentGreen,
            keywords = listOf("date", "calendar", "days between", "duration", "period", "difference")
        ),
        CalculatorInfo(
            id = CalculatorId.FUEL_COST,
            title = "Fuel Cost Calculator",
            subtitle = "Trip fuel consumption and cost",
            category = CalculatorCategory.TRAVEL_VEHICLE,
            accentColor = AccentEmerald,
            keywords = listOf("fuel", "gas", "petrol", "diesel", "mileage", "trip", "car", "distance")
        ),
        CalculatorInfo(
            id = CalculatorId.UNIT_CONVERTER,
            title = "Unit Converter",
            subtitle = "Length, weight, temp, volume & speed",
            category = CalculatorCategory.TRAVEL_VEHICLE,
            accentColor = AccentBlue,
            keywords = listOf("unit", "converter", "length", "km", "miles", "kg", "lbs", "celsius", "fahrenheit")
        ),
        CalculatorInfo(
            id = CalculatorId.CURRENCY_CONVERTER,
            title = "Currency Converter",
            subtitle = "Convert currencies with offline rates",
            category = CalculatorCategory.FINANCE_BUSINESS,
            accentColor = AccentTeal,
            keywords = listOf("currency", "exchange", "money", "dollar", "euro", "forex", "usd", "eur")
        ),
        CalculatorInfo(
            id = CalculatorId.BMI,
            title = "BMI Calculator",
            subtitle = "Body Mass Index & weight status",
            category = CalculatorCategory.FITNESS_WELLNESS,
            accentColor = AccentCyan,
            isHealth = true,
            keywords = listOf("bmi", "weight", "height", "body mass", "fitness", "healthy weight")
        ),
        CalculatorInfo(
            id = CalculatorId.PREGNANCY_DUE_DATE,
            title = "Pregnancy Due Date",
            subtitle = "Estimated due date & gestational weeks",
            category = CalculatorCategory.HEALTH_PREGNANCY,
            accentColor = AccentPink,
            isHealth = true,
            keywords = listOf("pregnancy", "due date", "baby", "gestation", "trimester", "lmp", "conception")
        ),
        CalculatorInfo(
            id = CalculatorId.FERTILE_WINDOW,
            title = "Fertile Window & Ovulation",
            subtitle = "Estimated ovulation and fertile cycle",
            category = CalculatorCategory.HEALTH_PREGNANCY,
            accentColor = AccentPink,
            isHealth = true,
            keywords = listOf("fertile", "ovulation", "cycle", "menstrual", "period", "fertility")
        )
    )

    fun getById(id: CalculatorId): CalculatorInfo =
        allCalculators.firstOrNull { it.id == id } ?: allCalculators.first()

    val defaultRecent = listOf(
        CalculatorId.DISCOUNT,
        CalculatorId.BMI,
        CalculatorId.AGE,
        CalculatorId.FUEL_COST
    )
}
