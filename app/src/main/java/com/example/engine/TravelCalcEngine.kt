package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode

object TravelCalcEngine {

    enum class EfficiencyUnit(val label: String) {
        KM_PER_LITER("km/L"),
        L_PER_100KM("L/100km"),
        MPG("MPG (US)")
    }

    data class FuelResult(
        val totalFuelNeeded: BigDecimal,
        val totalFuelCost: BigDecimal,
        val costPerKm: BigDecimal,
        val costPer100Km: BigDecimal
    )

    fun calculateFuel(
        distanceStr: String,
        efficiencyStr: String,
        pricePerUnitStr: String,
        efficiencyUnit: EfficiencyUnit = EfficiencyUnit.KM_PER_LITER
    ): FuelResult? {
        val distance = distanceStr.trim().toBigDecimalOrNull() ?: return null
        val efficiency = efficiencyStr.trim().toBigDecimalOrNull() ?: return null
        val price = pricePerUnitStr.trim().toBigDecimalOrNull() ?: return null

        if (distance <= BigDecimal.ZERO || efficiency <= BigDecimal.ZERO || price <= BigDecimal.ZERO) return null

        return try {
            val fuelNeeded = when (efficiencyUnit) {
                EfficiencyUnit.KM_PER_LITER -> distance.divide(efficiency, 3, RoundingMode.HALF_UP)
                EfficiencyUnit.L_PER_100KM -> distance.multiply(efficiency).divide(BigDecimal(100), 3, RoundingMode.HALF_UP)
                EfficiencyUnit.MPG -> {
                    // 1 MPG = 0.425144 km/L -> efficiency in km/L = MPG * 0.425144
                    val kmPerLiter = efficiency.multiply(BigDecimal("0.425144"))
                    distance.divide(kmPerLiter, 3, RoundingMode.HALF_UP)
                }
            }

            val totalCost = fuelNeeded.multiply(price).setScale(2, RoundingMode.HALF_UP)
            val costPerKm = totalCost.divide(distance, 4, RoundingMode.HALF_UP)
            val costPer100Km = costPerKm.multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)

            FuelResult(
                totalFuelNeeded = fuelNeeded.setScale(2, RoundingMode.HALF_UP),
                totalFuelCost = totalCost,
                costPerKm = costPerKm.setScale(2, RoundingMode.HALF_UP),
                costPer100Km = costPer100Km
            )
        } catch (e: Exception) {
            null
        }
    }

    enum class UnitCategory(val displayName: String) {
        LENGTH("Length"),
        WEIGHT("Weight"),
        VOLUME("Volume"),
        TEMPERATURE("Temperature"),
        SPEED("Speed")
    }

    data class UnitDef(val id: String, val name: String, val factorToBase: Double)

    val unitsByCategory = mapOf(
        UnitCategory.LENGTH to listOf(
            UnitDef("km", "Kilometers (km)", 1000.0),
            UnitDef("m", "Meters (m)", 1.0),
            UnitDef("cm", "Centimeters (cm)", 0.01),
            UnitDef("mi", "Miles (mi)", 1609.344),
            UnitDef("yd", "Yards (yd)", 0.9144),
            UnitDef("ft", "Feet (ft)", 0.3048),
            UnitDef("in", "Inches (in)", 0.0254)
        ),
        UnitCategory.WEIGHT to listOf(
            UnitDef("kg", "Kilograms (kg)", 1.0),
            UnitDef("g", "Grams (g)", 0.001),
            UnitDef("lb", "Pounds (lb)", 0.45359237),
            UnitDef("oz", "Ounces (oz)", 0.02834952),
            UnitDef("t", "Metric Ton (t)", 1000.0)
        ),
        UnitCategory.VOLUME to listOf(
            UnitDef("l", "Liters (L)", 1.0),
            UnitDef("ml", "Milliliters (ml)", 0.001),
            UnitDef("gal", "Gallons US (gal)", 3.78541),
            UnitDef("floz", "Fluid Ounces US (fl oz)", 0.0295735),
            UnitDef("cup", "Cups US (cup)", 0.236588)
        ),
        UnitCategory.TEMPERATURE to listOf(
            UnitDef("c", "Celsius (°C)", 1.0),
            UnitDef("f", "Fahrenheit (°F)", 1.0),
            UnitDef("k", "Kelvin (K)", 1.0)
        ),
        UnitCategory.SPEED to listOf(
            UnitDef("kmh", "km/h", 1.0),
            UnitDef("mph", "Miles/h (mph)", 1.60934),
            UnitDef("ms", "Meters/sec (m/s)", 3.6),
            UnitDef("knot", "Knots (kn)", 1.852)
        )
    )

    data class ConversionResult(
        val convertedValue: BigDecimal,
        val formattedResult: String,
        val conversionFormula: String
    )

    fun convert(
        category: UnitCategory,
        fromId: String,
        toId: String,
        inputStr: String
    ): ConversionResult? {
        val input = inputStr.trim().toBigDecimalOrNull() ?: return null
        val units = unitsByCategory[category] ?: return null
        val fromUnit = units.find { it.id == fromId } ?: return null
        val toUnit = units.find { it.id == toId } ?: return null

        return try {
            if (category == UnitCategory.TEMPERATURE) {
                val inputDouble = input.toDouble()
                // Convert to Celsius base
                val inCelsius = when (fromId) {
                    "c" -> inputDouble
                    "f" -> (inputDouble - 32.0) * (5.0 / 9.0)
                    "k" -> inputDouble - 273.15
                    else -> inputDouble
                }
                // Convert from Celsius to target
                val resultDouble = when (toId) {
                    "c" -> inCelsius
                    "f" -> (inCelsius * (9.0 / 5.0)) + 32.0
                    "k" -> inCelsius + 273.15
                    else -> inCelsius
                }
                val bd = BigDecimal(resultDouble).setScale(2, RoundingMode.HALF_UP)
                ConversionResult(
                    convertedValue = bd,
                    formattedResult = "${bd.stripTrailingZeros().toPlainString()} ${toUnit.name.split(" ").last()}",
                    conversionFormula = "Standard temperature scale conversion"
                )
            } else {
                // Base conversion
                val inBase = input.multiply(BigDecimal(fromUnit.factorToBase.toString()))
                val inTarget = inBase.divide(BigDecimal(toUnit.factorToBase.toString()), 6, RoundingMode.HALF_UP)
                val rounded = inTarget.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros()

                ConversionResult(
                    convertedValue = inTarget.setScale(4, RoundingMode.HALF_UP),
                    formattedResult = "${rounded.toPlainString()} ${toUnit.name.split(" ").last()}",
                    conversionFormula = "1 ${fromUnit.id} = ${BigDecimal(fromUnit.factorToBase / toUnit.factorToBase).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()} ${toUnit.id}"
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
