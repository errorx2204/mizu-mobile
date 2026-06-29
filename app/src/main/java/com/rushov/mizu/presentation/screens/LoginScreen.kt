package com.rushov.mizu.presentation.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.local.DataStoreManager
import com.rushov.mizu.data.remote.LoginRequest
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.presentation.components.MizuButton
import com.rushov.mizu.presentation.components.MizuCard
import com.rushov.mizu.presentation.components.MizuTextField
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MIZU",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Welcome back!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        MizuCard {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                MizuTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = "Email"
                )

                MizuTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = "Password"
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    MizuButton(
                        text = "Login",
                        onClick = {
                            when {
                                email.isEmpty() -> errorMessage = "Please enter email"
                                password.isEmpty() -> errorMessage = "Please enter password"
                                else -> {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = ""
                                        try {
                                            val response = RetrofitClient.api.login(
                                                LoginRequest(email, password)
                                            )
                                            if (response.isSuccessful) {
                                                val tokenResponse = response.body()
                                                tokenResponse?.let {
                                                    // Save token and user data
                                                    DataStoreManager.saveUserData(
                                                        context = context,
                                                        token = it.access_token,
                                                        userId = 1, // You'll get this from backend
                                                        name = email.substringBefore("@"),
                                                        email = email
                                                    )
                                                }
                                                onLoginSuccess()
                                            } else {
                                                errorMessage = "Invalid email or password"
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Network error: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = "Don't have an account? Register",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
