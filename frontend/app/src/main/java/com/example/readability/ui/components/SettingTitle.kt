package com.example.readability.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.readability.ui.theme.Gabarito

@Composable
fun SettingTitle(
    modifier: Modifier = Modifier, text: String
) {
    Text(
        modifier = modifier.padding(16.dp, 8.dp).fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = Gabarito, fontWeight = FontWeight.Medium
        )
    )
    HorizontalDivider(
        modifier = Modifier.padding(16.dp, 0.dp),
        thickness = Dp.Hairline,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}