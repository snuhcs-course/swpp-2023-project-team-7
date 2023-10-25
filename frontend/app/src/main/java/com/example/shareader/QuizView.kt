package com.example.shareader

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shareader.ui.theme.SHAReaderTheme
import com.example.shareader.ui.viewmodels.QuizViewModel

@Composable
fun QuizView(
    navController: NavController = NavController(LocalContext.current),
    quizViewModel: QuizViewModel = viewModel()
) {
    var quizIdx by remember { mutableIntStateOf(0) }
    var answerVisible by remember { mutableStateOf(false) }
    val quizList by quizViewModel.quizList.collectAsState()
    val quizSize by quizViewModel.quizSize.collectAsState()
    val quizLoadState by quizViewModel.quizLoadState.collectAsState()

    LaunchedEffect(Unit) {
        quizViewModel.loadQuiz()
    }

    val currentQuestion = if (quizList.size > quizIdx) quizList[quizIdx].question else null
    val currentAnswer = if (quizList.size > quizIdx) quizList[quizIdx].answer else null
    val currentVisible = currentQuestion != null && currentAnswer != null

    SHAReaderTheme {
        Scaffold(topBar = {
            TopBar(name = "Quiz", navController, currentQuestion, currentAnswer)
        }) { innerPadding ->
            AnimatedContent(targetState = currentVisible, label = "QuizView.Content") {
                when (it) {
                    true -> Column(
                        modifier = Modifier.padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        QuizProgressIndicator(quizIdx + 1, quizSize)
                        QuestionTitle(question = currentQuestion ?: "")
                        WriteAnswer(modifier = Modifier.weight(1f))
                        GeneratedAnswer(
                            modifier = Modifier.weight(1f),
                            answerVisible = answerVisible,
                            answer = quizList[quizIdx].answer
                        )
                        BottomBar(answerVisible, onPrevClicked = {
                            if (!answerVisible && quizIdx > 0) {
                                quizIdx--
                            }
                            answerVisible = !answerVisible
                        }, onNextClicked = {
                            if (answerVisible && quizIdx < 10) {
                                quizIdx++
                            }
                            answerVisible = !answerVisible
                        })
                    }

                    false -> Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // TODO: more user-freindly loading
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(name: String, navController: NavController, question: String?, answer: String?) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }
    TopAppBar(title = { Text(text = name) }, navigationIcon = {
        IconButton(onClick = {
            val intent = Intent(context, ReaderActivity::class.java)
            activityLauncher.launch(intent)
        }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack, contentDescription = "Arrow Back"
            )
        }
    }, actions = {
        IconButton(onClick = {
            if (question != null && answer != null) {
                expanded = !expanded
            }
        }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Menu",
                tint = Color.Black
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Report This Quiz") },
                onClick = { navController.navigate("report/${question}/${answer}") })
            DropdownMenuItem(text = { Text("Regenerate Quiz") },
                onClick = { Toast.makeText(context, "Regenerate", Toast.LENGTH_SHORT).show() })
        }

    })

}


@Composable
fun BottomBar(answerVisible: Boolean, onPrevClicked: () -> Unit, onNextClicked: () -> Unit) {

    Row(
        modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .height(48.dp)
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(12.dp),
            onClick = { onPrevClicked() },
        ) {
            Text(text = "Previous")
        }
        Button(
            modifier = Modifier
                .height(48.dp)
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(12.dp),
            onClick = { onNextClicked() },
        ) {
            Text(text = if (answerVisible) "Next" else "See Answer")
        }

    }


}

@Composable
fun QuizProgressIndicator(idx: Int, size: Int) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "$idx / $size")
        LinearProgressIndicator(
            progress = idx.toFloat() / size, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuestionTitle(modifier: Modifier = Modifier, question: String) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteAnswer(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(value = text,
        onValueChange = { text = it },
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        label = { Text("Answer") })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedAnswer(modifier: Modifier = Modifier, answerVisible: Boolean, answer: String) {
    val alpha = if (answerVisible) 1f else 0f
    OutlinedTextField(
        value = answer,
        onValueChange = {},
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .alpha(alpha),
        label = { Text("Answer") },
        readOnly = true
    )
}