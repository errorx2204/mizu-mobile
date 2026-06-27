package com.rushov.mizu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rushov.mizu.presentation.screens.MainAppScreen
import com.rushov.mizu.presentation.screens.OnboardingScreen
import com.rushov.mizu.presentation.screens.SplashScreen
import com.rushov.mizu.ui.theme.MizuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MizuTheme {
                MizuApp()
            }
        }
    }
}

@Composable
fun MizuApp() {
    var currentScreen by remember { mutableStateOf("splash") }

    when (currentScreen) {
        "splash" -> SplashScreen(
            onSplashFinished = { currentScreen = "onboarding" }
        )
        "onboarding" -> OnboardingScreen(
            onOnboardingFinished = { currentScreen = "main" }
        )
        "main" -> MainAppScreen()
    }
}
