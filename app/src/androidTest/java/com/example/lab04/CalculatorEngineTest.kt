package com.example.lab04

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mariuszgromada.math.mxparser.License

class CalculatorEngineTest {

    init {
        License.iConfirmNonCommercialUse("Student Lab04 Tests")
    }

    // --- 8 wierszy z tabeli 3.1 ---

    @Test
    fun row1_operatorPrecedence_2_plus_2_times_2_is_6() {
        assertEquals("6", CalculatorEngine.evaluateExpression("2+2*2")) //
    }

    @Test
    fun row2_parentheses_2_plus_2_in_parens_times_2_is_8() {
        assertEquals("8", CalculatorEngine.evaluateExpression("(2+2)*2")) //
    }

    @Test
    fun row3_divisionByZero_1_div_0_returnsClearMessage() {
        val result = CalculatorEngine.evaluateExpression("1/0") //
        assertNotNull(result)
        assertEquals("Dzielenie przez zero", result) //
    }

    @Test
    fun row4_incompleteExpression_2_plus_returnsNull() {
        assertNull(CalculatorEngine.evaluateExpression("2+")) //[cite: 11]
    }

    @Test
    fun row5_unclosedParenthesis_returnsNull() {
        assertNull(CalculatorEngine.evaluateExpression("((2+2)")) //[cite: 11]
    }

    @Test
    fun row6_emptyInput_returnsNullWithoutErrorMessage() {
        assertNull(CalculatorEngine.evaluateExpression("")) //[cite: 11]
    }

    @Test
    fun row7_domainError_sqrtMinus1_returnsClearMessage() {
        val result = CalculatorEngine.evaluateExpression("sqrt(-1)") //[cite: 11]
        assertNotNull(result)
        assertEquals("Błąd dziedziny", result) //[cite: 11]
    }

    @Test
    fun row8_largeNumbers_1234567890_times_1234567890() {
        assertEquals(
            "1524157875019052030",
            CalculatorEngine.evaluateExpression("1234567890*1234567890")
        )
    }

    // --- Testy separatora dziesiętnego (kropka i przecinek) ---

    @Test
    fun decimalComma_2_5_plus_1_is_3_5() {
        assertEquals("3,5", CalculatorEngine.evaluateExpression("2,5+1")) //[cite: 10]
    }

    @Test
    fun decimalDot_2_5_plus_1_is_3_5() {
        assertEquals("3,5", CalculatorEngine.evaluateExpression("2.5+1")) //[cite: 10]
    }
}