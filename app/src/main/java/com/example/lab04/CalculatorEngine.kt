package com.example.lab04

import org.mariuszgromada.math.mxparser.Expression
import java.math.BigDecimal

object CalculatorEngine {

    private val divisionByZeroPattern = Regex("""(?<![0-9.,)])([+-]?\s*(?:\d+(?:[.,]\d+)?|\([^)]+\)))\s*[/÷]\s*0(?:\.0+|,0+)?(?![0-9.,)])""")
    private val negativeSqrtPattern = Regex("""\bsqrt\s*\(\s*-\s*\d+(?:[.,]\d+)?\s*\)""", RegexOption.IGNORE_CASE)

    fun normalizeExpression(expression: String): String {
        return expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("π", "pi")
            .replace(",", ".")
    }

    private fun hasBalancedParentheses(s: String): Boolean {
        var depth = 0
        for (ch in s) {
            when (ch) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth < 0) return false
                }
            }
        }
        return depth == 0
    }

    /**
     * Zwraca:
     * - null: puste wejście, wyrażenie niedokończone ("2+") lub nawias niezamknięty ("((2+2)") -> brak wyniku, bez awarii
     * - czytelny komunikat błędu: np. dla dzielenia przez zero ("1/0") lub błędu dziedziny ("sqrt(-1)")
     * - sformatowany wynik: dla poprawnych wyrażeń i dużych liczb
     */
    fun evaluateExpression(expression: String): String? {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return null

        val prepared = normalizeExpression(trimmed)
        if (prepared.last() in charArrayOf('+', '-', '*', '/', '^')) {
            return null
        }

        if (!hasBalancedParentheses(prepared)) {
            return null
        }

        if (negativeSqrtPattern.containsMatchIn(prepared)) {
            return "Błąd dziedziny"
        }

        if (divisionByZeroPattern.containsMatchIn(prepared)) {
            return "Dzielenie przez zero"
        }

        val exp = Expression(prepared)
        val rawResult = exp.calculate()

        if (rawResult.isNaN()) {
            return "Błąd"
        }

        if (rawResult.isInfinite()) {
            return "Dzielenie przez zero"
        }

        val cleaned = BigDecimal.valueOf(rawResult).stripTrailingZeros()
        val formatted = if (cleaned.scale() <= 0) {
            cleaned.toPlainString()
        } else {
            cleaned.toPlainString().replace(".", ",")
        }

        return if (formatted == "-0") "0" else formatted
    }
}