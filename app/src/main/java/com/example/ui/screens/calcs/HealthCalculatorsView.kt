package com.example.ui.screens.calcs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardDefaults
import com.example.engine.HealthCalcEngine
import com.example.ui.components.CalcActionButtonsRow
import com.example.ui.components.CalcInputField
import com.example.ui.components.CalcResultCard
import com.example.ui.components.HealthPrivacyNotice
import com.example.ui.components.MedicalDisclaimerBanner
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BmiCalculatorView(
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var weight by remember { mutableStateOf("70") }
    var height by remember { mutableStateOf("175") }
    var result by remember { mutableStateOf<HealthCalcEngine.BmiResult?>(null) }

    fun compute() {
        val res = HealthCalcEngine.calculateBmi(weight, height)
        result = res
        if (res != null) {
            onResultGenerated("Weight: $weight kg, Height: $height cm", "BMI: ${res.bmi} (${res.category.label})")
        }
    }

    LaunchedEffect(weight, height) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HealthPrivacyNotice()

        CalcInputField(
            value = weight,
            onValueChange = { weight = it },
            label = "Weight",
            suffix = "kg",
            placeholder = "70"
        )

        CalcInputField(
            value = height,
            onValueChange = { height = it },
            label = "Height",
            suffix = "cm",
            placeholder = "175"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                weight = ""
                height = ""
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Your BMI",
                mainResult = "${res.bmi}",
                badgeIcon = Icons.Default.FitnessCenter,
                badgeColor = res.category.color,
                breakdownRows = listOf(
                    "Classification" to res.category.label,
                    "Reference" to res.category.advice,
                    "Healthy Weight Range" to "${res.healthyWeightMinKg} – ${res.healthyWeightMaxKg} kg"
                )
            )

            // BMI Spectrum Bar (Matches Screen 19)
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF3B82F6), // Underweight blue
                                    Color(0xFF10B981), // Normal green
                                    Color(0xFFF59E0B), // Overweight amber
                                    Color(0xFFEF4444)  // Obese red
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("18.5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Normal: 18.5 – 24.9", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("30.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        MedicalDisclaimerBanner()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyDueDateView(
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var lmpDate by remember { mutableStateOf(LocalDate.now().minusWeeks(14)) }
    var cycleLength by remember { mutableIntStateOf(28) }
    var result by remember { mutableStateOf<HealthCalcEngine.PregnancyResult?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    fun compute() {
        val res = HealthCalcEngine.calculatePregnancyDueDate(lmpDate, cycleLength)
        result = res
        if (res != null) {
            onResultGenerated("LMP: ${lmpDate.format(formatter)}, Cycle: $cycleLength days", "Due Date: ${res.estimatedDueDate.format(formatter)}")
        }
    }

    LaunchedEffect(lmpDate, cycleLength) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HealthPrivacyNotice()

        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column {
                    Text("First Day of Last Period (LMP)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(lmpDate.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select LMP Date", tint = AccentPink)
            }
        }

        CalcInputField(
            value = cycleLength.toString(),
            onValueChange = { cycleLength = it.toIntOrNull() ?: 28 },
            label = "Cycle Length (Days)",
            suffix = "days",
            placeholder = "28"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                lmpDate = LocalDate.now().minusWeeks(12)
                cycleLength = 28
                compute()
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Estimated Due Date",
                mainResult = res.estimatedDueDate.format(formatter),
                badgeIcon = Icons.Default.Spa,
                badgeColor = AccentPink,
                breakdownRows = listOf(
                    "Gestational Age" to "${res.gestationalWeeks} Weeks ${res.gestationalDays} Days",
                    "Current Trimester" to "Trimester ${res.trimester}",
                    "Days Remaining" to "${res.daysRemaining} days",
                    "Estimated Progress" to "${res.progressPercent}%"
                )
            )
        }

        MedicalDisclaimerBanner()
    }

    if (showDatePicker) {
        val millis = lmpDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        lmpDate = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertileWindowView(
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var lmpDate by remember { mutableStateOf(LocalDate.now().minusDays(10)) }
    var cycleLength by remember { mutableIntStateOf(28) }
    var result by remember { mutableStateOf<HealthCalcEngine.FertileWindowResult?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    fun compute() {
        val res = HealthCalcEngine.calculateFertileWindow(lmpDate, cycleLength)
        result = res
        if (res != null) {
            onResultGenerated("LMP: ${lmpDate.format(formatter)}, Cycle: $cycleLength days", "Ovulation: ${res.estimatedOvulationDate.format(formatter)}")
        }
    }

    LaunchedEffect(lmpDate, cycleLength) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HealthPrivacyNotice()

        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column {
                    Text("First Day of Last Period (LMP)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(lmpDate.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select LMP Date", tint = AccentPink)
            }
        }

        CalcInputField(
            value = cycleLength.toString(),
            onValueChange = { cycleLength = it.toIntOrNull() ?: 28 },
            label = "Cycle Length (Days)",
            suffix = "days",
            placeholder = "28"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                lmpDate = LocalDate.now().minusDays(7)
                cycleLength = 28
                compute()
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Estimated Ovulation",
                mainResult = res.estimatedOvulationDate.format(formatter),
                badgeIcon = Icons.Default.Spa,
                badgeColor = AccentPink,
                breakdownRows = listOf(
                    "Fertile Window" to "${res.fertileWindowStart.format(formatter)} – ${res.fertileWindowEnd.format(formatter)}",
                    "Next Period Estimate" to res.nextPeriodEstimate.format(formatter),
                    "Cycle Basis" to "$cycleLength day menstrual cycle"
                )
            )
        }

        MedicalDisclaimerBanner()
    }

    if (showDatePicker) {
        val millis = lmpDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        lmpDate = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
