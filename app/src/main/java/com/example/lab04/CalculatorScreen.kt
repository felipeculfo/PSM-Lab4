package com.example.lab04

import android.content.res.Configuration
import android.util.Log
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import org.mariuszgromada.math.mxparser.Expression
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.compose.runtime.saveable.listSaver

enum class ButtonRole {
    DIGIT, OPERATOR, ACTION, PARENTHESIS, SCIENTIFIC
}

data class CalculatorUiState(
    val expression: String = "",
    val previousExpression: String = ""
)

private val CalculatorUiStateSaver = listSaver<CalculatorUiState, String>(
    save = { state -> listOf(state.expression, state.previousExpression) },
    restore = { savedList ->
        CalculatorUiState(
            expression = savedList.getOrElse(0) { "" },
            previousExpression = savedList.getOrElse(1) { "" }
        )
    }
)

data class CalcButtonSpec(
    val label: String,
    val role: ButtonRole,
    val onClickAction: (CalculatorUiState) -> CalculatorUiState
)

private object MathExpressionEvaluator {
    private const val TAG = "MxParserEval"

    fun normalizeForMxParser(expression: String): String {
        return expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("π", "pi")
            .replace(",", ".")
    }

    fun evaluate(state: CalculatorUiState): CalculatorUiState {
        val currentExpression = state.expression
        if (currentExpression.isBlank()) return state

        val prepared = normalizeForMxParser(currentExpression)
        Log.d(TAG, "Przekazywane do mXparser: '$prepared'")

        val exp = Expression(prepared)
        val rawResult = exp.calculate()
        Log.d(TAG, "Wynik calculate(): $rawResult | syntax=${exp.checkSyntax()}")

        if (rawResult.isNaN() || !rawResult.isFinite()) {
            return state
        }

        val rounded = BigDecimal.valueOf(rawResult)
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        val formatted = if (rounded.scale() <= 0) {
            rounded.toPlainString()
        } else {
            rounded.toPlainString().replace(".", ",")
        }

        val finalResult = if (formatted == "-0") "0" else formatted
        return CalculatorUiState(
            expression = finalResult,
            previousExpression = currentExpression
        )
    }
}

val calculatorButtons = listOf(
    listOf(
        CalcButtonSpec("C", ButtonRole.ACTION) { CalculatorUiState() },
        CalcButtonSpec("(", ButtonRole.PARENTHESIS) { state ->
            state.copy(expression = state.expression + "(", previousExpression = "")
        },
        CalcButtonSpec(")", ButtonRole.PARENTHESIS) { state ->
            state.copy(expression = state.expression + ")", previousExpression = "")
        },
        CalcButtonSpec("÷", ButtonRole.OPERATOR) { state ->
            state.copy(expression = appendOperator(state.expression, "÷"), previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("7", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "7", previousExpression = "")
        },
        CalcButtonSpec("8", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "8", previousExpression = "")
        },
        CalcButtonSpec("9", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "9", previousExpression = "")
        },
        CalcButtonSpec("×", ButtonRole.OPERATOR) { state ->
            state.copy(expression = appendOperator(state.expression, "×"), previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("4", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "4", previousExpression = "")
        },
        CalcButtonSpec("5", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "5", previousExpression = "")
        },
        CalcButtonSpec("6", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "6", previousExpression = "")
        },
        CalcButtonSpec("-", ButtonRole.OPERATOR) { state ->
            state.copy(expression = appendOperator(state.expression, "-"), previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("1", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "1", previousExpression = "")
        },
        CalcButtonSpec("2", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "2", previousExpression = "")
        },
        CalcButtonSpec("3", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "3", previousExpression = "")
        },
        CalcButtonSpec("+", ButtonRole.OPERATOR) { state ->
            state.copy(expression = appendOperator(state.expression, "+"), previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("0", ButtonRole.DIGIT) { state ->
            state.copy(expression = state.expression + "0", previousExpression = "")
        },
        CalcButtonSpec(",", ButtonRole.DIGIT) { state ->
            state.copy(expression = appendDecimal(state.expression), previousExpression = "")
        },
        CalcButtonSpec("⌫", ButtonRole.ACTION) { state ->
            val nextExpr = if (state.expression.isNotEmpty()) state.expression.dropLast(1) else ""
            state.copy(expression = nextExpr, previousExpression = "")
        },
        CalcButtonSpec("=", ButtonRole.OPERATOR) { state ->
            val currentExpression = state.expression
            val evaluated = CalculatorEngine.evaluateExpression(currentExpression)
            if (evaluated != null) {
                state.copy(
                    expression = evaluated,
                    previousExpression = currentExpression
                )
            } else {
                state
            }
        }
    )
)

val scientificButtons = listOf(
    listOf(
        CalcButtonSpec("√", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "√(", previousExpression = "")
        },
        CalcButtonSpec("^", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "^", previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("sin", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "sin(", previousExpression = "")
        },
        CalcButtonSpec("cos", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "cos(", previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("tan", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "tan(", previousExpression = "")
        },
        CalcButtonSpec("log", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "log(", previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("π", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "π", previousExpression = "")
        },
        CalcButtonSpec("e", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "e", previousExpression = "")
        }
    ),
    listOf(
        CalcButtonSpec("ln", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "ln(", previousExpression = "")
        },
        CalcButtonSpec("abs", ButtonRole.SCIENTIFIC) { state ->
            state.copy(expression = state.expression + "abs(", previousExpression = "")
        }
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

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var uiState by rememberSaveable(stateSaver = CalculatorUiStateSaver) {
        mutableStateOf(CalculatorUiState())
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DisplayField(
            expression = uiState.expression,
            previousExpression = uiState.previousExpression,
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
                    onButtonClick = { action -> uiState = action(uiState) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                )
                CalculatorGrid(
                    buttonRows = scientificButtons,
                    onButtonClick = { action -> uiState = action(uiState) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )
            }
        } else {
            CalculatorGrid(
                buttonRows = calculatorButtons,
                onButtonClick = { action -> uiState = action(uiState) },
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
    previousExpression: String,
    modifier: Modifier = Modifier
) {
    val historyScrollState = rememberScrollState()
    val expressionScrollState = rememberScrollState()

    LaunchedEffect(expression) {
        expressionScrollState.animateScrollTo(expressionScrollState.maxValue)
    }

    LaunchedEffect(previousExpression) {
        historyScrollState.animateScrollTo(historyScrollState.maxValue)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (previousExpression.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(historyScrollState),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = previousExpression,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 20.sp
                            ),
                            softWrap = false,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(expressionScrollState),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineMedium,
                        softWrap = false,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorGrid(
    buttonRows: List<List<CalcButtonSpec>>,
    onButtonClick: ((CalculatorUiState) -> CalculatorUiState) -> Unit,
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
            style = MaterialTheme.typography.labelLarge
        )
    }
}