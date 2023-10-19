package com.example.shareader

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun QuizView(navController: NavController) {
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {


        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding()
        ){
            TopBar(name = "Quiz", navController)
            QuizProgressIndicator()
            getQuestion()
            writeAnswer()
            getAnswer()
            BottomBar()
        }

    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(name: String , navController: NavController) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
//    val activityLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { _ ->
//    }
    TopAppBar(
        title = { Text(text = name) },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Arrow Back"
                )
            }
        },
        actions = {
            IconButton(onClick = {expanded = !expanded}) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Report This Quiz") },
                    onClick = { navController.navigate("report")}
                )
                DropdownMenuItem(
                    text = { Text("Regenerate Quiz") },
                    onClick = { Toast.makeText(context, "Regenerate", Toast.LENGTH_SHORT).show() }
                )
            }

        }
    )

}



@Composable
fun BottomBar(){
    Row(
        modifier = Modifier.padding()

    ){
        Button(
            onClick = {

            },
            modifier = Modifier
                .width(180.dp)
                .padding(20.dp)
        ) {
            Text(
                text = "Previous",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = {

            },
            modifier = Modifier
                .width(180.dp)
                .padding(20.dp)
        ) {
            Text(
                text = "Next",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

    }


}

@Composable
fun QuizProgressIndicator(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "1/10")
        LinearProgressIndicator(progress = 0.7f)
    }
}

@Composable
fun getQuestion(){
    Text(text = "Question", fontSize = 24.sp, modifier = Modifier.padding(24.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun writeAnswer(){
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(180.dp),
        label = { Text("Answer") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getAnswer(){

    OutlinedTextField(
        value = "Generated Answer",
        onValueChange = {},
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(180.dp),
        label = { Text("Answer") },
        readOnly = true, // Make it non-editable
    )

}
//@Composable
//fun QuizView(content:@Composable()){
//    Scaffold (topBar = {
//        TopAppBar(title = { Text(text = "Quiz") },
//            navigationIcon = {Icon(imageVector = Icons.Default.ArrowBack)})},){
//        content()
//    }}
//    })
//
//}