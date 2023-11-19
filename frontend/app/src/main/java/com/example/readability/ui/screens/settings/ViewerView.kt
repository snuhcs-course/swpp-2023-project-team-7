package com.example.readability.ui.screens.settings

import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.data.viewer.FontDataSource
import com.example.readability.data.viewer.ViewerStyle
import com.example.readability.ui.theme.Gabarito
import com.example.readability.ui.theme.ReadabilityTheme
import com.example.readability.ui.theme.md_theme_dark_outline
import com.example.readability.ui.theme.md_theme_light_outline
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun ViewerPreview() {
    ReadabilityTheme {
        ViewerView(
            viewerStyle = ViewerStyle(),
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ViewerOptionsPreview() {
    ReadabilityTheme {
        Surface(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            ViewerOptions(viewerStyle = ViewerStyle(), onViewerStyleChanged = {})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ViewerView(
    viewerStyle: ViewerStyle,
    fontMap: Map<String, Int> = FontDataSource.fontMap,
    onDrawPage: (canvas: NativeCanvas, width: Int) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    onViewerStyleChanged: (viewerStyle: ViewerStyle) -> Unit = {},
) {
    val width = Resources.getSystem().displayMetrics.widthPixels
    val padding = with(LocalDensity.current) { 16.dp.toPx() }
    val pageHorizontalPadding =
        with(LocalDensity.current) { viewerStyle.horizontalPadding.dp.toPx() }

    val pagerState = rememberPagerState(initialPage = 0) {
        2
    }
    val pagerScope = rememberCoroutineScope()
    var maxHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Viewer Settings") }, navigationIcon = {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            // Preview
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
                    .fillMaxWidth()
                    .border(
                        1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)
                    )
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = viewerStyle.horizontalPadding.dp,
                            vertical = viewerStyle.verticalPadding.dp
                        )
                        .clipToBounds()
                ) {
                    drawIntoCanvas {
                        onDrawPage(it.nativeCanvas, (width - padding * 2 - pageHorizontalPadding * 2).roundToInt())
                    }
                }
            }
            PrimaryTabRow(
                modifier = Modifier.fillMaxWidth(), selectedTabIndex = pagerState.currentPage
            ) {
                listOf("Text", "Viewer").forEachIndexed { index, title ->
                    Tab(selected = pagerState.currentPage == index,
                        onClick = { pagerScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title, style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = Gabarito,
                                    fontWeight = FontWeight.Medium,
                                    color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        })
                }
            }
            HorizontalPager(
                modifier = if (maxHeight == 0.dp) Modifier.fillMaxWidth() else Modifier
                    .fillMaxWidth()
                    .height(maxHeight), state = pagerState, verticalAlignment = Alignment.Top
            ) {
                when (it) {
                    0 -> TextOptions(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged {
                                val height = with(density) { it.height.toDp() }
                                maxHeight = maxHeight.coerceAtLeast(height)
                            },
                        viewerStyle = viewerStyle,
                        fontMap = fontMap,
                        onViewerStyleChanged = onViewerStyleChanged
                    )

                    1 -> ViewerOptions(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged {
                                val height = with(density) { it.height.toDp() }
                                maxHeight = maxHeight.coerceAtLeast(height)
                            },
                        viewerStyle = viewerStyle,
                        onViewerStyleChanged = onViewerStyleChanged
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextOptions(
    modifier: Modifier = Modifier,
    viewerStyle: ViewerStyle,
    fontMap: Map<String, Int>,
    onViewerStyleChanged: (viewerStyle: ViewerStyle) -> Unit
) {
    var fontFamilyExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Font Family", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            ExposedDropdownMenuBox(
                expanded = fontFamilyExpanded,
                onExpandedChange = { fontFamilyExpanded = it }) {
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .menuAnchor()
                        .padding(start = 16.dp, end = 8.dp)
                        .defaultMinSize(minWidth = 128.dp)
                        .width(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = viewerStyle.fontFamily,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = fontFamilyExpanded
                    )
                }
                ExposedDropdownMenu(expanded = fontFamilyExpanded, onDismissRequest = {
                    fontFamilyExpanded = false
                }) {
                    fontMap.forEach { selectionOption ->
                        DropdownMenuItem(onClick = {
                            onViewerStyleChanged(viewerStyle.copy(fontFamily = selectionOption.key))
                            fontFamilyExpanded = false
                        }, text = {
                            Text(
                                text = selectionOption.key,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        })
                    }
                }
            }
        }
        OptionWithPlusMinus(
            title = "Text Size",
            value = viewerStyle.textSize.roundToInt().toString(),
            onPlus = {
                onViewerStyleChanged(viewerStyle.copy(textSize = viewerStyle.textSize + 1))
            },
            onMinus = {
                onViewerStyleChanged(
                    viewerStyle.copy(
                        textSize = maxOf(
                            1f, viewerStyle.textSize - 1
                        )
                    )
                )
            },
        )
        OptionWithPlusMinus(
            title = "Line Height",
            value = "${(viewerStyle.lineHeight * 100).roundToInt()}%",
            onPlus = {
                onViewerStyleChanged(viewerStyle.copy(lineHeight = viewerStyle.lineHeight + 0.05f))
            },
            onMinus = {
                onViewerStyleChanged(viewerStyle.copy(lineHeight = viewerStyle.lineHeight - 0.05f))
            },
        )
        OptionWithPlusMinus(
            title = "Letter Spacing",
            value = ((viewerStyle.letterSpacing * 100).roundToInt() / 100f).toString(),
            onPlus = {
                onViewerStyleChanged(viewerStyle.copy(letterSpacing = viewerStyle.letterSpacing + 0.01f))
            },
            onMinus = {
                onViewerStyleChanged(viewerStyle.copy(letterSpacing = viewerStyle.letterSpacing - 0.01f))
            },
        )
        OptionWithPlusMinus(
            title = "Paragraph Spacing",
            value = "${(viewerStyle.paragraphSpacing * 100).roundToInt()}%",
            onPlus = {
                onViewerStyleChanged(viewerStyle.copy(paragraphSpacing = viewerStyle.paragraphSpacing + 0.05f))
            },
            onMinus = {
                onViewerStyleChanged(viewerStyle.copy(paragraphSpacing = viewerStyle.paragraphSpacing - 0.05f))
            },
        )
    }
}

@Composable
fun ViewerOptions(
    modifier: Modifier = Modifier,
    viewerStyle: ViewerStyle,
    onViewerStyleChanged: (viewerStyle: ViewerStyle) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        OptionWithPlusMinus(
            title = "Horizontal Padding",
            value = viewerStyle.horizontalPadding.roundToInt().toString(),
            onPlus = {
                onViewerStyleChanged(viewerStyle.copy(horizontalPadding = viewerStyle.horizontalPadding + 1))
            },
            onMinus = {
                onViewerStyleChanged(
                    viewerStyle.copy(
                        horizontalPadding = maxOf(
                            0f, viewerStyle.horizontalPadding - 1
                        )
                    )
                )
            },
        )
        OptionWithPlusMinus(
            title = "Vertical Padding",
            value = viewerStyle.verticalPadding.roundToInt().toString(),
            onPlus = {
                onViewerStyleChanged(viewerStyle.copy(verticalPadding = viewerStyle.verticalPadding + 1))
            },
            onMinus = {
                onViewerStyleChanged(
                    viewerStyle.copy(
                        verticalPadding = maxOf(
                            0f, viewerStyle.verticalPadding - 1
                        )
                    )
                )
            },
        )
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Background Color", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(painter = painterResource(id = R.drawable.sun), contentDescription = "Sun")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, md_theme_light_outline, RoundedCornerShape(4.dp))
                        .background(Color(viewerStyle.brightBackgroundColor), RoundedCornerShape(4.dp))
                )
                Icon(painter = painterResource(id = R.drawable.moon), contentDescription = "Moon")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, md_theme_dark_outline, RoundedCornerShape(4.dp))
                        .background(Color(viewerStyle.darkBackgroundColor), RoundedCornerShape(4.dp))
                )
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Text Color", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(painter = painterResource(id = R.drawable.sun), contentDescription = "Sun")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, md_theme_light_outline, RoundedCornerShape(4.dp))
                        .background(Color(viewerStyle.brightTextColor), RoundedCornerShape(4.dp))
                )
                Icon(painter = painterResource(id = R.drawable.moon), contentDescription = "Moon")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, md_theme_dark_outline, RoundedCornerShape(4.dp))
                        .background(Color(viewerStyle.darkTextColor), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun OptionWithPlusMinus(
    title: String,
    value: String,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onPlus() }) {
            Icon(
                painter = painterResource(id = R.drawable.plus), contentDescription = "Plus"
            )
        }
        Text(
            modifier = Modifier.defaultMinSize(minWidth = 80.dp),
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = { onMinus() }) {
            Icon(
                painter = painterResource(id = R.drawable.minus), contentDescription = "Minus"
            )
        }
    }
}