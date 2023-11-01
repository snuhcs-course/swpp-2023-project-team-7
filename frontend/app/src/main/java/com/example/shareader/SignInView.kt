package com.example.shareader

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInView(navController: NavController, email:String){
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight()
    ){
        TopAppBar(
            title = { Text("Sign in", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("email")}) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }
            })
//        InfoImage()
        WrittenEmail(email)
        WritePassword("Password")
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { val intent = Intent(context, MainActivity::class.java)
            activityLauncher.launch(intent) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign in")

        }


    }
}
@Composable
fun WrittenEmail (email:String) {
//    var text by remember { mutableStateOf("") }
//    text = email
    OutlinedTextField(
        value = email,
        onValueChange = {},
        singleLine = true,
        label = {
            Text(text = "Email",color = Color.Gray.copy(alpha = 0.7f))
        },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Email, contentDescription = "delete", tint = Color.Gray.copy(alpha = 0.7f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        readOnly = true,
        textStyle = TextStyle(color = Color.Gray.copy(alpha = 0.7f)),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedLabelColor = Color.Gray.copy(alpha = 0.7f),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.7f),
            focusedLabelColor = Color.Gray.copy(alpha = 0.7f),
            focusedBorderColor = Color.Gray.copy(alpha = 0.7f),

        )
    )
}
@Composable
fun WritePassword (label: String) {
    var text by remember { mutableStateOf("") }
    var passwordVisibility: Boolean by remember { mutableStateOf(false) }
    OutlinedTextField(
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        value = text,
        onValueChange = {text = it},
        singleLine = true,
        label = {
            Text(text = label)
        },
        trailingIcon = {
            IconButton(onClick = {
                passwordVisibility = !passwordVisibility
            }){
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "visible")
            }
        },

        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}

