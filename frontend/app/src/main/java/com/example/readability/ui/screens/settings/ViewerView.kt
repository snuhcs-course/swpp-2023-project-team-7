package com.example.readability.ui.screens.settings

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.ui.PageSplitter
import com.example.readability.ui.models.BookModel
import com.example.readability.ui.models.SettingModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerView(
    onBack: () -> Unit = {},
) {
    val sampleText by SettingModel.getInstance().sampleText.collectAsState()
    val textSize by BookModel.getInstance().pageSplitter.textSize.collectAsState()
    val lineHeight by BookModel.getInstance().pageSplitter.lineHeight.collectAsState()
    val letterSpacing by BookModel.getInstance().pageSplitter.letterSpacing.collectAsState()
    val paragraphSpacing by BookModel.getInstance().pageSplitter.paragraphSpacing.collectAsState()
    val fontFamily by BookModel.getInstance().pageSplitter.fontFamily.collectAsState()

    val width = Resources.getSystem().displayMetrics.widthPixels
    val padding = with(LocalDensity.current) { 16.dp.toPx() }

    val dynamicLayout =
        remember(sampleText, textSize, lineHeight, letterSpacing, paragraphSpacing, fontFamily) {
            val textPaint = BookModel.getInstance().pageSplitter.buildTextPaint()
            BookModel.getInstance().pageSplitter.buildDynamicLayout(
                sampleText, textPaint, (width - padding * 2).toInt()
            )
        }

    var fontFamilyExpanded by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .clipToBounds()
        ) {
            drawIntoCanvas {
                dynamicLayout.draw(it.nativeCanvas)
            }
        }
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(top = 32.dp),
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Viewer Settings",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Font Family", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    ExposedDropdownMenuBox(expanded = fontFamilyExpanded,
                        onExpandedChange = { fontFamilyExpanded = it }) {
                        OutlinedTextField(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .menuAnchor(),
                            readOnly = true,
                            value = fontFamily,
                            onValueChange = {
                                BookModel.getInstance().pageSplitter.fontFamily.value = it
                            },
                            label = { Text("Font Family") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = fontFamilyExpanded
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(expanded = fontFamilyExpanded, onDismissRequest = {
                            fontFamilyExpanded = false
                        }) {
                            PageSplitter.fontMap.forEach { selectionOption ->
                                DropdownMenuItem(onClick = {
                                    BookModel.getInstance().pageSplitter.fontFamily.value =
                                        selectionOption.key
                                    fontFamilyExpanded = false
                                }, text = {
                                    Text(text = selectionOption.key)
                                })
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Text Size", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.textSize.value += 1
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Plus"
                        )
                    }
                    Text(
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                        text = textSize.roundToInt().toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.textSize.value -= 1
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.minus),
                            contentDescription = "Minus"
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Line Height", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.lineHeight.value += 0.1f
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Plus"
                        )
                    }
                    Text(
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                        text = "${(lineHeight * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.lineHeight.value -= 0.1f
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.minus),
                            contentDescription = "Minus"
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Letter Spacing", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.letterSpacing.value += 0.01f
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Plus"
                        )
                    }
                    Text(
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                        text = ((letterSpacing * 100).roundToInt() / 100f).toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.letterSpacing.value -= 0.01f
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.minus),
                            contentDescription = "Minus"
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Paragraph Spacing", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.paragraphSpacing.value += 0.1f
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "Plus"
                        )
                    }
                    Text(
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp),
                        text = "${(paragraphSpacing * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = {
                        BookModel.getInstance().pageSplitter.paragraphSpacing.value -= 0.1f
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.minus),
                            contentDescription = "Minus"
                        )
                    }
                }
            }
        }
    }
}