package com.example.readability.ui.screens.auth

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.ui.animation.animateImeDp
import com.example.readability.ui.components.PasswordTextField
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    onNavigateBookList: () -> Unit = {},
) {
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val scope = rememberCoroutineScope()

    val submit = {
        if (password.isEmpty()) {
            passwordError = "Please enter a password"
        } else {
            focusManager.clearFocus()
            loading = true
            scope.launch {
                onPasswordSubmitted(password).onSuccess {
                    withContext(Dispatchers.Main) { onNavigateBookList() }
                    Toast.makeText(
                        context,
                        "Welcome back!",
                        Toast.LENGTH_SHORT,
                    ).show()
                }.onFailure {
                    loading = false
                    passwordError = it.message ?: ""
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Sign in", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Arrow Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            passwordFocusRequester.requestFocus()
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("EmailTextField"),
                value = email,
                onValueChange = {},
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = false,
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(passwordFocusRequester)
                    .testTag("PasswordTextField"),
                password = password,
                onPasswordChanged = { password = it },
                label = "Password",
                keyboardActions = KeyboardActions(onDone = {
                    submit()
                }),
                isError = passwordError != null,
                supportingText = passwordError,
            )
            Spacer(modifier = Modifier.weight(1f))
            RoundedRectButton(
                onClick = { submit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("SignInButton"),
                imeAnimation = animateImeDp(
                    label = "AuthView_SignInView_imeAnimation",
                ),
                loading = loading,
            ) {
                Text("Sign in")
            }
        }
    }
}
