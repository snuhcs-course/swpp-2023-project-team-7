package com.snu.readability.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snu.readability.LocalSnackbarHost
import com.snu.readability.R
import com.snu.readability.ui.animation.animateImeDp
import com.snu.readability.ui.components.RoundedRectButton
import com.snu.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@Preview(device = "id:pixel_5")
fun VerifyEmailPreview() {
    ReadabilityTheme {
        VerifyEmailView(email = "test@example.com", fromSignUp = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailView(
    email: String,
    fromSignUp: Boolean,
    onBack: () -> Unit = {},
    onVerificationCodeSubmitted: suspend (String) -> Result<Unit> = { Result.success(Unit) },
    onNavigateBookList: () -> Unit = {},
    onNavigateResetPassword: () -> Unit = {},
    onVerificationCodeResent: () -> Unit = {},
) {
    var verificationCode by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val verificationCodeFocusRequester = remember { FocusRequester() }

    val snackbarHost = LocalSnackbarHost.current

    val submit = {
        loading = true
        scope.launch {
            onVerificationCodeSubmitted(verificationCode).onSuccess {
                withContext(Dispatchers.Main) {
                    if (fromSignUp) onNavigateBookList() else onNavigateResetPassword()
                }
            }.onFailure {
                loading = false
                snackbarHost.showSnackbar(it.toString())
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .systemBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(title = {
                Text(
                    "Verify Email",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Arrow Back",
                    )
                }
            })
        },
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            verificationCodeFocusRequester.requestFocus()
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Enter the code we sent to\n$email.",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = { onVerificationCodeResent() }) {
                    Text(text = "Resend Code")
                }
            }
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                singleLine = true,
                label = {
                    Text(text = "Verification Code")
                },
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.key), contentDescription = "key")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(verificationCodeFocusRequester)
                    .testTag("VerificationCodeTextField"),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(modifier = Modifier.weight(1f))
            RoundedRectButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("NextButton"),
                onClick = {
                    submit()
                },
                enabled = verificationCode.isNotBlank(),
                loading = loading,
                imeAnimation = animateImeDp(label = "AuthView_VerifyEmailView_imeDp"),
            ) {
                Text("Next")
            }
        }
    }
}
