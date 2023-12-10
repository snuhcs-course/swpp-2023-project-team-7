package com.snu.readability.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snu.readability.R
import com.snu.readability.ui.components.SettingTitle
import com.snu.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch

@Composable
@Preview
fun AccountPreview() {
    ReadabilityTheme {
        AccountView(
            email = "test@example.com",
            username = "John Doe",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountView(
    email: String,
    username: String,
    onBack: () -> Unit = {},
    onNavigateChangePassword: () -> Unit = {},
    onSignOut: suspend () -> Result<Unit> = { Result.success(Unit) },
    onDeleteAccount: suspend () -> Result<Unit> = { Result.success(Unit) },
    onNavigateIntro: () -> Unit = {},
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var updatePersonalInfoLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (showDeleteAccountDialog) {
        AlertDialog(icon = {
            Icon(
                Icons.Default.Info,
                contentDescription = "Info",
            )
        }, onDismissRequest = { showDeleteAccountDialog = false }, confirmButton = {
            Button(
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.error,
                ),
                onClick = {
                    scope.launch {
                        onDeleteAccount().onSuccess {
                            Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                            onNavigateIntro()
                        }.onFailure {
                            Toast.makeText(
                                context,
                                "Failed to delete account: " + it.message,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
            ) {
                Text(text = "Delete My Account")
            }
        }, title = {
            Text(text = "Delete Account")
        }, text = {
            Text(
                text = "WARNING: By deleting your account, all books will be deleted PERMANENTLY.\n" +
                    "\n" +
                    "This CANNOT be undone.",
                color = MaterialTheme.colorScheme.error,
            )
        }, dismissButton = {
            TextButton(onClick = { showDeleteAccountDialog = false }) {
                Text(text = "Cancel")
            }
        })
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .systemBarsPadding(),
        topBar = {
            TopAppBar(title = { Text(text = "Account") }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SettingTitle(text = "Personal Info")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("EmailTextField"),
                value = email,
                onValueChange = { },
                singleLine = true,
                label = {
                    Text(text = "Email")
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.email),
                        contentDescription = "email",
                    )
                },
                enabled = false,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("UsernameTextField"),
                value = username,
                onValueChange = {},
                singleLine = true,
                label = {
                    Text(text = "Username")
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "user",
                    )
                },
                enabled = false,
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingTitle(modifier = Modifier.padding(top = 24.dp), text = "Actions")
            // sign out
            ListItem(
                modifier = Modifier.clickable {
                    scope.launch {
                        onSignOut().onSuccess {
                            Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                            onNavigateIntro()
                        }.onFailure {
                            Toast.makeText(
                                context,
                                "Failed to sign out: " + it.message,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.sign_out),
                        contentDescription = "Sign Out",
                    )
                },
                headlineContent = { Text(text = "Sign Out") },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate",
                    )
                },
            )
            ListItem(
                modifier = Modifier.clickable {
                    onNavigateChangePassword()
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.password),
                        contentDescription = "Password",
                    )
                },
                headlineContent = { Text(text = "Change Password") },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate",
                    )
                },
            )
            // delete account
            ListItem(
                modifier = Modifier.clickable {
                    showDeleteAccountDialog = true
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                headlineContent = {
                    Text(
                        text = "Delete Account",
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate",
                    )
                },
            )
        }
    }
}
