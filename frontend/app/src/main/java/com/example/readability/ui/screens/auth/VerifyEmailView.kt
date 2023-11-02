package com.example.readability.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.ui.theme.ReadabilityTheme

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
    onNavigateBookList: () -> Unit = {},
    onNavigateResetPassword: () -> Unit = {},
    onVerificationCodeResent: () -> Unit = {},
) {
    var verificationCode by remember { mutableStateOf("") }
    Scaffold(
        modifier = Modifier
            .imePadding()
            .systemBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(title = {
                Text(
                    "Verify Email",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Arrow Back"
                        )
                    }
                })
        }) { innerPadding ->
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
                    text = "Enter the code we sent to\n$email",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = { onVerificationCodeResent() }) {
                    Text(text = "Resend Code")
                }
            }
            Verification(verificationCode) {
                verificationCode = it
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (fromSignUp) {
                        onNavigateBookList()
                    } else {
                        onNavigateResetPassword()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next")

            }
        }
    }

}

@Composable
fun Verification(verificationCode: String, onVerificationCodeChanged: (String) -> Unit = {}) {
    OutlinedTextField(
        value = verificationCode,
        onValueChange = { onVerificationCodeChanged(it) },
        singleLine = true,
        label = {
            Text(text = "Verification Code")
        },
        leadingIcon = {
            Icon(painter = painterResource(id = R.drawable.key), contentDescription = "key")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )

}