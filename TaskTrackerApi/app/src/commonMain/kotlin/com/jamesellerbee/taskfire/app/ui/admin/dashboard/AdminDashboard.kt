package com.jamesellerbee.taskfire.app.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jamesellerbee.taskfire.app.ui.theme.backgroundVariant
import com.jamesellerbee.tasktracker.lib.entities.Account
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator

/**
 * Displays the admin dashboard.
 */
@Composable
fun AdminDashboard(serviceLocator: ServiceLocator, modifier: Modifier = Modifier) {
    val viewModel = remember { AdminDashboardViewModel(serviceLocator) }

    Column(modifier.background(MaterialTheme.colors.backgroundVariant)) {
        Text(
            "Admin dashboard",
            style = MaterialTheme.typography.h3,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colors.primaryVariant
        )

        AccountsHeader(viewModel, Modifier.fillMaxWidth(0.75f).align(Alignment.CenterHorizontally))
    }
}

@Composable
internal fun AccountsHeader(viewModel: AdminDashboardViewModel, modifier: Modifier = Modifier) {
    var headerExpanded by remember { mutableStateOf(true) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    val showAccountDialog = selectedAccount != null

    val accounts = viewModel.accounts.collectAsState().value
    val isFetching = viewModel.isFetchingAccounts.collectAsState().value

    Column(modifier = modifier.padding(8.dp)) {
        Card {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { headerExpanded = !headerExpanded }
                ) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isFetching) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        }

                        Icon(Icons.Default.AccountCircle, null)

                        Spacer(Modifier.width(8.dp))

                        Text("Accounts")

                        Spacer(Modifier.weight(1f))

                        if (!headerExpanded) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Collapse header")
                        } else {
                            Icon(Icons.Default.ArrowDropUp, contentDescription = "Expand header")
                        }
                    }
                }

                // Body
                if (headerExpanded) {
                    // show accounts listing
                    LazyColumn {
                        items(accounts) { account ->
                            Divider(modifier = Modifier.fillMaxWidth())

                            Box(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedAccount = account
                                }
                            ) {
                                Spacer(Modifier.height(8.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (account.verified) {
                                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Account verified")
                                        } else {
                                            Icon(Icons.Default.Warning, contentDescription = "Account not verified")
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Text(account.name)
                                    }

                                    Text(account.email.ifEmpty { "Email not set" })
                                }

                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAccountDialog) {
        AccountDialog(viewModel, selectedAccount!!) { selectedAccount = null }
    }
}

@Composable
internal fun AccountDialog(viewModel: AdminDashboardViewModel, selectedAccount: Account, onDismiss: () -> Unit) {
    var accountDeletionDialog by remember { mutableStateOf(false) }
    var resetPasswordDialog by remember { mutableStateOf(false) }
    var setTemporaryPasswordDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text(
                    text = selectedAccount.name,
                    style = MaterialTheme.typography.h4,
                )

                Button(
                    onClick = {
                        accountDeletionDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                    )
                ) {
                    Text("Delete")
                }

                Button(onClick = {
                    resetPasswordDialog = true
                }) {
                    Text("Reset password")
                }

                Row {
                    Button(onClick = { onDismiss() }) {
                        Text("Close")
                    }
                }
            }
        }
    }


    if (accountDeletionDialog) {
        var deleteError by remember { mutableStateOf(false) }
        Dialog(onDismissRequest = { accountDeletionDialog = false }) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    if (deleteError) {
                        Text("Could not delete account", color = MaterialTheme.colors.error)
                    }

                    Text("Are you sure you want to delete ${selectedAccount.name}?")
                    Row {
                        Button(onClick = {
                            viewModel.deleteAccount(selectedAccount) { success ->
                                if (success) {
                                    onDismiss()
                                } else {
                                    deleteError = true
                                }
                            }
                        }) {
                            Text("Yes, delete account \"${selectedAccount.name}\"")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = { accountDeletionDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (resetPasswordDialog) {
        Dialog(onDismissRequest = { resetPasswordDialog = false }) {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Select password reset method")

                    Button(onClick = {
                    }) {
                        Text("Email password reset link")
                    }

                    Button(onClick = {
                        setTemporaryPasswordDialog = true
                    }) {
                        Text("Set a temporary password")
                    }

                    Button(onClick = {
                        resetPasswordDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    if (setTemporaryPasswordDialog) {
        var temporaryPassword by remember { mutableStateOf(viewModel.generateTemporaryPassword()) }

        Dialog(onDismissRequest = { setTemporaryPasswordDialog = false }) {
            Card {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("Set a new temporary password for ${selectedAccount.name}")

                    OutlinedTextField(
                        value = temporaryPassword,
                        onValueChange = { temporaryPassword = it },
                        label = { Text("Temporary password") },
                        trailingIcon = {
                            IconButton(onClick = {
                                temporaryPassword = viewModel.generateTemporaryPassword()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Generate new temporary password")
                            }
                        }
                    )

                    Row {
                        Button(onClick = {
                            viewModel.updateAccount(selectedAccount.copy(password = temporaryPassword)) { success ->
                                if (success) {
                                    setTemporaryPasswordDialog = false
                                }
                            }
                        }) {
                            Text("Ok")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = {
                            setTemporaryPasswordDialog = false
                        }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}