package com.jamesellerbee.taskfireandroid.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jamesellerbee.taskfireandroid.R
import com.jamesellerbee.taskfireandroid.ui.theme.PurpleGrey40
import com.jamesellerbee.taskfireandroid.ui.theme.TaskFireAndroidTheme
import com.jamesellerbee.taskfireandroid.util.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Mode {
    LOGIN,
    REGISTER
}

@Composable
fun Login(serviceLocator: ServiceLocator, modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf(Mode.LOGIN) }

    val onSuccessfulRegister = suspend {
        withContext(Dispatchers.Main) {
            mode = Mode.LOGIN
        }
    }

    val loginViewModel =
        remember { LoginViewModel(serviceLocator, onSuccessfulRegister = onSuccessfulRegister) }
    val message = loginViewModel.message.collectAsState().value
    val busy = loginViewModel.busy.collectAsState().value

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val actionsEnabled = username.isNotBlank() && password.isNotBlank()

    Column(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight(0.50f)
                .fillMaxWidth()
        ) {
            Box(
                Modifier
                    .background(PurpleGrey40, CircleShape)
            ) {
                Image(
                    painterResource(R.drawable.notesapp),
                    null,
                    Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }

            Text(
                text = "TaskFire",
                style = MaterialTheme.typography.headlineLarge,
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                message?.let {
                    Text(
                        text = it.second,
                        color = if (it.first) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (mode) {
                            Mode.LOGIN -> "Login"
                            Mode.REGISTER -> "Register"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    if (busy) {
                        CircularProgressIndicator()
                    }
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    label = { Text(text = "Username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                var showPassword by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = { Text(text = "Password") },
                    trailingIcon = {
                        IconButton(onClick = {
                            showPassword = !showPassword
                        }) {
                            if (showPassword) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Show password"
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Hide password",
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (mode == Mode.LOGIN) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 30.dp)
                    ) {
                        Checkbox(
                            checked = loginViewModel.rememberMe.collectAsState().value,
                            onCheckedChange = {
                                loginViewModel.onInteraction(LoginInteraction.RememberMe)
                            })
                        Text("Remember me")
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            when (mode) {
                                Mode.LOGIN -> {
                                    loginViewModel.onInteraction(
                                        LoginInteraction.Login(
                                            username = username,
                                            password = password
                                        )
                                    )
                                }

                                Mode.REGISTER -> {
                                    loginViewModel.onInteraction(
                                        LoginInteraction.Register(
                                            username = username,
                                            password = password
                                        )
                                    )
                                }
                            }
                        },
                        enabled = actionsEnabled
                    ) {
                        Text(
                            text = when (mode) {
                                Mode.LOGIN -> {
                                    "Sign in"
                                }

                                Mode.REGISTER -> {
                                    "Register"
                                }
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            mode = when (mode) {
                                Mode.LOGIN -> Mode.REGISTER
                                Mode.REGISTER -> Mode.LOGIN
                            }
                        },
                    ) {
                        Text(
                            text = when (mode) {
                                Mode.LOGIN -> {
                                    "Create an account"
                                }

                                Mode.REGISTER -> {
                                    "Back to sign in"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    val serviceLocator = ServiceLocator()

    TaskFireAndroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Login(serviceLocator = serviceLocator, modifier = Modifier.fillMaxSize())
        }
    }
}