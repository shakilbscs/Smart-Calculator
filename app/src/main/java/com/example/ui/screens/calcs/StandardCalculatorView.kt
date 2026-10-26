package com.example.ui.screens.calcs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.StandardCalcEngine
import com.example.ui.theme.BrandBlue

@Composable
fun StandardCalculatorView(
    onResultGenerated: (expression: String, result: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(StandardCalcEngine.CalculatorState()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Area (Matches Screen 9 in showcase)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Text(
                text = state.expression,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = TextAlign.End,
                lineHeight = 48.sp,
                modifier = Modifier.testTag("std_calc_expression")
            )
            if (state.resultPreview.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "= ${state.resultPreview}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("std_calc_preview")
                )
            }
        }

        // Keypad Grid (Matches Screen 9 in showcase)
        val buttons = listOf(
            listOf("AC", "+/-", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            buttons.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { key ->
                        val isOp = key in listOf("÷", "×", "-", "+", "=")
                        val isSpecial = key in listOf("AC", "+/-", "%")
                        val weight = if (key == "0") 2f else 1f

                        val isDark = androidx.compose.foundation.isSystemInDarkTheme() ||
                                MaterialTheme.colorScheme.surface.red < 0.2f

                        val bgColor = when {
                            key == "=" -> MaterialTheme.colorScheme.primary
                            isOp -> if (isDark) Color(0xFF1E3A8A) else Color(0xFFEFF6FF)
                            isSpecial -> if (isDark) Color(0xFF253347) else Color(0xFFF1F5F9)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val textColor = when {
                            key == "=" -> Color.White
                            isOp -> if (isDark) Color(0xFF93C5FD) else Color(0xFF1E60FF)
                            isSpecial -> if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        val shape = if (key == "0") RoundedCornerShape(32.dp) else CircleShape

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(weight)
                                .height(64.dp)
                                .clip(shape)
                                .then(
                                    if (key != "=") {
                                        Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape
                                        )
                                    } else Modifier
                                )
                                .background(bgColor)
                                .clickable {
                                    val oldExpr = state.expression
                                    val newState = StandardCalcEngine.onInput(state, key)
                                    state = newState
                                    if (key == "=" && newState.hasEvaluated) {
                                        onResultGenerated(oldExpr, newState.expression)
                                    }
                                }
                                .testTag("calc_key_$key")
                        ) {
                            Text(
                                text = key,
                                fontSize = 24.sp,
                                fontWeight = if (isOp || isSpecial || key == "=") FontWeight.Bold else FontWeight.SemiBold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
