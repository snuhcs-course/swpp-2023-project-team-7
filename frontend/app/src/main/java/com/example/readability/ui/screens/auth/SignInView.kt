package com.example.readability.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.LocalSnackbarHost
import com.example.readability.ui.animation.animateIMEDp
import com.example.readability.ui.components.CircularProgressIndicatorInButton
import com.example.readability.ui.components.PasswordTextField
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun SignInViewPreview() {
    ReadabilityTheme {
        SignInView(email = "test@example.com")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInView(
    email: String,
    onBack: () -> Unit = {},
    onPasswordSubmitted: suspend (String) -> Result<Unit> = { Result.success(Unit) },
    onNavigateForgotPassword: (String) -> Unit = {}
) {
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(false) }

    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val snackbarHost = LocalSnackbarHost.current

    val scope = rememberCoroutineScope()

    val submit = {
        if (password.isEmpty()) {
            passwordError = "Please enter a password"
        } else {
            focusManager.clearFocus()
            loading = true
            scope.launch {
                onPasswordSubmitted(password).onSuccess {
                    // TODO: show welcome message
                    snackbarHost.showSnackbar("Welcome back!")
                }.onFailure {
                    loading = false
                    passwordError = it.message ?: ""
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        passwordFocusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(title = { Text("Sign in", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Arrow Back"
                        )
                    }
                })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(value = email, onValueChange = {}, singleLine = true, label = {
                Text(text = "Email", color = Color.Gray.copy(alpha = 0.7f))
            }, leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = "delete",
                    tint = Color.Gray.copy(alpha = 0.7f)
                )
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), enabled = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(passwordFocusRequester),
                password = password,
                onPasswordChanged = { password = it },
                label = "Password",
                keyboardActions = KeyboardActions(onDone = {
                    submit()
                }),
                isError = passwordError != null,
                supportingText = passwordError
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { onNavigateForgotPassword(email) }) {
                Text("Forgot Password?")
            }
            RoundedRectButton(
                onClick = { submit() },
                modifier = Modifier.fillMaxWidth(),
                imeAnimation = animateIMEDp(
                    label = "AuthView_SignInView_imeAnimation"
                ),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicatorInButton()
                } else {
                    Text("Sign in")
                }
            }
        }
    }


}

