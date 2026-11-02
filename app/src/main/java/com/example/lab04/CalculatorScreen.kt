package com.example.lab04

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 3. WSKAZÓWKA: Opisanie przycisków jedną strukturą danych zamiast powielania wywołań
enum class ButtonRole {
    DIGIT, OPERATOR, ACTION, PARENTHESIS
}

data class CalcButtonSpec(
    val label: String,
    val role: ButtonRole,
    val onClickAction: (currentText: String) -> String
)

// Siatka 4 kolumny x 5 wierszy zdefiniowana w strukturze danych
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

// --- FUNKCJE LOGICZNE ---

fun appendOperator(text: String, op: String): String {
    if (text.isEmpty()) {
        return if (op == "-") "-" else ""
    }
    val lastChar = text.last().toString()
    if (lastChar in listOf("+", "-", "×", "÷")) {
        return text.dropLast(1) + op
    }
    return text + op
}

fun appendDecimal(text: String): String {
    val tokens = text.split(Regex("[+\\-×÷(]"))
    val currentNumber = tokens.lastOrNull() ?: ""
    return if (!currentNumber.contains(",")) {
        if (text.isEmpty() || text.last().toString() in listOf("+", "-", "×", "÷", "(")) {
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
    try {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(",", ".")

        val result = evaluateExpression(sanitized)
        return if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString().replace(".", ",")
        }
    } catch (e: Exception) {
        return "Błąd"
    }
}

private fun evaluateExpression(expr: String): Double {
    val tokens = tokenize(expr)
    val rpn = toRPN(tokens)
    return evalRPN(rpn)
}

private fun tokenize(s: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < s.length) {
        val c = s[i]
        if (c.isDigit() || c == '.') {
            val sb = StringBuilder()
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                sb.append(s[i])
                i++
            }
            tokens.add(sb.toString())
        } else if (c == '-' && (i == 0 || s[i - 1] in "+-*/(")) {
            i++
            val sb = StringBuilder("-")
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                sb.append(s[i])
                i++
            }
            tokens.add(sb.toString())
        } else {
            tokens.add(c.toString())
            i++
        }
    }
    return tokens
}

private fun toRPN(tokens: List<String>): List<String> {
    val out = mutableListOf<String>()
    val stack = mutableListOf<String>()
    val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)

    for (token in tokens) {
        if (token.toDoubleOrNull() != null) {
            out.add(token)
        } else if (token in precedence) {
            while (stack.isNotEmpty() && stack.last() in precedence && precedence[stack.last()]!! >= precedence[token]!!) {
                out.add(stack.removeAt(stack.size - 1))
            }
            stack.add(token)
        } else if (token == "(") {
            stack.add(token)
        } else if (token == ")") {
            while (stack.isNotEmpty() && stack.last() != "(") {
                out.add(stack.removeAt(stack.size - 1))
            }
            if (stack.isNotEmpty() && stack.last() == "(") stack.removeAt(stack.size - 1)
        }
    }
    while (stack.isNotEmpty()) {
        out.add(stack.removeAt(stack.size - 1))
    }
    return out
}

private fun evalRPN(rpn: List<String>): Double {
    val stack = mutableListOf<Double>()
    for (token in rpn) {
        val num = token.toDoubleOrNull()
        if (num != null) {
            stack.add(num)
        } else {
            val b = stack.removeAt(stack.size - 1)
            val a = stack.removeAt(stack.size - 1)
            val res = when (token) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> if (b != 0.0) a / b else throw ArithmeticException("Division by zero")
                else -> 0.0
            }
            stack.add(res)
        }
    }
    return if (stack.isNotEmpty()) stack.last() else 0.0
}

// GŁÓWNY EKRAN
@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2.2 POLE DZIAŁANIA: Obsługa długiego tekstu z poziomy scrollowaniem
        DisplayField(
            expression = expression,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
        )

        // 2.1 SIATKA PRZYCISKÓW: Zagnieżdżone Column i Row z Modifier.weight
        CalculatorGrid(
            buttonRows = calculatorButtons,
            onButtonClick = { action -> expression = action(expression) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.2f)
        )
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
        verticalArrangement = Arrangement.spacedBy(6.dp) // Wskazówka: Arrangement.spacedBy zamiast marginesów
    ) {
        buttonRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Wskazówka: proporcjonalny podział w pionie
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { spec ->
                    CalculatorButton(
                        spec = spec,
                        onClick = { onButtonClick(spec.onClickAction) },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f) // Wskazówka: proporcjonalny podział w poziomie
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
    }

    val contentColor = when (spec.role) {
        ButtonRole.DIGIT -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonRole.OPERATOR -> MaterialTheme.colorScheme.onPrimary
        ButtonRole.ACTION -> MaterialTheme.colorScheme.onErrorContainer
        ButtonRole.PARENTHESIS -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    // Wygląd przycisku zmieniany w jednym miejscu
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
            fontSize = 20.sp
        )
    }
}