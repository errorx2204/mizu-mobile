package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.local.SecurityPreferences
import kotlinx.coroutines.launch

@Composable
fun ChangePinScreen(
    onPinChanged: () -> Unit
) {

    val context = LocalContext.current
    val securityPreferences = remember {
        SecurityPreferences(context)
    }

    val currentPin by securityPreferences.savedPin.collectAsState(
        initial = "1234"
    )

    val scope = rememberCoroutineScope()

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "🔐 Change PIN",
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = oldPin,
            onValueChange = { if (it.length <= 4) oldPin = it },
            label = { Text("Current PIN") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newPin,
            onValueChange = { if (it.length <= 4) newPin = it },
            label = { Text("New PIN") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = { if (it.length <= 4) confirmPin = it },
            label = { Text("Confirm New PIN") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                when {

                    oldPin != currentPin ->
                        error = "Incorrect current PIN"

                    newPin.length != 4 ->
                        error = "PIN must be 4 digits"

                    newPin != confirmPin ->
                        error = "PINs do not match"

                    else -> {
                        scope.launch {
                            securityPreferences.savePin(newPin)
                            onPinChanged()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update PIN")
        }
    }
}