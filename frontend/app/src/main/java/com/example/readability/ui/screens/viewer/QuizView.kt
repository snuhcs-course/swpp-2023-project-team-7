package com.example.readability.ui.screens.viewer

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.readability.ui.models.Quiz
import com.example.readability.ui.models.QuizLoadState
import com.example.readability.ui.theme.ReadabilityTheme

enum class QuizState {
    QUESTION, ANSWER, FIRST_QUESTION, LAST_ANSWER, UNLOADED_ANSWER,
}

@Composable
fun QuizView(
    quizList: List<Quiz>,
    quizSize: Int,
    quizLoadState: QuizLoadState,
    onBack: () -> Unit,
    onNavigateReport: (Int) -> Unit
) {
    var quizIdx by remember { mutableIntStateOf(0) }
    var answerVisible by remember { mutableStateOf(false) }

    val currentQuestion = if (quizList.size > quizIdx) quizList[quizIdx].question else null
    val currentAnswer = if (quizList.size > quizIdx) quizList[quizIdx].answer else null
    val currentVisible = currentQuestion != null && currentAnswer != null

    val quizState = when (answerVisible) {
        true -> when (quizIdx) {
            quizSize - 1 -> QuizState.LAST_ANSWER
            else -> when (quizList.size) {
                in 0..(quizIdx + 1) -> QuizState.UNLOADED_ANSWER
                else -> QuizState.ANSWER
            }
        }

        false -> when (quizIdx) {
            0 -> QuizState.FIRST_QUESTION
            else -> QuizState.QUESTION
        }
    }

    ReadabilityTheme {
        Scaffold(topBar = {
            TopBar(name = "Quiz", onBack, onNavigateReport = {
                onNavigateReport(quizIdx)
            }, currentQuestion, currentAnswer)
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
                        BottomBar(
                            quizState = quizState,
                            onPrevClicked = {
                                if (!answerVisible && quizIdx > 0) {
                                    quizIdx--
                                }
                                if (answerVisible) {
                                    answerVisible = false
                                }
                            },
                            onNextClicked = {
                                if (quizState == QuizState.LAST_ANSWER) {
                                    onBack()
                                    return@BottomBar
                                }
                                if (answerVisible && quizIdx < quizList.size - 1) {
                                    quizIdx++
                                }
                                answerVisible = !answerVisible
                            },
                        )
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
fun TopBar(
    name: String,
    onBack: () -> Unit = {},
    onNavigateReport: () -> Unit,
    question: String?,
    answer: String?
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(title = { Text(text = name) }, navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Arrow Back"
            )
        }
    }, actions = {
        IconButton(onClick = {
            if (question != null && answer != null) {
                expanded = !expanded
            }
        }) {
            Icon(
                Icons.Filled.MoreVert, contentDescription = "Menu", tint = Color.Black
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Report This Quiz") }, onClick = onNavigateReport
            )
            DropdownMenuItem(text = { Text("Regenerate Quiz") },
                onClick = { Toast.makeText(context, "Regenerate", Toast.LENGTH_SHORT).show() })
        }

    })

}


@Composable
fun BottomBar(
    quizState: QuizState,
    onPrevClicked: () -> Unit,
    onNextClicked: () -> Unit,
) {

    Row(
        modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = quizState == QuizState.FIRST_QUESTION,
                label = "QuizView.BottomBar.PrevButton"
            ) {
                when (it) {
                    true -> Spacer(modifier = Modifier.fillMaxWidth())
                    else -> FilledTonalButton(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onPrevClicked() },
                    ) {
                        Text(
                            text = when (quizState) {
                                QuizState.QUESTION -> "Previous"
                                else -> "Solve Again"
                            }
                        )
                    }
                }
            }
        }
        Button(
            modifier = Modifier
                .height(48.dp)
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(12.dp),
            onClick = { onNextClicked() },
            enabled = when (quizState) {
                QuizState.UNLOADED_ANSWER -> false
                else -> true
            }
        ) {
            AnimatedContent(targetState = quizState, label = "QuizView.BottomBar.NextButton") {
                when (it) {
                    QuizState.UNLOADED_ANSWER -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp
                    )

                    else -> Text(
                        text = when (quizState) {
                            QuizState.QUESTION -> "See Answer"
                            QuizState.FIRST_QUESTION -> "See Answer"
                            QuizState.LAST_ANSWER -> "Finish"
                            else -> "Next"
                        }
                    )
                }

            }
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
            progress = { idx.toFloat() / size }, modifier = Modifier.fillMaxWidth()
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