package com.example.readability.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.ui.animation.animateIMEDp
import com.example.readability.ui.components.PasswordTextField
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@Preview(device = "id:pixel_5")
fun ResetPasswordPreview() {
    ReadabilityTheme {
        ResetPasswordView()
    }
}

private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordView(
    onBack: () -> Unit = {},
    onPasswordSubmitted: suspend (String) -> Result<Unit> = { Result.success(Unit) },
    onNavigateEmail: () -> Unit = {},
) {

    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var repeatPasswordError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val passwordFocusRequester = remember { FocusRequester() }

    val scope = rememberCoroutineScope()

    val isPasswordError = { newPassword.matches(passwordRegex).not() }
    val isRepeatPasswordError = { newPassword != repeatPassword }

    val isError = { isPasswordError() || isRepeatPasswordError() }

    val submit = {
        if (isError()) {
            showError = true
        } else {
            scope.launch {
                onPasswordSubmitted(newPassword).onSuccess {
                    withContext(Dispatchers.Main) { onNavigateEmail() }
                }.onFailure {
                    loading = false
                    showError = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .systemBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(title = { Text("Reset Password") }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }
            })
        }) { innerPadding ->
        LaunchedEffect(Unit) {
            passwordFocusRequester.requestFocus()
            passwordError = isPasswordError()
            repeatPasswordError = isRepeatPasswordError()
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(passwordFocusRequester)
                    .testTag("PasswordTextField"),
                label = "New Password",
                password = newPassword,
                onPasswordChanged = {
                    newPassword = it
                    passwordError = isPasswordError()
                },
                isError = passwordError && showError,
                supportingText = "At least 8 characters including a letter and a number",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("RepeatPasswordTextField"),
                label = "Repeat Password",
                password = repeatPassword,
                onPasswordChanged = {
                    repeatPassword = it
                    repeatPasswordError = isRepeatPasswordError()
                },
                keyboardActions = KeyboardActions(onDone = { submit() }),
                isError = showError && repeatPasswordError,
                supportingText = if (showError && repeatPasswordError) "Passwords do not match" else null,
            )
            Spacer(modifier = Modifier.weight(1f))
            RoundedRectButton(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ResetPasswordButton"),
                imeAnimation = animateIMEDp(label = "AuthView_ResetPasswordView_imeDp")
            ) {
                Text("Reset Password")
            }
        }
    }
}
