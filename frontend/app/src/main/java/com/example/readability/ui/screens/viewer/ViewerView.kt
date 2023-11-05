package com.example.readability.ui.screens.viewer

import android.content.res.Resources
import android.text.SpannableString
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import com.example.readability.R
import com.example.readability.ui.PageSplitter
import com.example.readability.ui.animation.EASING_EMPHASIZED
import com.example.readability.ui.models.BookData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


@Composable
fun ViewerView(
    bookData: BookData?,
    pageSize: Int,
    onBack: () -> Unit = {},
    onProgressChange: (Double) -> Unit = {},
    onPageSizeChanged: (Int, Int) -> Unit = { _, _ -> },
    onNavigateSettings: () -> Unit = {},
    onNavigateQuiz: () -> Unit = {},
    onNavigateSummary: () -> Unit = {}
) {
    var overlayVisible by remember { mutableStateOf(false) }

    val pageIndex = maxOf(minOf((pageSize * (bookData?.progress ?: 0.0)).toInt(), pageSize - 1), 0)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .systemBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            ViewerOverlay(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                visible = overlayVisible,
                bookData = bookData,
                pageSize = pageSize,
                onPageSizeChanged = { width, height ->
                    onPageSizeChanged(width, height)
                },
                onProgressChange = { onProgressChange(it.toDouble()) },
                onBack = { onBack() },
                onNavigateSettings = { onNavigateSettings() },
                onNavigateSummary = { onNavigateSummary() },
                onNavigateQuiz = { onNavigateQuiz() },
            ) {
                if (bookData == null) {
                    CircularProgressIndicator()
                } else {
                    BookPager(modifier = Modifier.fillMaxSize(),
                        bookData = bookData,
                        pageSize = pageSize,
                        pageIndex = pageIndex,
                        overlayVisible = overlayVisible,
                        onPageChanged = { pageIndex ->
                            onProgressChange((pageIndex + 0.5) / pageSize)
                        },
                        onOverlayVisibleChanged = { overlayVisible = it })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookPager(
    modifier: Modifier = Modifier,
    bookData: BookData,
    pageSize: Int,
    pageIndex: Int,
    overlayVisible: Boolean,
    onPageChanged: (Int) -> Unit = {},
    onOverlayVisibleChanged: (Boolean) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = pageIndex, initialPageOffsetFraction = 0f
    ) { pageSize }

    val mutex = remember { Mutex(false) }
    var animationCount by remember { mutableIntStateOf(0) }
    var animationFinishedTime by remember { mutableLongStateOf(0L) }

    val shrinkAnimation by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0f,
        label = "ViewerScreen_ViewerView_PageShrinkAnimation",
        animationSpec = tween(durationMillis = 300, 0, EASING_EMPHASIZED)
    )

    LaunchedEffect(pagerState.currentPage) {
        if (System.currentTimeMillis() - animationFinishedTime < 100) return@LaunchedEffect
        if (animationCount == 0) {
            if (pageIndex != pagerState.currentPage) {
                onPageChanged(pagerState.currentPage)
            }
        }
    }

    LaunchedEffect(bookData.progress) {
        if (pageIndex != pagerState.currentPage) {
            println("isMovingByAnimation = true")
            mutex.withLock { animationCount++ }
            try {
                pagerState.animateScrollToPage(pageIndex)
            } finally {
                println("isMovingByAnimation = false")
                mutex.withLock { animationCount-- }
                animationFinishedTime = System.currentTimeMillis()
            }
        }
    }

    val width = Resources.getSystem().displayMetrics.widthPixels
    val horizontalPadding = 64.dp * shrinkAnimation

    HorizontalPager(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(pageIndex, pageSize, overlayVisible) {
                awaitEachGesture {
                    val downEvent = awaitFirstDown(requireUnconsumed = false, PointerEventPass.Main)
                    var upEventOrCancellation: PointerInputChange? = null
                    while (upEventOrCancellation == null) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Main)
                        if (event.changes.fastAll { it.changedToUp() }) {
                            // All pointers are up
                            upEventOrCancellation = event.changes[0]
                        }
                    }
                    val diff = upEventOrCancellation.position - downEvent.position
                    if (diff.getDistanceSquared() < 1000) {
                        if (downEvent.position.x < 0.25 * width) {
                            onPageChanged(maxOf(pageIndex - 1, 0))
                        } else if (downEvent.position.x > 0.75 * width) {
                            onPageChanged(minOf(pageIndex + 1, pageSize - 1))
                        } else {
                            onOverlayVisibleChanged(!overlayVisible)
                        }
                    }
                }
            },
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            lowVelocityAnimationSpec = tween(durationMillis = 300, 0, EASING_EMPHASIZED),
            snapPositionalThreshold = 0.1f,
            pagerSnapDistance = PagerSnapDistance.atMost(if (overlayVisible) pageSize else 1)
        ),
        contentPadding = PaddingValues(
            horizontal = horizontalPadding
        ),
        pageSpacing = (32 * shrinkAnimation).dp,
        userScrollEnabled = animationCount == 0,
    ) { pageIndex ->
        BookPage(
            bookData = bookData,
            pageSize = pageSize,
            pageIndex = pageIndex,
        )
    }
}

@Composable
fun BookPage(
    modifier: Modifier = Modifier,
    bookData: BookData?,
    pageSize: Int,
    pageIndex: Int,
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val dynamicLayout = remember(
        pageIndex,
        if (pageSize <= pageIndex) 0 else bookData?.pageSplitData?.pageSplits?.get(pageIndex)
    ) {
        if (bookData == null) return@remember null
        val textPaint = PageSplitter.buildTextPaint()
        textPaint.color = onBackground.toArgb()
        val startIndex = if (pageIndex == 0) 0 else bookData.pageSplitData?.pageSplits?.get(
            pageIndex - 1
        ) ?: 0
        val endIndex =
            if (pageIndex == pageSize) bookData.content.length else bookData.pageSplitData?.pageSplits?.get(
                pageIndex
            ) ?: 0
        PageSplitter.buildDynamicLayout(
            SpannableString(bookData.content.subSequence(startIndex, endIndex)),
            textPaint,
            bookData.pageSplitData?.width ?: 0
        )
    }

    val padding = with(LocalDensity.current) { 16.dp.toPx() }

    val ratio =
        ((bookData?.pageSplitData?.width ?: 0) + padding * 2) / ((bookData?.pageSplitData?.height
            ?: 0) + padding * 2)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = pageSize > pageIndex, label = "ViewerScreen.PageContent"
            ) {
                when (it) {
                    true -> {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawIntoCanvas { canvas ->
                                // red background
                                val ratio =
                                    size.width / (bookData!!.pageSplitData!!.width + 32.dp.toPx())
                                // scale with pivot left top
                                scale(
                                    scale = ratio, pivot = Offset(0f, 0f)
                                ) {
                                    translate(left = 16.dp.toPx(), top = 16.dp.toPx()) {
                                        dynamicLayout?.draw(canvas.nativeCanvas)
                                    }
                                }
                            }
                        }
                    }

                    false -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean,
    bookData: BookData?,
    pageSize: Int,
    onPageSizeChanged: (Int, Int) -> Unit = { _, _ -> },
    onProgressChange: (Float) -> Unit,
    onBack: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateSummary: () -> Unit,
    onNavigateQuiz: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    var aiButtonsVisible by remember { mutableStateOf(false) }
    val pageIndex = minOf(
        (pageSize * (bookData?.progress ?: 0.0)).toInt(), pageSize - 1
    )
    val density = LocalDensity.current

    Box(
        modifier = modifier, contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        val padding = with(density) {
                            16.dp.toPx()
                        }
                        onPageSizeChanged(
                            it.size.width - (padding * 2).toInt(),
                            it.size.height - (padding * 2).toInt()
                        )
                    }, contentAlignment = Alignment.Center
            ) {
                if (bookData == null || pageSize == 0) {
                    CircularProgressIndicator()
                }
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .alpha(0f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${pageIndex + 1} / $pageSize",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (bookData != null && pageSize > 0) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = visible, label = "EbookView.ViewerOverlay.TopContent"
                ) {
                    when (it) {
                        true -> CenterAlignedTopAppBar(windowInsets = WindowInsets(0, 0, 0, 0),
                            title = {
                                Text(
                                    text = bookData.title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBack()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    onNavigateSettings()
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.settings),
                                        contentDescription = "Settings"
                                    )
                                }
                            })

                        false -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    content()
                }

                AnimatedContent(
                    targetState = visible, label = "EbookView.ViewerOverlay.BottomContent"
                ) { it ->
                    when (it) {
                        true -> Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface),
                        ) {
                            AnimatedContent(
                                targetState = aiButtonsVisible,
                                label = "EbookView.ViewerOverlay.BottomTopContent"
                            ) {
                                when (it) {
                                    false -> Slider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        value = bookData.progress.toFloat(),
                                        onValueChange = onProgressChange
                                    )

                                    true -> Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = {
                                                onNavigateSummary()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Generate Summary")
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                onNavigateQuiz()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Generate Quiz")
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    IconButton(onClick = { aiButtonsVisible = !aiButtonsVisible }) {
                                        Icon(
                                            painter = if (aiButtonsVisible) painterResource(id = R.drawable.sparkle_filled) else painterResource(
                                                id = R.drawable.sparkle
                                            ),
                                            contentDescription = "AI Button",
                                            tint = if (aiButtonsVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Text(
                                    text = "${pageIndex + 1} / $pageSize"
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        false -> Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${pageIndex + 1} / $pageSize",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }


}