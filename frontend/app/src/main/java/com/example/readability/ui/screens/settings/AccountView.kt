package com.example.readability.ui.screens.settings

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.readability.LocalSnackbarHost
import com.example.readability.R
import com.example.readability.ui.components.SettingTitle
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch

@Composable
@Preview
fun AccountPreview() {
    ReadabilityTheme {
        AccountView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountView(
    onBack: () -> Unit = {},
    onNavigateChangePassword: () -> Unit = {},
    onUpdatePhoto: suspend () -> Result<Unit> = { Result.success(Unit) },
    onUpdatePersonalInfo: suspend () -> Result<Unit> = { Result.success(Unit) },
    onDeleteAccount: suspend () -> Result<Unit> = { Result.success(Unit) },
    onNavigateIntro: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var updatePhotoLoading by remember { mutableStateOf(false) }
    var updatePersonalInfoLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHost = LocalSnackbarHost.current

    val checkEmailError = {
        email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val checkUsernameError = {
        username.isEmpty()
    }
    val checkError = { checkEmailError() || checkUsernameError() }

    val submit = {
        if (checkError()) {
            showError = true
        } else {
            showError = false
            updatePersonalInfoLoading = true
            scope.launch {
                onUpdatePersonalInfo().onSuccess {
                    snackbarHost.showSnackbar("Personal info updated")
                }.onFailure {
                    snackbarHost.showSnackbar("Failed to update personal info: " + it.message)
                }
                updatePersonalInfoLoading = false
            }
        }
    }

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
                            snackbarHost.showSnackbar("Account deleted")
                            onNavigateIntro()
                        }.onFailure {
                            snackbarHost.showSnackbar("Failed to delete account: " + it.message)
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
        LaunchedEffect(Unit) {
            emailError = checkEmailError()
            usernameError = checkUsernameError()
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(48.dp)),
                    model = "https://picsum.photos/200/200",
                    contentDescription = "Profile Image",
                )
            }
            SettingTitle(modifier = Modifier.padding(top = 24.dp), text = "Personal Info")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("EmailTextField"),
                value = email,
                onValueChange = {
                    email = it
                    emailError = checkEmailError()
                },
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
                isError = showError && emailError,
                supportingText = if (showError && emailError) {
                    { Text(text = "Please enter a valid email address") }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("UsernameTextField"),
                value = username,
                onValueChange = {
                    username = it
                    usernameError = checkUsernameError()
                },
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
                isError = showError && usernameError,
                supportingText = if (showError && usernameError) {
                    { Text(text = "Please enter a valid username") }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            Spacer(modifier = Modifier.height(16.dp))
//            RoundedRectButton(
//                modifier = Modifier.padding(16.dp),
//                loading = updatePersonalInfoLoading,
//                onClick = { submit() },
//            ) {
//                Text(text = "Update Personal Info")
//            }
            SettingTitle(modifier = Modifier.padding(top = 24.dp), text = "Actions")
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
