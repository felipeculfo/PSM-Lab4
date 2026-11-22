package com.example.lab04

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.lab04.ui.theme.Lab04Theme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CalcStateDebug"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate | savedInstanceState keys: ${savedInstanceState?.keySet()}")
        enableEdgeToEdge()
        setContent {
            Lab04Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val keys = outState.keySet()
        Log.d(TAG, "onSaveInstanceState | liczba kluczy: ${keys.size}, klucze: $keys")

        for (key in keys) {
            val value = outState.get(key)
            Log.d(TAG, "Bundle[$key] (${value?.javaClass?.simpleName}) = $value")
        }
    }
}