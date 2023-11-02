package com.example.readability.ui.screens.viewer

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.readability.ui.theme.ReadabilityTheme

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun ReportViewPreview() {
    ReportView(navController = NavController(LocalContext.current), question = "Question", answer = "Answer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportView(navController: NavController, question: String, answer: String) {
    ReadabilityTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Report Quiz") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Arrow Back"
                            )
                        }
                    })
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                Column (
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ReportTitle()
                    ReportQuestion(question, answer)
                    ReportSelection()
                }
                SubmitButton(navController)
            }
        }
    }
}

@Composable
fun ReportTitle() {
    Text(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .fillMaxWidth(),
        text = "Why are you reporting this quiz?",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportQuestion(question: String, answer: String) {

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Question:",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = question,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Generated Answer:",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

    }
}

@Composable
fun ReportSelection(modifier: Modifier = Modifier) {
    val radioOptions = listOf(
        "It is harmful / unsafe.",
        "It isn't true.",
        "It isn't helpful to understand content.",
        "Other reasons..."
    )
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1]) }
    Column (modifier = modifier) {
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
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                        .weight(1f)
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
fun SubmitButton(navController: NavController) {
    val context = LocalContext.current
    Button(
        onClick = {
            Toast.makeText(context, "Thank you for the feedback!", Toast.LENGTH_SHORT).show()
            navController.navigate("quiz")
        },
        modifier = Modifier
            .padding(16.dp)
            .height(48.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Submit Feedback"
        )
    }


}
