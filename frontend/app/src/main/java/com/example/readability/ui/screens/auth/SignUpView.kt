package com.example.readability.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.LocalSnackbarHost
import com.example.readability.R
import com.example.readability.ui.animation.animateIMEDp
import com.example.readability.ui.components.CircularProgressIndicatorInButton
import com.example.readability.ui.components.PasswordTextField
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch

private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)\$")

// password regex: At least 8 characters including a letter and a number
private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$")

@Composable
@Preview(device = "id:pixel_5")
fun SignUpPreview() {
    ReadabilityTheme {
        SignUpView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpView(
    onBack: () -> Unit = {},
    onSubmitted: suspend (String) -> Result<Unit> = { Result.success(Unit) },
    onNavigateVerify: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val emailFocusRequester = remember { FocusRequester() }

    val scope = rememberCoroutineScope()

    var showError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var repeatPasswordError by remember { mutableStateOf(false) }

    val checkEmailError = { emailRegex.matches(email).not() }
    val checkUsernameError = { username.isEmpty() }
    val checkPasswordError = { passwordRegex.matches(password).not() }
    val checkRepeatPasswordError = { password != repeatPassword }

    val checkError: () -> Boolean = {
        checkEmailError() || checkUsernameError() || checkPasswordError() || checkRepeatPasswordError()
    }

    val submit = {
        if (checkError()) {
            showError = true
        } else {
            loading = true
            scope.launch {
                onSubmitted(email).onSuccess {
                    onNavigateVerify(email)
                }.onFailure {
                    loading = false
                    showError = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        emailFocusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .systemBarsPadding(),
        topBar = {
            TopAppBar(title = { Text("Sign up") }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }
            })
        }) { innerPaddings ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddings)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(emailFocusRequester),
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
                            contentDescription = "email"
                        )
                    },
                    isError = showError && emailError,
                    supportingText = if (showError && emailError) {
                        { Text(text = "Please enter a valid email") }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                            contentDescription = "user"
                        )
                    },
                    isError = showError && usernameError,
                    supportingText = if (showError && usernameError) {
                        { Text(text = "Please enter a valid username") }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                PasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = "Password",
                    password = password,
                    onPasswordChanged = {
                        password = it
                        passwordError = checkPasswordError()
                        repeatPasswordError = checkRepeatPasswordError()
                    },
                    supportingText = "At least 8 characters including a letter and a number",
                    isError = showError && passwordError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                PasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = "Repeat Password",
                    password = repeatPassword,
                    onPasswordChanged = {
                        repeatPassword = it
                        repeatPasswordError = checkRepeatPasswordError()
                    },
                    supportingText = if (showError && repeatPasswordError) "Passwords do not match" else null,
                    isError = showError && repeatPasswordError,
                    keyboardActions = KeyboardActions(onDone = {
                        submit()
                    }),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
            }
            RoundedRectButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { submit() },
                imeAnimation = animateIMEDp(label = "AuthView_SignUpView_imeDp"),
                enabled = !loading
            ) {
                if (loading) CircularProgressIndicatorInButton() else Text("Sign up")
            }
        }
    }
}


