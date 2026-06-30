package com.rushov.mizu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.rushov.mizu.data.local.DataStoreManager
import com.rushov.mizu.presentation.screens.LoginScreen
import com.rushov.mizu.presentation.screens.MainAppScreen
import com.rushov.mizu.presentation.screens.OnboardingScreen
import com.rushov.mizu.presentation.screens.RegisterScreen
import com.rushov.mizu.presentation.screens.SplashScreen
import com.rushov.mizu.ui.theme.MizuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MizuApp()
        }
    }
}

@Composable
fun MizuApp() {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    var currentScreen by remember { mutableStateOf("splash") }
    
    // Use collectAsState for reactive dark mode updates
    val isDarkMode by dataStore.isDarkMode.collectAsState(initial = false)

    MizuTheme(darkTheme = isDarkMode) {
        when (currentScreen) {
            "splash" -> SplashScreen(
                onNavigateToOnboarding = { currentScreen = "onboarding" },
                onNavigateToMainApp = { currentScreen = "main" }
            )
            "onboarding" -> OnboardingScreen(
                onOnboardingFinished = { currentScreen = "login" }
            )
            "login" -> LoginScreen(
                onLoginSuccess = { currentScreen = "main" },
                onNavigateToRegister = { currentScreen = "register" }
            )
            "register" -> RegisterScreen(
                onRegisterSuccess = { currentScreen = "login" },
                onNavigateToLogin = { currentScreen = "login" }
            )
            "main" -> MainAppScreen(
                onLogout = { currentScreen = "login" }
            )
        }
    }
}
