package com.example.shareader

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.text.SpannableString
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shareader.ui.PageSplitter
import com.example.shareader.ui.viewmodels.ViewerViewModel
import com.example.shareader.ui.viewmodels.ViewerViewModelFactory

//import androidx.paging.Pager
//import com.google.accompanist.pager.ExperimentalPagerApi
//import com.google.accompanist.pager.PagerState
//import com.google.accompanist.pager.rememberPagerState

@Composable
fun EbookView(
    viewerViewModel: ViewerViewModel = viewModel(factory = ViewerViewModelFactory("1"))
) {
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            BookPager(modifier = Modifier.weight(1f).fillMaxWidth(), viewerViewModel = viewerViewModel)
            Row(
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(context, SummaryActivity::class.java)
                        activityLauncher.launch(intent)
                    }, modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Generate Summary")
                }
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(context, QuizActivity::class.java)
                        activityLauncher.launch(intent)
                    }, modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Generate Quiz")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookPager(
    modifier : Modifier = Modifier,
    viewerViewModel: ViewerViewModel
) {
    val bookData by viewerViewModel.bookData.collectAsState(initial = null)
    val pageSize by viewerViewModel.pageSize.collectAsState(initial = 0)
    println(pageSize)
    val pagerState = rememberPagerState(
        initialPage = 0, initialPageOffsetFraction = 0f
    ) {
        pageSize + 1
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
                val density = Resources.getSystem().displayMetrics.density
                val textPaint = TextPaint()
                textPaint.isAntiAlias = true
//                textPaint.typeface = Lora
                textPaint.textSize = 24f * density
                textPaint.color = onBackground.toArgb()
                val startIndex =
                    if (pageIndex == 0) 0 else bookData!!.pageSplitData?.pageSplits?.get(
                        pageIndex - 1
                    ) ?: 0
                val endIndex =
                    if (pageIndex == pageSize) bookData!!.content.length else bookData!!.pageSplitData?.pageSplits?.get(
                        pageIndex
                    ) ?: 0
                PageSplitter.buildDynamicLayout(
                    SpannableString(bookData!!.content.subSequence(startIndex, endIndex)),
                    textPaint,
                    bookData?.pageSplitData?.width ?: 0
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
                            viewerViewModel.setPageSize(it.size.width, it.size.height)
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
