package com.example.ui.screens.calcs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.engine.DateCalcEngine
import com.example.ui.components.CalcActionButtonsRow
import com.example.ui.components.CalcResultCard
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorView(
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var birthDate by remember { mutableStateOf(LocalDate.of(1990, 1, 15)) }
    var result by remember { mutableStateOf<DateCalcEngine.AgeResult?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    fun compute() {
        val res = DateCalcEngine.calculateAge(birthDate)
        result = res
        if (res != null) {
            onResultGenerated("Born: ${birthDate.format(formatter)}", "Age: ${res.years} Years ${res.months} Mos")
        }
    }

    LaunchedEffect(birthDate) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text("Date of Birth", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(birthDate.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
            }
        }

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                birthDate = LocalDate.now().minusYears(25)
                compute()
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Your Age",
                mainResult = "${res.years} Years",
                badgeIcon = Icons.Default.CalendarMonth,
                badgeColor = AccentPurple,
                breakdownRows = listOf(
                    "Years" to "${res.years}",
                    "Months" to "${res.months}",
                    "Days" to "${res.days}",
                    "Total Days" to "${res.totalDays}",
                    "Next Birthday" to "in ${res.nextBirthdayInDays} days",
                    "Born on" to res.dayOfWeekBorn
                )
            )
        }
    }

    if (showDatePicker) {
        val initialMillis = birthDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        birthDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDifferenceView(
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusYears(1).withDayOfMonth(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(3)) }
    var result by remember { mutableStateOf<DateCalcEngine.DateDiffResult?>(null) }

    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    fun compute() {
        val res = DateCalcEngine.calculateDateDifference(startDate, endDate)
        result = res
        onResultGenerated("${startDate.format(formatter)} to ${endDate.format(formatter)}", "${res.totalDays} Days")
    }

    LaunchedEffect(startDate, endDate) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { pickingStart = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text("Start Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(startDate.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Start Date", tint = MaterialTheme.colorScheme.primary)
            }
        }

        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { pickingEnd = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text("End Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(endDate.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select End Date", tint = MaterialTheme.colorScheme.primary)
            }
        }

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                startDate = LocalDate.now()
                endDate = LocalDate.now().plusMonths(1)
                compute()
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Difference",
                mainResult = "${res.totalDays} Days",
                badgeIcon = Icons.Default.CalendarMonth,
                badgeColor = AccentGreen,
                breakdownRows = listOf(
                    "Breakdown" to "${res.years} Year(s), ${res.months} Month(s), ${res.days} Day(s)",
                    "Weeks" to "${res.totalWeeks} Weeks + ${res.remainingDays} Days",
                    "Total Days" to "${res.totalDays} days"
                )
            )
        }
    }

    if (pickingStart) {
        val millis = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { pickingStart = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    pickingStart = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingStart = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (pickingEnd) {
        val millis = endDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { pickingEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    pickingEnd = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingEnd = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
