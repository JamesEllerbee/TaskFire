package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.theme.backgroundVariant
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

@Composable
fun Login(serviceLocator: ServiceLocator, modifier: Modifier = Modifier) {
    val viewModel = remember { LoginViewModel(serviceLocator) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoggingIn = viewModel.isLoggingIn.collectAsState().value
    val isLoginSuccess = viewModel.isLoginSuccess.collectAsState().value

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.background(MaterialTheme.colors.backgroundVariant)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.33f),
        ) {
            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                if (isLoginSuccess == false) {
                    Text("Your login was unsuccessful", color = MaterialTheme.colors.error)
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.login(username, password)
                        },
                        enabled = username.isNotBlank() && password.isNotBlank(),
                    ) {
                        Text(text = "Login")
                    }
                }
            }
        }
    }
}