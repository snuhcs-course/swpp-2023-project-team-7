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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.ui.animation.animateImeDp
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@Preview(device = "id:pixel_5")
fun ForgotPasswordPreview() {
    ReadabilityTheme {
        ForgotPasswordView("test@example.com")
    }
}

private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)\$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordView(
    email: String,
    onEmailChanged: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateVerify: (String) -> Unit = {},
    onEmailSubmitted: suspend (String) -> Result<Unit> = { Result.success(Unit) },
) {
    var emailError by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val emailFocusRequester = remember { FocusRequester() }

    val checkEmailError = { !emailRegex.matches(email) }

    val submit = {
        if (checkEmailError()) {
            showError = true
        } else {
            loading = true
            scope.launch {
                onEmailSubmitted(email).onSuccess {
                    withContext(Dispatchers.Main) { onNavigateVerify(email) }
                }.onFailure {
                    showError = true
                    loading = false
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Find Password") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Arrow Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            emailFocusRequester.requestFocus()
            emailError = checkEmailError()
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Enter your email\nto receive a recovery code.",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(emailFocusRequester)
                    .testTag("EmailTextField"),
                value = email,
                onValueChange = {
                    onEmailChanged(it)
                    emailError = checkEmailError()
                },
                singleLine = true,
                label = {
                    Text(text = "Email")
                },
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.email), contentDescription = "email")
                },
                isError = emailError && showError,
                supportingText = if (emailError && showError) {
                    { Text("Please enter a valid email address") }
                } else {
                    null
                },
                keyboardActions = KeyboardActions(onDone = { submit() }),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(modifier = Modifier.weight(1f))
            RoundedRectButton(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("NextButton"),
                imeAnimation = animateImeDp(label = "ForgotPasswordView_NextButton_imeDP"),
                loading = loading,
            ) {
                Text("Next")
            }
        }
    }
}
