package com.example.ui.screens.calcs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
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
import com.example.engine.FinanceCalcEngine
import com.example.ui.components.CalcActionButtonsRow
import com.example.ui.components.CalcInputField
import com.example.ui.components.CalcResultCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentTeal

@Composable
fun ProfitMarginView(
    currency: String,
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var costPrice by remember { mutableStateOf("60") }
    var sellingPrice by remember { mutableStateOf("100") }
    var result by remember { mutableStateOf<FinanceCalcEngine.ProfitMarginResult?>(null) }

    fun compute() {
        val res = FinanceCalcEngine.calculateProfitMargin(costPrice, sellingPrice)
        result = res
        if (res != null) {
            onResultGenerated("Cost: $currency$costPrice, Selling: $currency$sellingPrice", "Margin: ${res.marginPercent}%")
        }
    }

    LaunchedEffect(costPrice, sellingPrice) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CalcInputField(
            value = costPrice,
            onValueChange = { costPrice = it },
            label = "Cost Price",
            prefix = currency,
            placeholder = "60"
        )

        CalcInputField(
            value = sellingPrice,
            onValueChange = { sellingPrice = it },
            label = "Selling Price",
            prefix = currency,
            placeholder = "100"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                costPrice = ""
                sellingPrice = ""
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Profit Margin",
                mainResult = "${res.marginPercent}%",
                badgeIcon = Icons.Default.MonetizationOn,
                badgeColor = AccentOrange,
                breakdownRows = listOf(
                    "Profit Amount" to "$currency ${res.profitAmount}",
                    "Cost Markup" to "${res.markupPercent}%",
                    "Status" to if (res.isProfitable) "Profitable" else "Loss"
                )
            )
        }
    }
}

@Composable
fun LoanCalculatorView(
    currency: String,
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var loanAmount by remember { mutableStateOf("10000") }
    var interestRate by remember { mutableStateOf("5") }
    var termYears by remember { mutableStateOf("3") }
    var result by remember { mutableStateOf<FinanceCalcEngine.LoanResult?>(null) }

    fun compute() {
        val res = FinanceCalcEngine.calculateLoan(loanAmount, interestRate, termYears)
        result = res
        if (res != null) {
            onResultGenerated("Loan: $currency$loanAmount, $interestRate%, $termYears yrs", "Monthly: $currency${res.monthlyPayment}")
        }
    }

    LaunchedEffect(loanAmount, interestRate, termYears) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CalcInputField(
            value = loanAmount,
            onValueChange = { loanAmount = it },
            label = "Loan Amount",
            prefix = currency,
            placeholder = "10000"
        )

        CalcInputField(
            value = interestRate,
            onValueChange = { interestRate = it },
            label = "Annual Interest Rate",
            suffix = "%",
            placeholder = "5"
        )

        CalcInputField(
            value = termYears,
            onValueChange = { termYears = it },
            label = "Loan Term (Years)",
            suffix = "yrs",
            placeholder = "3"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                loanAmount = ""
                interestRate = ""
                termYears = ""
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Monthly Payment",
                mainResult = "$currency ${res.monthlyPayment}",
                badgeIcon = Icons.Default.MonetizationOn,
                badgeColor = AccentBlue,
                breakdownRows = listOf(
                    "Total Payment" to "$currency ${res.totalPayment}",
                    "Total Interest" to "$currency ${res.totalInterest}",
                    "Total Months" to "${res.totalMonths} months"
                )
            )
        }
    }
}

@Composable
fun CurrencyConverterView(
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("100") }
    var fromCode by remember { mutableStateOf("USD") }
    var toCode by remember { mutableStateOf("EUR") }
    var result by remember { mutableStateOf<FinanceCalcEngine.CurrencyConversionResult?>(null) }

    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    fun compute() {
        val res = FinanceCalcEngine.convertCurrency(amount, fromCode, toCode)
        result = res
        if (res != null) {
            onResultGenerated("$amount $fromCode to $toCode", "${res.convertedAmount} $toCode")
        }
    }

    LaunchedEffect(amount, fromCode, toCode) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // From Currency Picker
        Box {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedFrom = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text("From", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = FinanceCalcEngine.supportedCurrencies.find { it.code == fromCode }?.let { "${it.code} - ${it.name}" } ?: fromCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expandedFrom, onDismissRequest = { expandedFrom = false }) {
                FinanceCalcEngine.supportedCurrencies.forEach { curr ->
                    DropdownMenuItem(
                        text = { Text("${curr.code} - ${curr.name} (${curr.symbol})") },
                        onClick = {
                            fromCode = curr.code
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
                    val temp = fromCode
                    fromCode = toCode
                    toCode = temp
                }
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap currencies", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // To Currency Picker
        Box {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedTo = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text("To", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = FinanceCalcEngine.supportedCurrencies.find { it.code == toCode }?.let { "${it.code} - ${it.name}" } ?: toCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = expandedTo, onDismissRequest = { expandedTo = false }) {
                FinanceCalcEngine.supportedCurrencies.forEach { curr ->
                    DropdownMenuItem(
                        text = { Text("${curr.code} - ${curr.name} (${curr.symbol})") },
                        onClick = {
                            toCode = curr.code
                            expandedTo = false
                        }
                    )
                }
            }
        }

        CalcInputField(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount",
            placeholder = "100"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                amount = ""
                result = null
            },
            calculateLabel = "Convert"
        )

        result?.let { res ->
            CalcResultCard(
                title = "Converted Amount",
                mainResult = "${res.convertedAmount} $toCode",
                badgeIcon = Icons.Default.MonetizationOn,
                badgeColor = AccentTeal,
                breakdownRows = listOf(
                    "Original Amount" to "$amount $fromCode",
                    "Exchange Rate" to "1 $fromCode = ${res.exchangeRate} $toCode",
                    "Mode" to "Offline Reference Rates"
                )
            )
        }

        // Offline reference note
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Rates are offline references. Does not use paid live APIs.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
