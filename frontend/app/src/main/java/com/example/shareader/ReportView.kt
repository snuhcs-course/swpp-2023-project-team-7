package com.example.shareader

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportView(navController: NavController) {
    val context = LocalContext.current
//    val activityLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { _ ->
//    }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding()
    ){
        TopAppBar(
            title = { Text(text = "ReportQuiz") },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("quiz") }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Arrow Back"
                    )
                }
            })
        ReportTitle()
        ReportQuestion()
        ReportSelection()
        SubmitButton(navController)
    }

}

@Composable
fun ReportTitle(){
    Text(text = "Why are you reporting this quiz?", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportQuestion(){

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
    ){
        Text(text = "Question:", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        Text(text = "examplequestions",modifier = Modifier.padding(horizontal = 16.dp), textAlign = TextAlign.Center, )
        Text(text = " ")
        Text(text = "Generated Answer:", fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start,modifier = Modifier.padding(horizontal = 16.dp))
        Text(text = "exampleanswers",modifier = Modifier.padding(horizontal = 16.dp,  vertical = 8.dp), textAlign = TextAlign.Center)

    }
}

@Composable
fun ReportSelection(){
    val radioOptions = listOf("It is harmful/unsafe.", "It isn;t true.", "It isn't helpful to understand content.", "Other reasons...")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1] ) }
    Column {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = {
                            onOptionSelected(text)
                        }
                    )
            ) {

                Text(
                    text = text,
//                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically).weight(1f)
                )
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = { onOptionSelected(text) }
                )
            }
        }
    }
}
@Composable
fun SubmitButton(navController: NavController){
    val context = LocalContext.current
    Button(
        onClick = {
            Toast.makeText(context, "Thankyou for the feedback!", Toast.LENGTH_SHORT).show()
            navController.navigate("quiz")
        },
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
        ) {
            Text(
                text = "Submit Feedback",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }




}
