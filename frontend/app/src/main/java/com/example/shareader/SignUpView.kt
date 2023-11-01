package com.example.shareader

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpView(navController: NavController){
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
            title = { Text("Sign up", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("email")}) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }
            })
//        InfoImage()
        var text by remember { mutableStateOf("") }
        OutlinedTextField(
            value = text,
            onValueChange = {text = it},
            singleLine = true,
            label = {
                Text(text = "Email")
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Email, contentDescription = "delete")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        )
        Username()
        WritePassword("Password")
        Text("At least 8 characters including a letter and a number", fontSize = 12.sp, textAlign = TextAlign.Start, modifier = Modifier.padding(top = 5.dp))
        WritePassword("Repeat Password")
        Spacer(modifier = Modifier.weight(1f))
        val fromSignUp = true
        Button(onClick = { if(text.isEmpty()){ Toast.makeText(context, "Invalid Email Address", Toast.LENGTH_SHORT).show()
        }else{navController.navigate("verify/${text}/${fromSignUp}")}  },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign up")

        }


    }
}

@Composable
fun Username () {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = {text = it},
        singleLine = true,
        label = {
            Text(text = "Username")
        },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Person, contentDescription = "delete")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 10.dp),
    )

}

@Composable
fun WriteEmail () {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = {text = it},
        singleLine = true,
        label = {
            Text(text = "Email",color = Color.Gray.copy(alpha = 0.7f))
        },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Email, contentDescription = "delete")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
            )

}

