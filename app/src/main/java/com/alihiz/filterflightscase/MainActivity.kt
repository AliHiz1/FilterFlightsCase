package com.alihiz.filterflightscase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.alihiz.filterflightscase.core.theme.FilterFlightsCaseTheme
import com.alihiz.filterflightscase.presentation.map.FlightMapScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FilterFlightsCaseTheme {
                FlightMapScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
