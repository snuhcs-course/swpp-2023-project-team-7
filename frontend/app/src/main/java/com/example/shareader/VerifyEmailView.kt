package com.example.shareader

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailView(navController: NavController, email:String, fromSignUp:Boolean){
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
            title = { Text("Verify Email", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = { if(fromSignUp){
                    navController.navigate("sign_up")
                }else{
                    navController.navigate("pwd")
                }
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }
            })
        Text(text = "Enter the code we sent to", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 40.dp))
        Text(text = "$email.", style = MaterialTheme.typography.headlineSmall)
        Text("Resend code",
            modifier = Modifier
                .clickable {Toast.makeText(context, "Verification Code Resent!", Toast.LENGTH_SHORT).show()}
                .padding(10.dp)
            , color = Color(0xFF29628d), textAlign = TextAlign.Center)
        Verification()
        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = {
            if(fromSignUp){
                Toast.makeText(context, "Sign Up is Successful!\nLog in with your email.", Toast.LENGTH_SHORT).show()
                navController.navigate("email")
            }else{
                navController.navigate("reset")
            }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next")

        }


    }
}

@Composable
fun Verification() {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = {text = it},
        singleLine = true,
        label = {
            Text(text = "Verification Code")
        },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "key")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    )

}