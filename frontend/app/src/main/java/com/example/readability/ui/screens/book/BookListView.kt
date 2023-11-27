package com.example.readability.ui.screens.book

import BottomSheet
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readability.R
import com.example.readability.data.book.BookCardData
import com.example.readability.ui.theme.Gabarito
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BookListView(
    bookCardDataList: List<BookCardData>,
    onLoadImage: suspend (id: Int) -> Result<Unit> = { Result.success(Unit) },
    onLoadContent: suspend (id: Int) -> Result<Unit> = { Result.success(Unit) },
    onProgressChanged: (Int, Double) -> Unit = { _, _ -> },
    onBookDeleted: suspend (Int) -> Result<Unit> = { Result.success(Unit) },
    onNavigateSettings: () -> Unit = {},
    onNavigateAddBook: () -> Unit = {},
    onNavigateViewer: (id: Int) -> Unit = {},
    onNavigateBookList: () -> Unit = {},
    onRefresh: suspend () -> Unit = {},
) {
    val contentLoadScope = rememberCoroutineScope()
    val context = LocalContext.current
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo

    var refreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()

    fun refresh() = refreshScope.launch {
        refreshing = true
        if (activeNetwork?.isConnectedOrConnecting == null || !activeNetwork.isConnectedOrConnecting) {
            delay(700)
            refreshing = false
            delay(200)
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        } else {
            onRefresh()
            delay(1000) // TODO
            refreshing = false
        }
    }

    val state = rememberPullRefreshState(refreshing, ::refresh)

    // TODO: Empty Library message is shown during the database loading -> do not show it while loading the database
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("My Library") }, actions = {
            IconButton(onClick = {
                onNavigateSettings()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                )
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { onNavigateAddBook() },
            modifier = Modifier.testTag("Floating action button"),
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pullRefresh(state),
        ) {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize(),
                targetState = bookCardDataList.isEmpty(),
                label = "BookScreen.BookListView.Content",
            ) {
                when (it) {
                    true -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.size(96.dp),
                            painter = painterResource(id = R.drawable.file_dashed_thin),
                            contentDescription = "No File",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = "Library is Empty",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Press the button below\nto add books to your library.",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }

                    false -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        items(bookCardDataList.size) { index ->
                            if (index < bookCardDataList.size) {
                                BookCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    bookCardData = bookCardDataList[index],
                                    onClick = {
                                        // TODO: show download status
                                        contentLoadScope.launch {
                                            onLoadContent(bookCardDataList[index].id).onSuccess {
                                                onNavigateViewer(bookCardDataList[index].id)
                                            }.onFailure {
                                                it.printStackTrace()
                                                Toast.makeText(
                                                    context,
                                                    "Failed to load content. ${it.message}",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        }
                                    },
                                    onLoadImage = {
                                        onLoadImage(bookCardDataList[index].id)
                                    },
                                    onProgressChanged = { id, progress ->
                                        onProgressChanged(id, progress)
                                    },
                                    onBookDeleted = { id ->
                                        onBookDeleted(id)
                                    },
                                    onNavigateBookList = {
                                        onNavigateBookList()
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                                if (index == bookCardDataList.size - 1){
                                    LazyColumn (
                                        modifier = Modifier.height(80.dp)
                                    ){

                                    }
                                }

                            }
                        }

                    }
                }
            }
            PullRefreshIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                refreshing = refreshing,
                state = state
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun BookCardPreview() {
    ReadabilityTheme {
        BookCard(
            modifier = Modifier
                .width(400.dp)
                .background(MaterialTheme.colorScheme.background),
            bookCardData = BookCardData(
                id = 1,
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                progress = 0.5,
                content = "aasdasd",
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    modifier: Modifier = Modifier,
    bookCardData: BookCardData,
    onClick: () -> Unit = {},
    onLoadImage: suspend () -> Unit = {},
    onProgressChanged: (Int, Double) -> Unit = { _, _ -> },
    onBookDeleted: suspend (Int) -> Result<Unit> = { Result.success(Unit) },
    onNavigateBookList: () -> Unit = {},
) {
    var showSheet by remember { mutableStateOf(false) }
    var loadingImage by remember { mutableStateOf(false) }
    val imageLoadScope = rememberCoroutineScope()

    if (showSheet) {
        BottomSheet(bookCardData = bookCardData, onDismiss = {
            showSheet = false
        }, onProgressChanged = onProgressChanged,
            onBookDeleted = onBookDeleted,
            onNavigateBookList = onNavigateBookList,)
    }

    LaunchedEffect(bookCardData.coverImage) {
        if (bookCardData.coverImage != null && bookCardData.coverImageData == null && !loadingImage) {
            loadingImage = true
            imageLoadScope.launch {
                onLoadImage()
                loadingImage = false
            }
        }
    }

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bookCardData.coverImageData == null) {
            Box(
                Modifier
                    .padding(16.dp, 16.dp, 0.dp, 16.dp)
                    .fillMaxHeight()
                    .width(64.dp),
            ) {
                // TODO: Add placeholder image
            }
        } else {
            Image(
                modifier = Modifier
                    .padding(16.dp, 16.dp, 0.dp, 16.dp)
                    .fillMaxHeight()
                    .width(64.dp)
                    .testTag(bookCardData.coverImage ?: ""),
                bitmap = bookCardData.coverImageData,
                contentDescription = "Book Cover Image",
                contentScale = ContentScale.FillWidth,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 16.dp, 16.dp, 0.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = bookCardData.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = Gabarito,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = bookCardData.author,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = Gabarito,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 4.dp, 0.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(with(LocalDensity.current) { 24.sp.toDp() }),
                        painter = painterResource(id = R.drawable.cloud_check),
                        contentDescription = "Uploaded",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "${(bookCardData.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                IconButton(onClick = { showSheet = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.dots_three),
                        contentDescription = "More",
                    )
                }
            }
        }
    }
}
