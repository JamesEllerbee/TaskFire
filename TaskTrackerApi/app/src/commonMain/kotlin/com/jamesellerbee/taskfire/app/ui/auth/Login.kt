package com.jamesellerbee.taskfire.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jamesellerbee.taskfire.app.ui.theme.backgroundVariant
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

@Composable
fun Login(serviceLocator: ServiceLocator, modifier: Modifier = Modifier) {
    val viewModel = remember { LoginViewModel(serviceLocator) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.background(MaterialTheme.colors.backgroundVariant)
    ) {
        LoginCard(viewModel = viewModel)
    }
}

@Composable
fun LoginCard(viewModel: LoginViewModel) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val mode = viewModel.mode.collectAsState().value
    val isLoggingIn = viewModel.isLoggingIn.collectAsState().value
    val isLoginSuccess = viewModel.isLoginSuccess.collectAsState().value
    val isRegisterSuccess = viewModel.isRegisterSuccess.collectAsState().value

    Card(
        modifier = Modifier.fillMaxWidth(0.33f),
    ) {

        Column(Modifier.fillMaxWidth().padding(8.dp)) {
            Text(
                text = when (mode) {
                    LoginViewModel.Mode.LOGIN -> "Login"
                    LoginViewModel.Mode.REGISTER -> "Register"
                }
            )

            if (mode == LoginViewModel.Mode.REGISTER) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
//                    singleLine = true
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
//                    singleLine = true
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
//                    singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (isLoginSuccess == false) {
                Text("Your login was unsuccessful", color = MaterialTheme.colors.error)
            }

            if (isRegisterSuccess == true) {
                Text("Success! You may now login using your username and password combination", color = Color.Green)

            }

            if (isRegisterSuccess == false) {
                Text("Your registration was unsuccessful", color = MaterialTheme.colors.error)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoggingIn) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        when (mode) {
                            LoginViewModel.Mode.LOGIN -> viewModel.login(username, password)
                            LoginViewModel.Mode.REGISTER -> viewModel.register(email, username, password)
                        }

                    },
                    enabled = when (mode) {
                        LoginViewModel.Mode.LOGIN -> username.isNotBlank() && password.isNotBlank()
                        LoginViewModel.Mode.REGISTER -> email.isNotBlank() && username.isNotBlank() && password.isNotBlank()
                    },
                ) {
                    Text(
                        text = when (mode) {
                            LoginViewModel.Mode.LOGIN -> "Login"
                            LoginViewModel.Mode.REGISTER -> "Register"
                        }
                    )
                }
            }

            Row {
                Spacer(Modifier.weight(1f))
                Text(
                    when (mode) {
                        LoginViewModel.Mode.LOGIN -> "Need an account?"
                        LoginViewModel.Mode.REGISTER -> "Already have an account?"
                    }
                )
            }

            Row {
                Spacer(Modifier.weight(1f))
                Button(onClick = {
                    viewModel.setMode(
                        when (mode) {
                            LoginViewModel.Mode.LOGIN -> LoginViewModel.Mode.REGISTER
                            LoginViewModel.Mode.REGISTER -> LoginViewModel.Mode.LOGIN
                        }
                    )
                }) {
                    Text(
                        text = when (mode) {
                            LoginViewModel.Mode.LOGIN -> "Register"
                            LoginViewModel.Mode.REGISTER -> "Login"
                        }
                    )
                }
            }
        }
    }
}