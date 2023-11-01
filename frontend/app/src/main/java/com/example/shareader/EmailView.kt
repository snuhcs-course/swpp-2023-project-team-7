package com.example.shareader

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailView(navController: NavController ){
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight()
    ){
        TopAppBar(
            title = { Text(text = "Continue with email", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = { val intent = Intent(context, LaunchActivity::class.java)
                    activityLauncher.launch(intent)  }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }
            })
        var text by remember { mutableStateOf("") }


        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            OutlinedTextField(
                value = text,
                onValueChange = {text = it},
                singleLine = true,
                label = {
                    Text(text = "Email")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Email, contentDescription = "delete", tint = Color.Black)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )

            Text("Sign Up", modifier = Modifier
                .size(width = 200.dp, height = 30.dp)
                .clickable { navController.navigate("sign_up") }
                .padding(top = 10.dp),
                color = Color(0xFF29628d),
                textAlign = TextAlign.Center,
                )
            Text("Forgot your password?", modifier = Modifier
                .size(width = 200.dp, height = 30.dp)
                .clickable { navController.navigate("pwd") }
                .padding(top = 10.dp),
                color = Color(0xFF29628d),
                textAlign = TextAlign.Center,
            )

        }

        Button(onClick = { if(text.isEmpty()){ Toast.makeText(context, "Invalid Email Address", Toast.LENGTH_SHORT).show()
            }else{navController.navigate("sign_in/${text}")}  },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next")
        }
//        InfoImage()
//        WriteEmail()
//        NextButton(navController)


    }
}

@Composable
fun InfoImage(){
    Image(
        painterResource(R.drawable.info_image),
        contentDescription = "",
        contentScale = ContentScale.Crop,
        modifier = Modifier.height(280.dp).padding(15.dp)
    )
}

@Composable
fun WriteEmail (modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = {text = it},
        singleLine = true,
        label = {
            Text(text = "Email")
        },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Email, contentDescription = "delete", tint = Color.Black)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    )
}

@Composable
fun NextButton(navController: NavController){
    Button(onClick = { navController.navigate("sign_in")  },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Next")

    }
}
