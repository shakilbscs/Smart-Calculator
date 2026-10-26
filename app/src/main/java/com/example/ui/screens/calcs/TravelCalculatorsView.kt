package com.example.ui.screens.calcs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardDefaults
import com.example.engine.TravelCalcEngine
import com.example.ui.components.CalcActionButtonsRow
import com.example.ui.components.CalcInputField
import com.example.ui.components.CalcResultCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald

@Composable
fun FuelCostCalculatorView(
    currency: String,
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var distance by remember { mutableStateOf("500") }
    var efficiency by remember { mutableStateOf("15") }
    var pricePerUnit by remember { mutableStateOf("1.50") }
    var unitMode by remember { mutableStateOf(TravelCalcEngine.EfficiencyUnit.KM_PER_LITER) }
    var result by remember { mutableStateOf<TravelCalcEngine.FuelResult?>(null) }

    fun compute() {
        val res = TravelCalcEngine.calculateFuel(distance, efficiency, pricePerUnit, unitMode)
        result = res
        if (res != null) {
            onResultGenerated("Distance: $distance km, Efficiency: $efficiency ${unitMode.label}", "Fuel Cost: $currency${res.totalFuelCost}")
        }
    }

    LaunchedEffect(distance, efficiency, pricePerUnit, unitMode) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Unit toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TravelCalcEngine.EfficiencyUnit.entries.forEach { u ->
                FilterChip(
                    selected = unitMode == u,
                    onClick = { unitMode = u },
                    label = { Text(u.label) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        CalcInputField(
            value = distance,
            onValueChange = { distance = it },
            label = "Distance",
            suffix = "km",
            placeholder = "500"
        )

        CalcInputField(
            value = efficiency,
            onValueChange = { efficiency = it },
            label = "Fuel Efficiency",
            suffix = unitMode.label,
            placeholder = "15"
        )

        CalcInputField(
            value = pricePerUnit,
            onValueChange = { pricePerUnit = it },
            label = "Fuel Price per Liter",
            prefix = currency,
            placeholder = "1.50"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                distance = ""
                efficiency = ""
                pricePerUnit = ""
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Total Fuel Cost",
                mainResult = "$currency ${res.totalFuelCost}",
                badgeIcon = Icons.Default.DirectionsCar,
                badgeColor = AccentEmerald,
                breakdownRows = listOf(
                    "Fuel Needed" to "${res.totalFuelNeeded} L",
                    "Cost per 100 km" to "$currency ${res.costPer100Km}",
                    "Cost per km" to "$currency ${res.costPerKm}"
                )
            )
        }
    }
}

@Composable
fun UnitConverterView(
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var category by remember { mutableStateOf(TravelCalcEngine.UnitCategory.LENGTH) }
    var fromId by remember { mutableStateOf("km") }
    var toId by remember { mutableStateOf("mi") }
    var inputValue by remember { mutableStateOf("5") }
    var result by remember { mutableStateOf<TravelCalcEngine.ConversionResult?>(null) }

    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    val currentUnits = TravelCalcEngine.unitsByCategory[category] ?: emptyList()

    fun compute() {
        val res = TravelCalcEngine.convert(category, fromId, toId, inputValue)
        result = res
        if (res != null) {
            onResultGenerated("$inputValue $fromId to $toId", res.formattedResult)
        }
    }

    LaunchedEffect(category, fromId, toId, inputValue) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Category Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TravelCalcEngine.UnitCategory.entries.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = {
                        category = cat
                        val units = TravelCalcEngine.unitsByCategory[cat] ?: emptyList()
                        fromId = units.firstOrNull()?.id ?: ""
                        toId = units.getOrNull(1)?.id ?: fromId
                    },
                    label = { Text(cat.displayName) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // From Unit
        Box {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedFrom = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column {
                        Text("From", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = currentUnits.find { it.id == fromId }?.name ?: fromId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expandedFrom, onDismissRequest = { expandedFrom = false }) {
                currentUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.name) },
                        onClick = {
                            fromId = unit.id
                            expandedFrom = false
                        }
                    )
                }
            }
        }

        // Swap button
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = {
                    val temp = fromId
                    fromId = toId
                    toId = temp
                }
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap units", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // To Unit
        Box {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedTo = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column {
                        Text("To", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = currentUnits.find { it.id == toId }?.name ?: toId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expandedTo, onDismissRequest = { expandedTo = false }) {
                currentUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.name) },
                        onClick = {
                            toId = unit.id
                            expandedTo = false
                        }
                    )
                }
            }
        }

        CalcInputField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = "Value",
            placeholder = "5"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                inputValue = ""
                result = null
            },
            calculateLabel = "Convert"
        )

        result?.let { res ->
            CalcResultCard(
                title = "Result",
                mainResult = res.formattedResult,
                badgeIcon = Icons.Default.Calculate,
                badgeColor = AccentBlue,
                breakdownRows = listOf(
                    "Input" to "$inputValue $fromId",
                    "Formula" to res.conversionFormula
                )
            )
        }
    }
}
