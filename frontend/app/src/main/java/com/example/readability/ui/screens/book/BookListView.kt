package com.example.readability.ui.screens.book

import BottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.readability.R
import com.example.readability.ui.models.BookCardData
import com.example.readability.ui.theme.Gabarito
import com.example.readability.ui.theme.ReadabilityTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListView(
    bookCardDataList: List<BookCardData>,
    onNavigateSettings: () -> Unit = {},
    onNavigateAddBook: () -> Unit = {},
    onNavigateViewer: (id: String) -> Unit = {}
) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("My Library") }, actions = {
            IconButton(onClick = {
                onNavigateSettings()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings"
                )
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { onNavigateAddBook() },
            modifier = Modifier
                .testTag("Floating action button")
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            bookCardDataList.forEach { bookCardData ->
                BookCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    bookCardData = bookCardData,
                    onClick = {
                        onNavigateViewer(bookCardData.id)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun BookCardPreview() {
    ReadabilityTheme {
        BookCard(
            modifier = Modifier.width(400.dp).background(MaterialTheme.colorScheme.background),
            bookCardData = BookCardData(
                id = "1",
                coverImage = "https://images-na.ssl-images-amazon.com/images/I/51ZU%2BCvkTyL._SX331_BO1,204,203,200_.jpg",
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                progress = 0.5
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(modifier: Modifier = Modifier, bookCardData: BookCardData, onClick: () -> Unit = {}) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        BottomSheet(bookCardData = bookCardData) {
            showSheet = false
        }
    }

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(16.dp, 16.dp, 0.dp, 16.dp)
                .fillMaxHeight()
                .aspectRatio(3f / 4f, true)
                .testTag(bookCardData.coverImage),
            model = bookCardData.coverImage,
            contentDescription = "Book Cover Image",
            contentScale = ContentScale.FillWidth
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
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
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = bookCardData.author,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = Gabarito,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 4.dp, 0.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier
                            .size(with(LocalDensity.current) { 24.sp.toDp() }),
                        painter = painterResource(id = R.drawable.cloud_check),
                        contentDescription = "Uploaded",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${(bookCardData.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = { showSheet = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.dots_three),
                        contentDescription = "More"
                    )
                }
            }
        }
    }
}
