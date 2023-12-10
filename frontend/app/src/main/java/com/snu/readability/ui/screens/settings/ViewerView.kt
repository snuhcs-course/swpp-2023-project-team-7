package com.snu.readability.ui.screens.settings

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
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.snu.readability.R
import com.snu.readability.data.viewer.FontDataSource
import com.snu.readability.data.viewer.ViewerStyle
import com.snu.readability.data.viewer.ViewerStyleBuilder
import com.snu.readability.ui.theme.Gabarito
import com.snu.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun ViewerPreview() {
    ReadabilityTheme {
        ViewerView(
            viewerStyle = ViewerStyleBuilder().build(),
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ViewerOptionsPreview() {
    ReadabilityTheme {
        Surface(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        ) {
            ViewerOptions(viewerStyle = ViewerStyleBuilder().build(), onViewerStyleChanged = {})
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
    val pageHorizontalPadding = with(LocalDensity.current) { viewerStyle.horizontalPadding.dp.toPx() }

    val pagerState = rememberPagerState(initialPage = 0) {
        2
    }
    val pagerScope = rememberCoroutineScope()
    var maxHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopAppBar(title = { Text(text = "Viewer Settings") }, navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                }
            })
        },
    ) { innerPadding ->
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
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(12.dp),
                    ),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = viewerStyle.horizontalPadding.dp,
                            vertical = viewerStyle.verticalPadding.dp,
                        )
                        .clipToBounds(),
                ) {
                    drawIntoCanvas {
                        onDrawPage(it.nativeCanvas, (width - padding * 2 - pageHorizontalPadding * 2).roundToInt())
                    }
                }
            }
            PrimaryTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
            ) {
                listOf("Text", "Viewer").forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { pagerScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = Gabarito,
                                    fontWeight = FontWeight.Medium,
                                    color = if (pagerState.currentPage == index) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                ),
                            )
                        },
                    )
                }
            }
            HorizontalPager(
                modifier = if (maxHeight == 0.dp) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(maxHeight)
                },
                state = pagerState,
                verticalAlignment = Alignment.Top,
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
                        onViewerStyleChanged = onViewerStyleChanged,
                    )

                    1 -> ViewerOptions(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged {
                                val height = with(density) { it.height.toDp() }
                                maxHeight = maxHeight.coerceAtLeast(height)
                            },
                        viewerStyle = viewerStyle,
                        onViewerStyleChanged = onViewerStyleChanged,
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
    onViewerStyleChanged: (viewerStyle: ViewerStyle) -> Unit,
) {
    var fontFamilyExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Font Family", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            ExposedDropdownMenuBox(
                expanded = fontFamilyExpanded,
                onExpandedChange = { fontFamilyExpanded = it },
            ) {
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .menuAnchor()
                        .padding(start = 16.dp, end = 8.dp)
                        .defaultMinSize(minWidth = 128.dp)
                        .width(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = viewerStyle.fontFamily,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = fontFamilyExpanded,
                    )
                }
                ExposedDropdownMenu(expanded = fontFamilyExpanded, onDismissRequest = {
                    fontFamilyExpanded = false
                }) {
                    fontMap.forEach { selectionOption ->
                        DropdownMenuItem(onClick = {
                            onViewerStyleChanged(
                                ViewerStyleBuilder(viewerStyle)
                                    .fontFamily(selectionOption.key)
                                    .build(),
                            )
                            fontFamilyExpanded = false
                        }, text = {
                            Text(
                                text = selectionOption.key,
                                style = MaterialTheme.typography.bodyLarge,
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
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .textSize(minOf(50f, viewerStyle.textSize + 1f))
                        .build(),
                )
            },
            onMinus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .textSize(maxOf(10f, viewerStyle.textSize - 1f))
                        .build(),
                )
            },
        )
        OptionWithPlusMinus(
            title = "Line Height",
            value = "${(viewerStyle.lineHeight * 100).roundToInt()}%",
            onPlus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .lineHeight(minOf(6f, viewerStyle.lineHeight + 0.05f))
                        .build(),
                )
            },
            onMinus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .lineHeight(maxOf(0.6f, viewerStyle.lineHeight - 0.05f))
                        .build(),
                )
            },
        )
        OptionWithPlusMinus(
            title = "Letter Spacing",
            value = ((viewerStyle.letterSpacing * 100).roundToInt() / 100f).toString(),
            onPlus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .letterSpacing(minOf(0.4f, viewerStyle.letterSpacing + 0.01f))
                        .build(),
                )
            },
            onMinus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .letterSpacing(maxOf(0f, viewerStyle.letterSpacing - 0.01f))
                        .build(),
                )
            },
        )
        OptionWithPlusMinus(
            title = "Paragraph Spacing",
            value = "${(viewerStyle.paragraphSpacing * 100).roundToInt()}%",
            onPlus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .paragraphSpacing(minOf(3f, viewerStyle.paragraphSpacing + 0.05f))
                        .build(),
                )
            },
            onMinus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .paragraphSpacing(maxOf(1f, viewerStyle.paragraphSpacing - 0.05f))
                        .build(),
                )
            },
        )
    }
}

@Composable
fun ViewerOptions(
    modifier: Modifier = Modifier,
    viewerStyle: ViewerStyle,
    onViewerStyleChanged: (viewerStyle: ViewerStyle) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        OptionWithPlusMinus(
            title = "Horizontal Padding",
            value = viewerStyle.horizontalPadding.roundToInt().toString(),
            onPlus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .horizontalPadding(minOf(70f, viewerStyle.horizontalPadding + 1))
                        .build(),
                )
            },
            onMinus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .horizontalPadding(maxOf(0f, viewerStyle.horizontalPadding - 1))
                        .build(),
                )
            },
        )
        OptionWithPlusMinus(
            title = "Vertical Padding",
            value = viewerStyle.verticalPadding.roundToInt().toString(),
            onPlus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .verticalPadding(minOf(110f, viewerStyle.verticalPadding + 1))
                        .build(),
                )
            },
            onMinus = {
                onViewerStyleChanged(
                    ViewerStyleBuilder(viewerStyle)
                        .verticalPadding(maxOf(0f, viewerStyle.verticalPadding - 1))
                        .build(),
                )
            },
        )
    }
}

@Composable
fun OptionWithPlusMinus(title: String, value: String, onPlus: () -> Unit, onMinus: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onMinus() }) {
            Icon(
                painter = painterResource(id = R.drawable.minus),
                contentDescription = "Minus",
            )
        }
        Text(
            modifier = Modifier.defaultMinSize(minWidth = 80.dp),
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = { onPlus() }) {
            Icon(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = "Plus",
            )
        }
    }
}
