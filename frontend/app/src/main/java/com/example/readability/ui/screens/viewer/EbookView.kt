package com.example.readability.ui.screens.viewer

import android.content.Intent
import android.content.res.Resources
import android.text.SpannableString
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readability.MainActivity
import com.example.readability.R
import com.example.readability.ui.PageSplitter
import com.example.readability.ui.models.BookData
import com.example.readability.ui.models.QuizModel
import com.example.readability.ui.viewmodels.ViewerViewModel
import com.example.readability.ui.viewmodels.ViewerViewModelFactory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


@Composable
fun EbookView(
    viewerViewModel: ViewerViewModel = viewModel(factory = ViewerViewModelFactory("1"))
) {
    val bookData by viewerViewModel.bookData.collectAsState(initial = null)
    val pageSize by viewerViewModel.pageSize.collectAsState(initial = 0)
    val pageIndex = minOf((pageSize * (bookData?.progress ?: 0.0)).toInt(), pageSize - 1)
    println("pageIndex: $pageIndex")

    var overlayVisible by remember { mutableStateOf(false) }

    val width = Resources.getSystem().displayMetrics.widthPixels

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            if (offset.x < 0.25 * width) {
                                // prev page
                                val pageIndex = minOf((pageSize * (bookData?.progress ?: 0.0)).toInt(), pageSize - 1)
                                val newPageIndex = maxOf(pageIndex - 1, 0)
                                println("newPageIndex: $newPageIndex")
                                if (newPageIndex != pageIndex) {
                                    viewerViewModel.setProgress((newPageIndex + 0.5) / pageSize)
                                }
                            } else if (offset.x > 0.75 * width) {
                                // next page
                                val pageIndex = minOf((pageSize * (bookData?.progress ?: 0.0)).toInt(), pageSize - 1)
                                val newPageIndex = minOf(pageIndex + 1, pageSize - 1)
                                println("pageIndex: $pageIndex, pageSize: $pageSize, newPageIndex: $newPageIndex")
                                if (newPageIndex != pageIndex) {
                                    viewerViewModel.setProgress((newPageIndex + 0.5) / pageSize)
                                }
                            } else {
                                overlayVisible = !overlayVisible
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                BookPager(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    bookData = bookData,
                    pageSize = pageSize,
                    onPageSizeChanged = { width, height ->
                        viewerViewModel.setPageSize(width, height)
                    },
                    onPageChanged = { pageIndex ->
                        viewerViewModel.setProgress((pageIndex + 0.5) / pageSize)
                    },
                    pageIndex = pageIndex
                )
            }
            bookData?.let {
                ViewerOverlay(
                    visible = overlayVisible,
                    bookData = it,
                    pageSize = pageSize,
                    onProgressChange = { progress ->
                        viewerViewModel.setProgress(progress.toDouble())
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookPager(
    modifier: Modifier = Modifier,
    bookData: BookData?,
    pageSize: Int,
    pageIndex: Int,
    onPageSizeChanged: (Int, Int) -> Unit,
    onPageChanged: (Int) -> Unit = {}
) {
    println("pageSize: $pageSize")
    val pagerState = rememberPagerState(
        initialPage = 0, initialPageOffsetFraction = 0f
    ) {
        pageSize + 1
    }

    val mutex = remember {
        Mutex(false)
    }

    var isMovingByAnimation by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (bookData == null) return@LaunchedEffect
        println("isMovingByAnimation: $isMovingByAnimation")
        if (isMovingByAnimation) {
            isMovingByAnimation = false
            return@LaunchedEffect
        }
        if (pageIndex != pagerState.currentPage) {
            println("onPageChanged: $pageIndex")
            onPageChanged(pagerState.currentPage)
        }
    }

    LaunchedEffect(bookData?.progress) {
        if (bookData == null) return@LaunchedEffect
        if (pageIndex != pagerState.currentPage) {
            mutex.withLock {
                println("isMovingByAnimation = true")
                isMovingByAnimation = true
                pagerState.scrollToPage(pageIndex)
                println("isMovingByAnimation = false")
            }
        }
    }

    val onBackground = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
    ) {
        HorizontalPager(
            state = pagerState
        ) { pageIndex ->
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp, 16.dp, 16.dp, 0.dp)
                        .onGloballyPositioned {
                            onPageSizeChanged(it.size.width, it.size.height)
                        }) {
                        AnimatedContent(
                            targetState = pageSize > pageIndex, label = "ViewerScreen.PageContent"
                        ) {
                            when (it) {
                                true -> {
                                    Canvas(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        drawIntoCanvas { canvas ->
                                            dynamicLayout?.draw(canvas.nativeCanvas)
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
                    Row(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean,
    bookData: BookData,
    pageSize: Int,
    onProgressChange: (Float) -> Unit
) {
    var aiButtonsVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }

    Column(
        modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedContent(targetState = visible, label = "EbookView.ViewerOverlay.TopContent") {
            when (it) {
                true -> CenterAlignedTopAppBar(title = {
                    Text(text = bookData.title, style = MaterialTheme.typography.bodyLarge)
                }, navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        activityLauncher.launch(intent)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                        )
                    }
                }, actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "Settings"
                        )
                    }
                })

                false -> null
            }
        }

        AnimatedContent(targetState = visible, label = "EbookView.ViewerOverlay.BottomContent") {
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
                                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
//                                        val intent = Intent(context, SummaryActivity::class.java)
//                                        activityLauncher.launch(intent)
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
                                        QuizModel.getInstance().loadQuiz("1", 0.98)
//                                        val intent = Intent(context, QuizActivity::class.java)
//                                        activityLauncher.launch(intent)
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
                            text = "${
                                minOf(
                                    (pageSize * bookData.progress).toInt(), pageSize - 1
                                ) + 1
                            } / $pageSize"
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                false -> null
            }
        }
    }
}