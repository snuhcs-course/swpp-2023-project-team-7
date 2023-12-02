package com.example.readability.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.ui.theme.GabaritoMedium
import com.example.readability.ui.theme.ReadabilityTheme

@Composable
@Preview
fun SettingTitlePreview() {
    ReadabilityTheme {
        Surface(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            SettingTitle(text = "General")
        }
    }
}

@Composable
fun SettingTitle(modifier: Modifier = Modifier, text: String) {
    Column(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.padding(16.dp, 8.dp)
                .fillMaxWidth(),
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = GabaritoMedium,
                fontWeight = FontWeight.Medium,
            ),
        )
        HorizontalDivider(
            modifier = Modifier.padding(16.dp, 0.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
