package com.example.calculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(
                onLocationClick = {
                    startActivity(Intent(this, LocationActivity::class.java))
                },
                onCalculatorClick = {},
                onMediaPlayerClick = {}
//                onCalculatorClick = {
//                    startActivity(Intent(this, CalculatorActivity::class.java))
//                },
//                onMediaPlayerClick = {
//                    startActivity(Intent(this, MediaPlayerActivity::class.java))
//                }
            )
        }
    }
}

@Composable
fun MainScreen(
    onLocationClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    onMediaPlayerClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onLocationClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Location Activity")
        }
        Button(onClick = onCalculatorClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Calculator Activity")
        }
        Button(onClick = onMediaPlayerClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "MediaPlayer Activity")
        }
    }
}