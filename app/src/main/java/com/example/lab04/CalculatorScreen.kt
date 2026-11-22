package com.example.lab04

import android.content.res.Configuration
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonRole {
    DIGIT, OPERATOR, ACTION, PARENTHESIS, SCIENTIFIC
}

data class CalcButtonSpec(
    val label: String,
    val role: ButtonRole,
    val onClickAction: (currentText: String) -> String
)

val calculatorButtons = listOf(
    listOf(
        CalcButtonSpec("C", ButtonRole.ACTION) { "" },
        CalcButtonSpec("(", ButtonRole.PARENTHESIS) { text -> text + "(" },
        CalcButtonSpec(")", ButtonRole.PARENTHESIS) { text -> text + ")" },
        CalcButtonSpec("÷", ButtonRole.OPERATOR) { text -> appendOperator(text, "÷") }
    ),
    listOf(
        CalcButtonSpec("7", ButtonRole.DIGIT) { text -> text + "7" },
        CalcButtonSpec("8", ButtonRole.DIGIT) { text -> text + "8" },
        CalcButtonSpec("9", ButtonRole.DIGIT) { text -> text + "9" },
        CalcButtonSpec("×", ButtonRole.OPERATOR) { text -> appendOperator(text, "×") }
    ),
    listOf(
        CalcButtonSpec("4", ButtonRole.DIGIT) { text -> text + "4" },
        CalcButtonSpec("5", ButtonRole.DIGIT) { text -> text + "5" },
        CalcButtonSpec("6", ButtonRole.DIGIT) { text -> text + "6" },
        CalcButtonSpec("-", ButtonRole.OPERATOR) { text -> appendOperator(text, "-") }
    ),
    listOf(
        CalcButtonSpec("1", ButtonRole.DIGIT) { text -> text + "1" },
        CalcButtonSpec("2", ButtonRole.DIGIT) { text -> text + "2" },
        CalcButtonSpec("3", ButtonRole.DIGIT) { text -> text + "3" },
        CalcButtonSpec("+", ButtonRole.OPERATOR) { text -> appendOperator(text, "+") }
    ),
    listOf(
        CalcButtonSpec("0", ButtonRole.DIGIT) { text -> text + "0" },
        CalcButtonSpec(",", ButtonRole.DIGIT) { text -> appendDecimal(text) },
        CalcButtonSpec("⌫", ButtonRole.ACTION) { text -> if (text.isNotEmpty()) text.dropLast(1) else "" },
        CalcButtonSpec("=", ButtonRole.OPERATOR) { text -> calculateResult(text) }
    )
)

val scientificButtons = listOf(
    listOf(
        CalcButtonSpec("√", ButtonRole.SCIENTIFIC) { text -> text + "√(" },
        CalcButtonSpec("^", ButtonRole.SCIENTIFIC) { text -> text + "^" }
    ),
    listOf(
        CalcButtonSpec("sin", ButtonRole.SCIENTIFIC) { text -> text + "sin(" },
        CalcButtonSpec("cos", ButtonRole.SCIENTIFIC) { text -> text + "cos(" }
    ),
    listOf(
        CalcButtonSpec("tan", ButtonRole.SCIENTIFIC) { text -> text + "tan(" },
        CalcButtonSpec("log", ButtonRole.SCIENTIFIC) { text -> text + "log(" }
    ),
    listOf(
        CalcButtonSpec("π", ButtonRole.SCIENTIFIC) { text -> text + "π" },
        CalcButtonSpec("e", ButtonRole.SCIENTIFIC) { text -> text + "e" }
    ),
    listOf(
        CalcButtonSpec("ln", ButtonRole.SCIENTIFIC) { text -> text + "ln(" },
        CalcButtonSpec("abs", ButtonRole.SCIENTIFIC) { text -> text + "abs(" }
    )
)

fun appendOperator(text: String, op: String): String {
    if (text.isEmpty()) {
        return if (op == "-") "-" else ""
    }
    val lastChar = text.last().toString()
    if (lastChar in listOf("+", "-", "×", "÷", "^")) {
        return text.dropLast(1) + op
    }
    return text + op
}

fun appendDecimal(text: String): String {
    val tokens = text.split(Regex("[+\\-×÷(^]"))
    val currentNumber = tokens.lastOrNull() ?: ""
    return if (!currentNumber.contains(",")) {
        if (text.isEmpty() || text.last().toString() in listOf("+", "-", "×", "÷", "(", "^")) {
            text + "0,"
        } else {
            text + ","
        }
    } else {
        text
    }
}

fun calculateResult(expression: String): String {
    if (expression.isEmpty()) return ""
    return try {
        // Zabezpieczone obliczenia podstawowe i naukowe
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())
            .replace(",", ".")

        val result = evalSimple(sanitized)
        if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString().replace(".", ",")
        }
    } catch (e: Exception) {
        "Błąd"
    }
}

// Bezpieczny evaluator zoptymalizowany pod kątem stabilności laboratoriów
private fun evalSimple(expr: String): Double {
    // Prosty evaluator bazujący na sparsowanym ciągu znaków lub bibliotece standardowej
    val cleanExpr = expr.replace(" ", "")

    // Obsługa podstawowych funkcji jednoargumentowych i stałych
    if (cleanExpr.startsWith("sqrt(")) {
        val inner = cleanExpr.substring(5, cleanExpr.length - 1)
        return Math.sqrt(evalSimple(inner))
    }
    if (cleanExpr.startsWith("sin(")) {
        val inner = cleanExpr.substring(4, cleanExpr.length - 1)
        return Math.sin(Math.toRadians(evalSimple(inner)))
    }
    if (cleanExpr.startsWith("cos(")) {
        val inner = cleanExpr.substring(4, cleanExpr.length - 1)
        return Math.cos(Math.toRadians(evalSimple(inner)))
    }
    if (cleanExpr.startsWith("tan(")) {
        val inner = cleanExpr.substring(4, cleanExpr.length - 1)
        return Math.tan(Math.toRadians(evalSimple(inner)))
    }
    if (cleanExpr.startsWith("log(")) {
        val inner = cleanExpr.substring(4, cleanExpr.length - 1)
        return Math.log10(evalSimple(inner))
    }
    if (cleanExpr.startsWith("ln(")) {
        val inner = cleanExpr.substring(3, cleanExpr.length - 1)
        return Math.log(evalSimple(inner))
    }
    if (cleanExpr.startsWith("abs(")) {
        val inner = cleanExpr.substring(4, cleanExpr.length - 1)
        return Math.abs(evalSimple(inner))
    }

    // Podstawowy parser działań binarnych (+, -, *, /, ^)
    return parseAddSubtract(cleanExpr)
}

private fun parseAddSubtract(s: String): Double {
    var pos = s.length - 1
    var parenCount = 0
    while (pos >= 0) {
        when (s[pos]) {
            ')' -> parenCount++
            '(' -> parenCount--
            '+', '-' -> {
                if (parenCount == 0 && pos > 0 && s[pos-1] !in "+-*/^(") {
                    val left = parseAddSubtract(s.substring(0, pos))
                    val right = parseMultiplyDivide(s.substring(pos + 1))
                    return if (s[pos] == '+') left + right else left - right
                }
            }
        }
        pos--
    }
    return parseMultiplyDivide(s)
}

private fun parseMultiplyDivide(s: String): Double {
    var pos = s.length - 1
    var parenCount = 0
    while (pos >= 0) {
        when (s[pos]) {
            ')' -> parenCount++
            '(' -> parenCount--
            '*', '/' -> {
                if (parenCount == 0) {
                    val left = parseMultiplyDivide(s.substring(0, pos))
                    val right = parsePower(s.substring(pos + 1))
                    return if (s[pos] == '*') left * right else left / right
                }
            }
        }
        pos--
    }
    return parsePower(s)
}

private fun parsePower(s: String): Double {
    val idx = s.indexOf('^')
    if (idx != -1) {
        val left = parseFactor(s.substring(0, idx))
        val right = parsePower(s.substring(idx + 1))
        return Math.pow(left, right)
    }
    return parseFactor(s)
}

private fun parseFactor(s: String): Double {
    if (s.startsWith("(") && s.endsWith(")")) {
        return parseAddSubtract(s.substring(1, s.length - 1))
    }
    return s.toDoubleOrNull() ?: 0.0
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by rememberSaveable { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DisplayField(
            expression = expression,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
        )

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2.2f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorGrid(
                    buttonRows = calculatorButtons,
                    onButtonClick = { action -> expression = action(expression) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                )
                CalculatorGrid(
                    buttonRows = scientificButtons,
                    onButtonClick = { action -> expression = action(expression) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
        } else {
            CalculatorGrid(
                buttonRows = calculatorButtons,
                onButtonClick = { action -> expression = action(expression) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2.2f)
            )
        }
    }
}

@Composable
fun DisplayField(
    expression: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState, reverseScrolling = true),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    fontSize = 32.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalculatorGrid(
    buttonRows: List<List<CalcButtonSpec>>,
    onButtonClick: ((String) -> String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        buttonRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { spec ->
                    CalculatorButton(
                        spec = spec,
                        onClick = { onButtonClick(spec.onClickAction) },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    spec: CalcButtonSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (spec.role) {
        ButtonRole.DIGIT -> MaterialTheme.colorScheme.secondaryContainer
        ButtonRole.OPERATOR -> MaterialTheme.colorScheme.primary
        ButtonRole.ACTION -> MaterialTheme.colorScheme.errorContainer
        ButtonRole.PARENTHESIS -> MaterialTheme.colorScheme.tertiaryContainer
        ButtonRole.SCIENTIFIC -> MaterialTheme.colorScheme.tertiary
    }

    val contentColor = when (spec.role) {
        ButtonRole.DIGIT -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonRole.OPERATOR -> MaterialTheme.colorScheme.onPrimary
        ButtonRole.ACTION -> MaterialTheme.colorScheme.onErrorContainer
        ButtonRole.PARENTHESIS -> MaterialTheme.colorScheme.onTertiaryContainer
        ButtonRole.SCIENTIFIC -> MaterialTheme.colorScheme.onTertiary
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = spec.label,
            fontSize = 18.sp
        )
    }
}