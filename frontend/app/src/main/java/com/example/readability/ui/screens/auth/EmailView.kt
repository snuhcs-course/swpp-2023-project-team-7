package com.example.readability.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.ui.animation.animateIMEDp
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.theme.ReadabilityTheme

@Composable
@Preview(device = "id:pixel_5")
fun EmailPreview() {
    ReadabilityTheme {
        EmailView()
    }
}

private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)\$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailView(
    onBack: () -> Unit = {},
    onNavigateSignIn: (String) -> Unit = {},
    onNavigateSignUp: () -> Unit = {},
    onNavigateForgotPassword: () -> Unit = {}
) {
    var showError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }

    val emailFocusRequester = remember { FocusRequester() }

    val checkEmailError = { emailRegex.matches(email).not() }

    val checkError = {
        checkEmailError()
    }

    val submit = {
        if (checkError()) {
            showError = true
        } else {
            onNavigateSignIn(email)
        }
    }

    LaunchedEffect(Unit) {
        emailFocusRequester.requestFocus()
    }

    Scaffold(modifier = Modifier
        .imePadding()
        .systemBarsPadding()
        .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Continue with email")
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Arrow Back"
                        )
                    }
                },
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                            contentDescription = "email icon",
                        )
                    },
                    isError = showError && emailError,
                    supportingText = if (showError && emailError) {
                        { Text(text = "Please enter a valid email address") }
                    } else null,
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
            }

            TextButton(onClick = { onNavigateForgotPassword() }) {
                Text("Forgot Password?")
            }

            TextButton(onClick = { onNavigateSignUp() }) {
                Text("Sign up")
            }

            RoundedRectButton(
                onClick = { submit() },
                modifier = Modifier.fillMaxWidth(),
                imeAnimation = animateIMEDp(
                    label = "EmailView_SignInButton_IMEDp",
                )
            ) {
                Text("Sign in")
            }

        }
    }
}
