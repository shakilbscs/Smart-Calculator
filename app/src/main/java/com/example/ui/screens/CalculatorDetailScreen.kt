package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CalculatorId
import com.example.model.CalculatorRegistry
import com.example.ui.components.AdMobBannerPlaceholder
import com.example.ui.screens.calcs.AgeCalculatorView
import com.example.ui.screens.calcs.BmiCalculatorView
import com.example.ui.screens.calcs.CurrencyConverterView
import com.example.ui.screens.calcs.DateDifferenceView
import com.example.ui.screens.calcs.DiscountCalculatorView
import com.example.ui.screens.calcs.FertileWindowView
import com.example.ui.screens.calcs.FuelCostCalculatorView
import com.example.ui.screens.calcs.LoanCalculatorView
import com.example.ui.screens.calcs.PercentageCalculatorView
import com.example.ui.screens.calcs.PregnancyDueDateView
import com.example.ui.screens.calcs.ProfitMarginView
import com.example.ui.screens.calcs.StandardCalculatorView
import com.example.ui.screens.calcs.TaxVatCalculatorView
import com.example.ui.screens.calcs.TipSplitterView
import com.example.ui.screens.calcs.UnitConverterView
import com.example.ui.theme.AccentPink
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorDetailScreen(
    calculatorId: CalculatorId,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val calcInfo = remember(calculatorId) { CalculatorRegistry.getById(calculatorId) }

    val favorites by viewModel.favorites.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isFav = favorites.contains(calculatorId.name)

    var lastResultSummary by remember { mutableStateOf("") }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = calcInfo.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (lastResultSummary.isNotEmpty()) {
                        IconButton(onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${calcInfo.title} Result: $lastResultSummary")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Result"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share result",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val nextMode = if (themeMode == "dark") "light" else "dark"
                            viewModel.setThemeMode(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (themeMode == "dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { viewModel.toggleFavorite(calculatorId) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) AccentPink else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Strictly exclude AdMob on health / sensitive calculators per privacy policy & prompt requirements!
            if (!calcInfo.isHealth && calculatorId != CalculatorId.STANDARD) {
                Box(modifier = Modifier.padding(12.dp)) {
                    AdMobBannerPlaceholder()
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        val onResult: (String, String) -> Unit = { summary, result ->
            lastResultSummary = "$summary => $result"
            viewModel.saveHistory(calculatorId, summary, result)
        }

        if (calculatorId == CalculatorId.STANDARD) {
            StandardCalculatorView(
                onResultGenerated = { expr, res ->
                    onResult(expr, res)
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (calculatorId) {
                    CalculatorId.PERCENTAGE -> PercentageCalculatorView(currency, onResult)
                    CalculatorId.DISCOUNT -> DiscountCalculatorView(currency, onResult)
                    CalculatorId.TAX_VAT -> TaxVatCalculatorView(currency, onResult)
                    CalculatorId.TIP_SPLIT -> TipSplitterView(currency, onResult)
                    CalculatorId.PROFIT_MARGIN -> ProfitMarginView(currency, onResult)
                    CalculatorId.LOAN_ESTIMATE -> LoanCalculatorView(currency, onResult)
                    CalculatorId.AGE -> AgeCalculatorView(onResult)
                    CalculatorId.DATE_DIFFERENCE -> DateDifferenceView(onResult)
                    CalculatorId.FUEL_COST -> FuelCostCalculatorView(currency, onResult)
                    CalculatorId.UNIT_CONVERTER -> UnitConverterView(onResult)
                    CalculatorId.CURRENCY_CONVERTER -> CurrencyConverterView(onResult)
                    CalculatorId.BMI -> BmiCalculatorView(onResult)
                    CalculatorId.PREGNANCY_DUE_DATE -> PregnancyDueDateView(onResult)
                    CalculatorId.FERTILE_WINDOW -> FertileWindowView(onResult)
                    else -> {}
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
