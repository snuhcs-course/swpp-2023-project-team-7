package com.example.shareader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SummaryView() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Previous Story", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Alice was beginning to get very tired of sitting by her sister on the bank, " +
                        "and of having nothing to do: once or twice she had peeped into the book her " +
                        "sister was reading, but it had no pictures or conversations in it, 'and what is the " +
                        "use of a book, thought Alice 'without pictures or conversation?'" +
                        "\n\n So she was considering in her own mind (as well as she could, for the hot day" +
                        " made her feel very sleepy and stupid), whether the pleasure of making a daisy-chain" +
                        " would be worth the trouble of getting up and picking the daisies, when suddenly" +
                        " a White Rabbit with pink eyes ran close by her.",
                fontSize = 22.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}