package com.example.readability.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.readability.R

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    password: String,
    label: String,
    onPasswordChanged: (String) -> Unit = {},
    supportingText: String? = null,
    isError: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var passwordVisibility: Boolean by remember { mutableStateOf(false) }

    OutlinedTextField(modifier = modifier,
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        value = password,
        onValueChange = { onPasswordChanged(it) },
        singleLine = true,
        label = {
            Text(text = label)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.password), contentDescription = "Password"
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                passwordVisibility = !passwordVisibility
            }) {
                if (passwordVisibility) Icon(
                    painter = painterResource(id = R.drawable.eye), contentDescription = "visible"
                ) else Icon(
                    painter = painterResource(id = R.drawable.closed_eye),
                    contentDescription = "not visible"
                )
            }
        },
        supportingText = supportingText?.let {
            { Text(text = it) }
        },
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        isError = isError
    )
}