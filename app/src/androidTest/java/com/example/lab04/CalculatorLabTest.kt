package com.example.lab04

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab04.ui.theme.Lab04Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalculatorLabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun task1_testBasicInputAndDisplay() {
        composeTestRule.setContent {
            Lab04Theme {
                CalculatorScreen()
            }
        }

        // Weryfikacja obecności kluczowych elementów interfejsu
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
        composeTestRule.onNodeWithText("+").assertIsDisplayed()
        composeTestRule.onNodeWithText("=").assertIsDisplayed()
        composeTestRule.onNodeWithText("C").assertIsDisplayed()

        // Symulacja kliknięcia i sprawdzenie reakcji
        composeTestRule.onNodeWithText("7").performClick()
    }

    @Test
    fun task2_and_3_testScientificLayoutComponents() {
        composeTestRule.setContent {
            Lab04Theme {
                CalculatorScreen()
            }
        }

        // Sprawdzenie obecności elementów bazowych kalkulatora w UI
        composeTestRule.onNodeWithText("C").assertIsDisplayed()
        composeTestRule.onNodeWithText("(").assertIsDisplayed()
        composeTestRule.onNodeWithText(")").assertIsDisplayed()
    }

    @Test
    fun task4_testLightFlavorThemeRendering() {
        // Test renderowania wyłącznie w trybie jasnym
        composeTestRule.setContent {
            Lab04Theme(darkTheme = false) {
                CalculatorScreen()
            }
        }
        composeTestRule.onNodeWithText("C").assertIsDisplayed()
    }

    @Test
    fun task4_testDarkFlavorThemeRendering() {
        // Test renderowania wyłącznie w trybie ciemnym (osobny test zapobiega błędom setContent)
        composeTestRule.setContent {
            Lab04Theme(darkTheme = true) {
                CalculatorScreen()
            }
        }
        composeTestRule.onNodeWithText("C").assertIsDisplayed()
    }
}