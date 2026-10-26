package com.example.ui.screens.calcs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ShoppingCalcEngine
import com.example.ui.components.CalcActionButtonsRow
import com.example.ui.components.CalcInputField
import com.example.ui.components.CalcResultCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentTeal

@Composable
fun PercentageCalculatorView(
    currency: String,
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF) }
    var val1 by remember { mutableStateOf("200") }
    var val2 by remember { mutableStateOf("15") }
    var result by remember { mutableStateOf<ShoppingCalcEngine.PercentageResult?>(null) }

    fun compute() {
        val res = when (mode) {
            ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF ->
                ShoppingCalcEngine.calculatePercentage(mode, val2, val1)
            else ->
                ShoppingCalcEngine.calculatePercentage(mode, val1, val2)
        }
        result = res
        if (res != null) {
            onResultGenerated("${res.formattedDetails}", res.mainResult)
        }
    }

    LaunchedEffect(mode, val1, val2) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Mode Selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = mode == ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF,
                onClick = { mode = ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF },
                label = { Text("What is %") },
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = mode == ShoppingCalcEngine.PercentageMode.X_IS_WHAT_PERCENT_OF,
                onClick = { mode = ShoppingCalcEngine.PercentageMode.X_IS_WHAT_PERCENT_OF },
                label = { Text("Ratio %") },
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = mode == ShoppingCalcEngine.PercentageMode.PERCENT_INCREASE_DECREASE,
                onClick = { mode = ShoppingCalcEngine.PercentageMode.PERCENT_INCREASE_DECREASE },
                label = { Text("+/- Change") },
                shape = RoundedCornerShape(20.dp)
            )
        }

        CalcInputField(
            value = val1,
            onValueChange = { val1 = it },
            label = if (mode == ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF) "Total value" else "Initial value",
            placeholder = "200",
            testTag = "pct_val1_input"
        )

        CalcInputField(
            value = val2,
            onValueChange = { val2 = it },
            label = if (mode == ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF) "Percentage (%)" else "Final value",
            suffix = if (mode == ShoppingCalcEngine.PercentageMode.WHAT_IS_PERCENT_OF) "%" else null,
            placeholder = "15",
            testTag = "pct_val2_input"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                val1 = ""
                val2 = ""
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Result",
                mainResult = res.mainResult,
                badgeIcon = Icons.Default.Percent,
                badgeColor = AccentGreen,
                breakdownRows = res.secondaryDetails
            )
        }
    }
}

@Composable
fun DiscountCalculatorView(
    currency: String,
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var originalPrice by remember { mutableStateOf("100") }
    var discountPercent by remember { mutableStateOf("25") }
    var extraDiscount by remember { mutableStateOf("0") }
    var taxPercent by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf<ShoppingCalcEngine.DiscountResult?>(null) }

    fun compute() {
        val res = ShoppingCalcEngine.calculateDiscount(originalPrice, discountPercent, extraDiscount, taxPercent)
        result = res
        if (res != null) {
            onResultGenerated("Original: $currency$originalPrice, Discount: $discountPercent%", "Sale Price: $currency${res.salePrice}")
        }
    }

    LaunchedEffect(originalPrice, discountPercent, extraDiscount, taxPercent) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CalcInputField(
            value = originalPrice,
            onValueChange = { originalPrice = it },
            label = "Original Price",
            prefix = currency,
            placeholder = "100",
            testTag = "discount_original_input"
        )

        CalcInputField(
            value = discountPercent,
            onValueChange = { discountPercent = it },
            label = "Discount Percentage",
            suffix = "%",
            placeholder = "25",
            testTag = "discount_percent_input"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                originalPrice = ""
                discountPercent = ""
                extraDiscount = "0"
                taxPercent = "0"
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Sale Price",
                mainResult = "$currency ${res.salePrice}",
                badgeIcon = Icons.Default.LocalOffer,
                badgeColor = AccentOrange,
                breakdownRows = listOf(
                    "Discount Amount" to "$currency ${res.discountAmount}",
                    "You Save" to "${res.totalSavingsPercent}%",
                    "Final with Tax" to "$currency ${res.finalWithTax}"
                )
            )
        }
    }
}

@Composable
fun TaxVatCalculatorView(
    currency: String,
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("100") }
    var taxPercent by remember { mutableStateOf("10") }
    var isInclusive by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ShoppingCalcEngine.TaxResult?>(null) }

    fun compute() {
        val res = ShoppingCalcEngine.calculateTax(amount, taxPercent, isInclusive)
        result = res
        if (res != null) {
            onResultGenerated("Amount: $currency$amount, Tax: $taxPercent% (${if (isInclusive) "Inclusive" else "Exclusive"})", "Total: $currency${res.totalAmount}")
        }
    }

    LaunchedEffect(amount, taxPercent, isInclusive) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = !isInclusive,
                onClick = { isInclusive = false },
                label = { Text("Tax Exclusive (Add Tax)") },
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = isInclusive,
                onClick = { isInclusive = true },
                label = { Text("Tax Inclusive (Extract)") },
                shape = RoundedCornerShape(20.dp)
            )
        }

        CalcInputField(
            value = amount,
            onValueChange = { amount = it },
            label = if (isInclusive) "Total Price (Tax Included)" else "Base Amount",
            prefix = currency,
            placeholder = "100"
        )

        CalcInputField(
            value = taxPercent,
            onValueChange = { taxPercent = it },
            label = "Tax / VAT Percentage",
            suffix = "%",
            placeholder = "10"
        )

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                amount = ""
                taxPercent = ""
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Total Amount",
                mainResult = "$currency ${res.totalAmount}",
                badgeIcon = Icons.Default.MonetizationOn,
                badgeColor = AccentAmber,
                breakdownRows = listOf(
                    "Tax Amount" to "$currency ${res.taxAmount}",
                    "Base Amount" to "$currency ${res.baseAmount}",
                    "Tax Type" to if (res.isInclusive) "Tax Inclusive" else "Tax Added"
                )
            )
        }
    }
}

@Composable
fun TipSplitterView(
    currency: String,
    onResultGenerated: (summary: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var billAmount by remember { mutableStateOf("50") }
    var tipPercent by remember { mutableStateOf("15") }
    var splitCount by remember { mutableIntStateOf(2) }
    var result by remember { mutableStateOf<ShoppingCalcEngine.TipResult?>(null) }

    fun compute() {
        val res = ShoppingCalcEngine.calculateTip(billAmount, tipPercent, splitCount.toString())
        result = res
        if (res != null) {
            onResultGenerated("Bill: $currency$billAmount, Tip: $tipPercent%, $splitCount people", "Per Person: $currency${res.perPersonAmount}")
        }
    }

    LaunchedEffect(billAmount, tipPercent, splitCount) {
        compute()
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CalcInputField(
            value = billAmount,
            onValueChange = { billAmount = it },
            label = "Bill Amount",
            prefix = currency,
            placeholder = "50"
        )

        // Preset Tip Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("10", "15", "18", "20").forEach { preset ->
                FilterChip(
                    selected = tipPercent == preset,
                    onClick = { tipPercent = preset },
                    label = { Text("$preset%") },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        CalcInputField(
            value = tipPercent,
            onValueChange = { tipPercent = it },
            label = "Tip Percentage",
            suffix = "%",
            placeholder = "15"
        )

        // Stepper for Split Count
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Split Between",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { if (splitCount > 1) splitCount-- },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = "$splitCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    FilledIconButton(
                        onClick = { if (splitCount < 100) splitCount++ },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }

        CalcActionButtonsRow(
            onCalculate = { compute() },
            onReset = {
                billAmount = ""
                tipPercent = "15"
                splitCount = 1
                result = null
            }
        )

        result?.let { res ->
            CalcResultCard(
                title = "Amount Per Person",
                mainResult = "$currency ${res.perPersonAmount}",
                badgeIcon = Icons.Default.ShoppingBag,
                badgeColor = AccentTeal,
                breakdownRows = listOf(
                    "Tip Amount" to "$currency ${res.tipAmount}",
                    "Total Bill" to "$currency ${res.totalBill}",
                    "Split Across" to "${res.splitCount} people",
                    "Tip Per Person" to "$currency ${res.perPersonTip}"
                )
            )
        }
    }
}
