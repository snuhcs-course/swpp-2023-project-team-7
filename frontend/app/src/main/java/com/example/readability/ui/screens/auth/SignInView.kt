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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.ui.theme.ReadabilityTheme
import com.example.readability.ui.components.PasswordTextField

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
    onPasswordSubmitted: (String) -> Unit = {},
    onNavigateForgotPassword: (String) -> Unit = {}
) {
    var password by remember { mutableStateOf("") }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
            PasswordTextField(password = password, onPasswordChanged = { password = it }, label = "Password")
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onPasswordSubmitted(password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign in")
            }
        }
    }


}

