package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode

object StandardCalcEngine {

    data class CalculatorState(
        val expression: String = "0",
        val resultPreview: String = "",
        val hasEvaluated: Boolean = false,
        val errorMessage: String? = null
    )

    fun onInput(state: CalculatorState, action: String): CalculatorState {
        return when (action) {
            "AC" -> CalculatorState(expression = "0", resultPreview = "")
            "C" -> {
                if (state.expression.length <= 1 || state.expression == "0") {
                    CalculatorState(expression = "0", resultPreview = "")
                } else {
                    val newExpr = state.expression.dropLast(1)
                    val preview = evaluateExpression(newExpr)
                    CalculatorState(expression = newExpr, resultPreview = preview ?: "")
                }
            }
            "+/-" -> {
                if (state.expression == "0" || state.expression.isBlank()) return state
                // Toggle sign on last number
                val lastNum = findLastNumber(state.expression)
                if (lastNum != null) {
                    val toggled = if (lastNum.startsWith("-")) lastNum.substring(1) else "-$lastNum"
                    val newExpr = state.expression.substring(0, state.expression.length - lastNum.length) + toggled
                    val preview = evaluateExpression(newExpr)
                    CalculatorState(expression = newExpr, resultPreview = preview ?: "")
                } else state
            }
            "%" -> {
                val lastNum = findLastNumber(state.expression)
                if (lastNum != null && !lastNum.endsWith("%")) {
                    val numBD = lastNum.toBigDecimalOrNull()
                    if (numBD != null) {
                        val pctVal = numBD.divide(BigDecimal(100), 6, RoundingMode.HALF_UP).stripTrailingZeros()
                        val newExpr = state.expression.substring(0, state.expression.length - lastNum.length) + pctVal.toPlainString()
                        val preview = evaluateExpression(newExpr)
                        CalculatorState(expression = newExpr, resultPreview = preview ?: "")
                    } else state
                } else state
            }
            "=" -> {
                val finalResult = evaluateExpression(state.expression)
                if (finalResult != null) {
                    CalculatorState(
                        expression = finalResult,
                        resultPreview = "",
                        hasEvaluated = true
                    )
                } else {
                    CalculatorState(
                        expression = state.expression,
                        resultPreview = "",
                        errorMessage = "Invalid calculation"
                    )
                }
            }
            "+", "-", "×", "÷" -> {
                val op = action
                val expr = if (state.hasEvaluated) state.expression else state.expression
                if (expr.endsWith("+") || expr.endsWith("-") || expr.endsWith("×") || expr.endsWith("÷")) {
                    val newExpr = expr.dropLast(1) + op
                    CalculatorState(expression = newExpr, resultPreview = state.resultPreview)
                } else {
                    val newExpr = expr + op
                    CalculatorState(expression = newExpr, resultPreview = state.resultPreview)
                }
            }
            "." -> {
                val lastNum = findLastNumber(state.expression)
                if (lastNum != null && lastNum.contains(".")) {
                    state // already has decimal point
                } else {
                    val newExpr = state.expression + "."
                    CalculatorState(expression = newExpr, resultPreview = state.resultPreview)
                }
            }
            else -> {
                // Number 0..9
                val digit = action
                val newExpr = if (state.expression == "0" || state.hasEvaluated) {
                    digit
                } else {
                    state.expression + digit
                }
                val preview = evaluateExpression(newExpr)
                CalculatorState(
                    expression = newExpr,
                    resultPreview = preview ?: "",
                    hasEvaluated = false
                )
            }
        }
    }

    private fun findLastNumber(expr: String): String? {
        val tokens = expr.split(Regex("[+×÷]"))
        return tokens.lastOrNull()
    }

    fun evaluateExpression(expr: String): String? {
        val sanitized = expr.replace("×", "*").replace("÷", "/")
        if (sanitized.isBlank() || sanitized.endsWith("+") || sanitized.endsWith("-") ||
            sanitized.endsWith("*") || sanitized.endsWith("/")
        ) {
            return null
        }

        return try {
            // Tokenize numbers and operators
            val tokens = mutableListOf<String>()
            var currentNumber = StringBuilder()

            var i = 0
            while (i < sanitized.length) {
                val c = sanitized[i]
                if (c in '0'..'9' || c == '.') {
                    currentNumber.append(c)
                } else if (c in listOf('+', '-', '*', '/')) {
                    if (c == '-' && (i == 0 || sanitized[i - 1] in listOf('+', '-', '*', '/'))) {
                        // Unary minus
                        currentNumber.append(c)
                    } else {
                        if (currentNumber.isNotEmpty()) {
                            tokens.add(currentNumber.toString())
                            currentNumber = StringBuilder()
                        }
                        tokens.add(c.toString())
                    }
                }
                i++
            }
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber.toString())
            }

            if (tokens.isEmpty()) return null

            // First pass: Multiply and Divide
            val pass1 = mutableListOf<String>()
            var idx = 0
            while (idx < tokens.size) {
                val token = tokens[idx]
                if (token == "*" || token == "/") {
                    if (pass1.isEmpty() || idx + 1 >= tokens.size) return null
                    val left = BigDecimal(pass1.removeAt(pass1.size - 1))
                    val right = BigDecimal(tokens[idx + 1])
                    if (token == "/" && right.compareTo(BigDecimal.ZERO) == 0) {
                        return "Error (Div by 0)"
                    }
                    val res = if (token == "*") {
                        left.multiply(right)
                    } else {
                        left.divide(right, 8, RoundingMode.HALF_UP)
                    }
                    pass1.add(res.stripTrailingZeros().toPlainString())
                    idx += 2
                } else {
                    pass1.add(token)
                    idx++
                }
            }

            // Second pass: Add and Subtract
            if (pass1.isEmpty()) return null
            var total = BigDecimal(pass1[0])
            var pIdx = 1
            while (pIdx < pass1.size) {
                val op = pass1[pIdx]
                val nextVal = BigDecimal(pass1[pIdx + 1])
                total = if (op == "+") {
                    total.add(nextVal)
                } else {
                    total.subtract(nextVal)
                }
                pIdx += 2
            }

            total.stripTrailingZeros().toPlainString()
        } catch (e: Exception) {
            null
        }
    }
}
