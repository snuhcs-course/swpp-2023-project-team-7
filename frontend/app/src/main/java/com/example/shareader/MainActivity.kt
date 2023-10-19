package com.example.shareader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shareader.ui.theme.SHAReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SHAReaderTheme {
                BookListView()
            }
        }
    }
}

@Composable
fun HomeView() {
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding()
        ){
            Button(
                onClick = {
                    val intent = Intent(context, ReaderActivity::class.java)
                    activityLauncher.launch(intent)
                },
                modifier = Modifier
                    .width(200.dp)
            ) {
                Text(
                    text = "E-Book Reader",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = {
                    val intent = Intent(context, SummaryActivity::class.java)
                    activityLauncher.launch(intent)
                },
                modifier = Modifier
                    .width(200.dp)
            ) {
                Text(
                    text = "Summary View",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = {
                    val intent = Intent(context, QuizActivity::class.java)
                    activityLauncher.launch(intent)
                },
                modifier = Modifier
                    .width(200.dp)
            ) {
                Text(
                    text = "Quiz View",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}