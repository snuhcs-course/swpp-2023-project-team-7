package com.example.readability.ui.screens.viewer

import android.graphics.Typeface
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.readability.data.viewer.ViewerStyle

@Composable
fun dpToSp(dp: Dp): TextUnit = with(LocalDensity.current) { dp.toSp() }

@Composable
fun pxToSp(px: Float): TextUnit = with(LocalDensity.current) { px.toSp() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryView(
    summary: String,
    viewerStyle: ViewerStyle,
    typeface: Typeface?,
    referenceLineHeight: Float,
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Previous Story") }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Arrow Back")
                }
            })
        },
    ) { innerPadding ->
        Text(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState(),
                )
                .padding(16.dp),
            text = summary,
            style = TextStyle(
                fontSize = dpToSp(dp = viewerStyle.textSize.dp),
                lineHeight = pxToSp(px = (viewerStyle.lineHeight * referenceLineHeight * viewerStyle.textSize / 16f)),
                fontFamily = typeface?.let { FontFamily(it) },
                letterSpacing = viewerStyle.letterSpacing.em,
            ),
        )
    }
}
