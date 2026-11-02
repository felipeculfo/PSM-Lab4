package com.example.lab04

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.lab04.ui.theme.Lab04Theme // Zmień na Lab04Theme (domyślna nazwa motywu dla tego projektu)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab04Theme { // Użycie właściwej nazwy motywu wygenerowanego dla projektu
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Wywołanie ekranu kalkulatora z poprawnym przekazaniem marginesów
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}