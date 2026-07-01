package com.rushov.mizu.presentation.screens

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.rushov.mizu.data.local.DataStoreManager
import com.rushov.mizu.data.local.SecurityPreferences
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    onForgotPin: () -> Unit = {}
) {
    val context = LocalContext.current
    val securityPreferences = remember { SecurityPreferences(context) }
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()

    val savedPin by securityPreferences.savedPin.collectAsState(initial = "1234")
    val isBiometricEnabled by dataStore.isBiometricEnabled.collectAsState(initial = false)

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showForgotDialog by remember { mutableStateOf(false) }
    var biometricFailed by remember { mutableStateOf(false) }

    // Check if biometric is actually available on this device
    val biometricManager = remember { BiometricManager.from(context) }
    val canAuthenticate = remember {
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Only try biometric if: enabled in settings + hardware available + not already failed
    val shouldTryBiometric = isBiometricEnabled && canAuthenticate && !biometricFailed

    // Auto-trigger biometric on launch
    LaunchedEffect(shouldTryBiometric) {
        if (!shouldTryBiometric) return@LaunchedEffect

        // Try to get FragmentActivity from context
        val activity = context as? FragmentActivity
        if (activity == null) {
            Log.w("MIZU", "LockScreen: Context is not FragmentActivity (type=${context.javaClass.name}), skipping biometric")
            biometricFailed = true
            return@LaunchedEffect
        }

        try {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onUnlock()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.d("MIZU", "Biometric error: $errorCode - $errString")
                        biometricFailed = true
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d("MIZU", "Biometric authentication failed")
                        biometricFailed = true
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock MIZU")
                .setSubtitle("Use your fingerprint to unlock")
                .setNegativeButtonText("Use PIN")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("MIZU", "Biometric prompt failed: ${e.message}")
            biometricFailed = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "?? Unlock MIZU",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Show fingerprint icon if biometric is enabled and available but failed/cancelled
        if (isBiometricEnabled && canAuthenticate) {
            IconButton(
                onClick = { biometricFailed = false }, // Re-trigger biometric
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Use Fingerprint",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Tap fingerprint to unlock",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 4) {
                    pin = it
                    error = ""
                }
            },
            label = { Text("Enter PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (pin == savedPin) {
                    onUnlock()
                } else {
                    error = "Wrong PIN"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unlock")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                showForgotDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forgot PIN?")
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
            },
            title = {
                Text("Reset PIN")
            },
            text = {
                Text(
                    "This will reset your PIN to 1234 and return you to the login screen."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            securityPreferences.savePin("1234")
                        }
                        showForgotDialog = false
                        onForgotPin()
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
