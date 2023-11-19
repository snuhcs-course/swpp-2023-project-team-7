package com.example.readability.ui.screens.viewer

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import coil.compose.AsyncImage
import com.example.readability.R
import com.example.readability.data.book.Book
import com.example.readability.data.viewer.PageSplitData
import com.example.readability.ui.animation.DURATION_EMPHASIZED
import com.example.readability.ui.animation.EASING_EMPHASIZED
import com.example.readability.ui.animation.EASING_LEGACY
import com.example.readability.ui.components.RoundedRectFilledTonalButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


@Composable
fun ViewerView(
    bookData: Book?,
    pageSplitData: PageSplitData?,
    onPageDraw: (canvas: NativeCanvas, pageIndex: Int) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    onProgressChange: (Double) -> Unit = {},
    onPageSizeChanged: (Int, Int) -> Unit = { _, _ -> },
    onNavigateSettings: () -> Unit = {},
    onNavigateQuiz: () -> Unit = {},
    onNavigateSummary: () -> Unit = {}
) {
    var overlayVisible by remember { mutableStateOf(false) }
    var closeLoading by remember { mutableStateOf(true) }
    var transitionDuration by remember { mutableStateOf(0) }
    var lastBookReady by remember { mutableStateOf(true) }
    val bookReady by rememberUpdatedState(newValue = bookData != null && pageSplitData != null && pageSplitData.pageSplits.isNotEmpty() && closeLoading)

    // if book is ready within 150ms, don't show loading screen
    // otherwise, show loading screen for at least 700ms
    LaunchedEffect(bookReady, lastBookReady) {
        if (!bookReady && lastBookReady) {
            transitionDuration = 0
            delay(150)
            if (bookReady) return@LaunchedEffect
            closeLoading = false
            transitionDuration = 300
            delay(550)
            closeLoading = true
        }
    }

    SideEffect {
        lastBookReady = bookReady
        if (bookReady) {
            transitionDuration = 0
        }
    }

    val pageSize = pageSplitData?.pageSplits?.size ?: 0
    val pageIndex = maxOf(minOf((pageSize * (bookData?.progress ?: 0.0)).toInt(), pageSize - 1), 0)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .systemBarsPadding()
    ) { innerPadding ->
        ViewerSizeMeasurer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onPageSizeChanged = { width, height ->
                onPageSizeChanged(width, height)
            },
        )
        AnimatedContent(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
            targetState = bookReady,
            label = "ViewerScreen.ViewerView.Content",
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = transitionDuration, 0, EASING_LEGACY)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = transitionDuration, 0, EASING_LEGACY)
                )
            }) {
            when (it) {
                true -> ViewerOverlay(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    visible = overlayVisible,
                    bookData = bookData,
                    pageSize = pageSize,
                    onProgressChange = { onProgressChange(it.toDouble()) },
                    onBack = { onBack() },
                    onNavigateSettings = { onNavigateSettings() },
                    onNavigateSummary = { onNavigateSummary() },
                    onNavigateQuiz = { onNavigateQuiz() },
                ) {
                    if (bookData != null && pageSize > 0) {
                        BookPager(modifier = Modifier.fillMaxSize(),
                            bookData = bookData,
                            pageSplitData = pageSplitData,
                            pageSize = pageSize,
                            onPageDraw = { canvas, pageIndex ->
                                onPageDraw(canvas, pageIndex)
                            },
                            pageIndex = pageIndex,
                            overlayVisible = overlayVisible,
                            onPageChanged = { pageIndex ->
                                onProgressChange((pageIndex + 0.5) / pageSize)
                            },
                            onOverlayVisibleChanged = { overlayVisible = it })
                    } else {
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }

                false -> if (closeLoading) {
                    // prevent flickering
                    Spacer(modifier = Modifier.fillMaxSize())
                } else {
                    LoadingScreen(modifier = Modifier.fillMaxSize(), bookData = bookData)
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier, bookData: Book?) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(64.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bookData != null) {
                    Column(
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        AsyncImage(
                            modifier = Modifier.weight(2f),
                            model = bookData.coverImage,
                            contentDescription = "Book Cover Image",
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            24.dp, Alignment.CenterVertically
                        ), horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = bookData.title,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Opening Book...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bookData != null) {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        AsyncImage(
                            modifier = Modifier.weight(2f),
                            model = bookData.coverImage,
                            contentDescription = "Book Cover Image",
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            24.dp, Alignment.CenterVertically
                        ), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = bookData.title,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Opening Book...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookPager(
    modifier: Modifier = Modifier,
    bookData: Book,
    pageSplitData: PageSplitData?,
    pageSize: Int,
    pageIndex: Int,
    overlayVisible: Boolean,
    onPageDraw: (canvas: NativeCanvas, pageIndex: Int) -> Unit = { _, _ -> },
    onPageChanged: (Int) -> Unit = {},
    onOverlayVisibleChanged: (Boolean) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = pageIndex, initialPageOffsetFraction = 0f
    ) { pageSize }

    val mutex = remember { Mutex(false) }
    var animationCount by remember { mutableIntStateOf(0) }
    var animationFinishedTime by remember { mutableLongStateOf(0L) }
    val overlayChangeScope = rememberCoroutineScope()
    val animationScope = rememberCoroutineScope()

    val shrinkAnimation by animateFloatAsState(
        targetValue = if (overlayVisible) 1f else 0f,
        label = "ViewerScreen.ViewerView.PageShrinkAnimation",
        animationSpec = tween(durationMillis = DURATION_EMPHASIZED, 0, EASING_EMPHASIZED)
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
                pagerState.animateScrollToPage(
                    pageIndex, animationSpec = tween(300, 0, EASING_LEGACY)
                )
            } finally {
                println("isMovingByAnimation = false")
                mutex.withLock { animationCount-- }
                animationFinishedTime = System.currentTimeMillis()
            }
        }
    }

    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    val padding = with(LocalDensity.current) { 16.dp.toPx() }
    val pageWidth = (pageSplitData?.width ?: 0) + padding * 2
    val pageHeight = (pageSplitData?.height ?: 0) + padding * 2
    val density = LocalDensity.current

    val pagePadding = remember(width, height, pageWidth, pageHeight) {
        val pageGap = 32.dp

        var pagePadding = pageGap * 2
        if (width == 0 || height == 0) {
            return@remember pagePadding
        }
        val pagePaddingPx = with(density) { pagePadding.toPx() }

        val ratio = pageWidth / pageHeight

        val desiredWidth = width - pagePaddingPx * 2
        val derivedHeight = desiredWidth / ratio
        if (height - pagePaddingPx < derivedHeight) {
            // shrink height to match it
            val diff = derivedHeight - (height - pagePaddingPx)
            val diffWidth = diff * ratio
            pagePadding += with(density) { (diffWidth / 2).toDp() }
        }

        pagePadding
    }

    HorizontalPager(
        modifier = modifier
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
                    if (diff.getDistanceSquared() < 10000) {
                        if (downEvent.position.x < 0.25 * width) {
                            onPageChanged(maxOf(pageIndex - 1, 0))
                        } else if (downEvent.position.x > 0.75 * width) {
                            onPageChanged(minOf(pageIndex + 1, pageSize - 1))
                        } else {
                            // if the page size is changed with offset, the page stops at the middle of the page
                            // to prevent that, force remove the offset and close the overlay
                            val targetValue = !overlayVisible
                            overlayChangeScope.launch {
                                animationScope.launch { pagerState.animateScrollToPage(pagerState.currentPage) }
                                while (pagerState.currentPageOffsetFraction != 0f && isActive) {
                                    delay(16)
                                }
                                if (isActive) onOverlayVisibleChanged(targetValue)
                            }
                        }
                    }
                }
            }
            .onSizeChanged {
                width = it.width
                height = it.height
            },
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            lowVelocityAnimationSpec = tween(durationMillis = 300, 0, EASING_LEGACY),
            snapPositionalThreshold = 0.1f,
            pagerSnapDistance = PagerSnapDistance.atMost(if (overlayVisible) pageSize else 1)
        ),
        contentPadding = PaddingValues(
            horizontal = pagePadding * shrinkAnimation
        ),
        pageSpacing = 32.dp * shrinkAnimation,
        userScrollEnabled = animationCount == 0,
    ) { pageIndex ->
        BookPage(
            pageSplitData = pageSplitData,
            pageSize = pageSize,
            pageIndex = pageIndex,
            onPageDraw = onPageDraw
        )
    }
}

@Composable
fun BookPage(
    modifier: Modifier = Modifier,
    pageSplitData: PageSplitData?,
    pageSize: Int,
    pageIndex: Int,
    onPageDraw: (canvas: NativeCanvas, pageIndex: Int) -> Unit = { _, _ -> },
) {
    val padding = with(LocalDensity.current) { 16.dp.toPx() }

    val ratio =
        ((pageSplitData?.width ?: 0) + padding * 2) / ((pageSplitData?.height ?: 0) + padding * 2)

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
                                val ratio = size.width / (pageSplitData!!.width + 32.dp.toPx())
                                // scale with pivot left top
                                scale(
                                    scale = ratio, pivot = Offset(0f, 0f)
                                ) {
                                    translate(left = 16.dp.toPx(), top = 16.dp.toPx()) {
                                        onPageDraw(canvas.nativeCanvas, pageIndex)
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

@Composable
fun ViewerSizeMeasurer(
    modifier: Modifier = Modifier,
    onPageSizeChanged: (Int, Int) -> Unit = { _, _ -> },
) {
    Column(
        modifier = modifier
    ) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(16.dp)
            .onGloballyPositioned {
                onPageSizeChanged(
                    it.size.width, it.size.height
                )
            })
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .alpha(0f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "test", style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean,
    bookData: Book?,
    pageSize: Int,
    onProgressChange: (Float) -> Unit,
    onBack: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateSummary: () -> Unit,
    onNavigateQuiz: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    val pageIndex = minOf(
        (pageSize * (bookData?.progress ?: 0.0)).toInt(), pageSize - 1
    )

    Column(
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = visible, label = "EbookView.ViewerOverlay.TopContent",
            enter = fadeIn(tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED)) + expandVertically(
                tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED)
            ),
            exit = fadeOut(tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED)) + shrinkVertically(
                tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED)
            ),
        ) {
            CenterAlignedTopAppBar(windowInsets = WindowInsets(0, 0, 0, 0), title = {
                Text(
                    text = bookData?.title ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }, navigationIcon = {
                IconButton(onClick = {
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }, actions = {
                IconButton(onClick = {
                    onNavigateSettings()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "Settings"
                    )
                }
            })
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }

        AnimatedVisibility(
            visible = visible,
            label = "EbookView.ViewerOverlay.BottomContent",
            enter = fadeIn(tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED)) + expandVertically(
                tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED), expandFrom = Alignment.Top
            ),
            exit = fadeOut(tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED)) + shrinkVertically(
                tween(DURATION_EMPHASIZED, 0, EASING_EMPHASIZED), shrinkTowards = Alignment.Top
            ),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RoundedRectFilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateSummary() }) {
                            Text("Generate Summary")
                        }
                        RoundedRectFilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateQuiz() }) {
                            Text("Generate Quiz")
                        }
                    }
                    Slider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        value = bookData?.progress?.toFloat() ?: 0f,
                        onValueChange = onProgressChange
                    )
                }
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            text = "${pageIndex + 1} / $pageSize",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }


}