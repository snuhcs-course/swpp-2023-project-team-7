package com.snu.readability.ui.screens.viewer

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snu.readability.ui.components.CircularProgressIndicatorInButton
import com.snu.readability.ui.components.RoundedRectButton
import com.snu.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch

private val REPORT_REASONS = listOf(
    "It is harmful / unsafe.",
    "It isn't true.",
    "It isn't helpful to understand content.",
    "Other reasons...",
)

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun ReportViewPreview() {
    QuizReportView(
        question = "Question",
        answer = "Answer",
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizReportView(
    question: String,
    answer: String,
    onBack: () -> Unit = {},
    onReport: suspend (String) -> Result<Unit> = { Result.success(Unit) },
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var reasonIdx by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    ReadabilityTheme {
        Scaffold(topBar = {
            TopAppBar(title = { Text(text = "Report Quiz") }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Arrow Back",
                    )
                }
            })
        }) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    ReportTitle()
                    ReportQuestion(question, answer)
                    ReportSelection(value = reasonIdx, onChange = { reasonIdx = it })
                }
                SubmitButton(onClick = {
                    loading = true
                    scope.launch {
                        onReport(REPORT_REASONS[reasonIdx]).onSuccess {
                            Toast.makeText(
                                context,
                                "Thank you for the feedback! " +
                                    "We’ll continue to improve our service based on your opinion.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            onBack()
                        }.onFailure {
                            Toast.makeText(
                                context,
                                "Unknown error occurred while sending feedback. Please try again.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            loading = false
                        }
                    }
                }, loading = loading)
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
        textAlign = TextAlign.Center,
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
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = "Question:",
            style = MaterialTheme.typography.labelLarge,
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
            style = MaterialTheme.typography.labelLarge,
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
fun ReportSelection(modifier: Modifier = Modifier, value: Int, onChange: (Int) -> Unit = {}) {
    Column(modifier = modifier) {
        REPORT_REASONS.forEachIndexed { index, text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = (index == value), onClick = {
                        onChange(index)
                    })
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = text,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                )
                RadioButton(selected = (index == value), onClick = { onChange(index) })
            }
        }
    }
}

@Composable
fun SubmitButton(onClick: () -> Unit = {}, loading: Boolean) {
    RoundedRectButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick,
        enabled = !loading,
    ) {
        if (loading) CircularProgressIndicatorInButton() else Text(text = "Submit Feedback")
    }
}
