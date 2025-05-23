package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calculator.ui.theme.CalculatorTheme

class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                CalculatorUI()
            }
        }
    }
}

@Composable
fun CalculatorUI() {
    var display by remember { mutableStateOf("") }
    var firstOperand by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("") }

    fun onDigitPress(digit: String) {
        display += digit
    }

    fun onOperatorPress(op: String) {
        if (display.isNotEmpty()) {
            firstOperand = display
            operator = op
            display = ""
        }
    }

    fun onEqualsPress() {
        val secondOperand = display
        val result = when (operator) {
            "+" -> firstOperand.toDoubleOrNull()?.plus(secondOperand.toDoubleOrNull() ?: 0.0)
            "-" -> firstOperand.toDoubleOrNull()?.minus(secondOperand.toDoubleOrNull() ?: 0.0)
            "*" -> firstOperand.toDoubleOrNull()?.times(secondOperand.toDoubleOrNull() ?: 0.0)
            "/" -> firstOperand.toDoubleOrNull()?.div(secondOperand.toDoubleOrNull() ?: 1.0)
            else -> null
        }
        display = result?.toString() ?: "Error"
        firstOperand = ""
        operator = ""
    }

    fun onClear() {
        display = ""
        firstOperand = ""
        operator = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = display,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val buttons = listOf(
                listOf("7", "8", "9", "/"),
                listOf("4", "5", "6", "*"),
                listOf("1", "2", "3", "-"),
                listOf("0", "C", "=", "+")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        Button(
                            onClick = {
                                when (label) {
                                    in "0".."9" -> onDigitPress(label)
                                    "+", "-", "*", "/" -> onOperatorPress(label)
                                    "=" -> onEqualsPress()
                                    "C" -> onClear()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}
