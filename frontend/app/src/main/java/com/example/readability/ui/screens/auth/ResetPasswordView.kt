package com.example.readability.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.example.readability.ui.animation.animateIMEDp
import com.example.readability.ui.components.PasswordTextField
import com.example.readability.ui.theme.ReadabilityTheme

@Composable
@Preview(device = "id:pixel_5")
fun ResetPasswordPreview() {
    ReadabilityTheme {
        ResetPasswordView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordView(
    onBack: () -> Unit = {},
    onNavigateEmail: (String) -> Unit = {},
) {

    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    val imeDp by animateIMEDp("EmailView_IMEDp")

    Scaffold(modifier = Modifier
        .imePadding()
        .systemBarsPadding()
        .navigationBarsPadding(),
        topBar = {
            TopAppBar(title = { Text("Reset Password") }, navigationIcon = {
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = "New Password",
                password = newPassword,
                onPasswordChanged = { newPassword = it },
                supportingText = "At least 8 characters including a letter and a number"
            )
            PasswordTextField(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
                label = "Repeat Password",
                password = repeatPassword,
                onPasswordChanged = { repeatPassword = it })
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onNavigateEmail(newPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16 * imeDp)
                    .height(48.dp),
                shape = RoundedCornerShape(12 * imeDp)
            ) {
                Text("Reset Password")
            }
        }
    }
}
