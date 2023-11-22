package com.example.readability.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.ui.animation.animateImeDp
import com.example.readability.ui.components.PasswordTextField
import com.example.readability.ui.components.RoundedRectButton
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun PasswordCheckViewPreview() {
    ReadabilityTheme {
        PasswordCheckView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordCheckView(
    onBack: () -> Unit = {},
    onPasswordSubmitted: suspend () -> Result<Unit> = { Result.success(Unit) },
    onNavigateAccount: () -> Unit = {},
) {
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val submit = {
        focusManager.clearFocus()
        scope.launch {
            loading = true
            onPasswordSubmitted().onSuccess {
                onNavigateAccount()
            }.onFailure {
                loading = false
                passwordError = it.message ?: "Unknown error occurred. Please try again."
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(title = { Text(text = "Account") }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            })
        },
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            passwordFocusRequester.requestFocus()
        }
        Column(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(64.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        modifier = Modifier.size(96.dp),
                        painter = painterResource(id = R.drawable.lock_simple_thin),
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Verification Needed",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Because you're accessing sensitive info,\nyou need to verify your password.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.secondary),
                        textAlign = TextAlign.Center,
                    )
                }
                PasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester)
                        .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    password = password,
                    label = "Password",
                    isError = passwordError.isNotEmpty(),
                    onPasswordChanged = {
                        password = it
                        passwordError = ""
                    },
                    supportingText = passwordError,
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
            }
            RoundedRectButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { submit() },
                imeAnimation = animateImeDp(label = "SettingsScreen.PasswordCheckView.NextButton"),
                loading = loading,
            ) {
                Text(text = "Next")
            }
        }
    }
}
