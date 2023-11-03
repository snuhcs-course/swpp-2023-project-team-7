package com.example.readability.ui.screens.book

import BottomSheet
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.readability.R
import com.example.readability.ui.models.BookCardData
import com.example.readability.ui.theme.Gabarito
import com.example.readability.ui.viewmodels.BookListViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListView(
    bookListViewModel: BookListViewModel = viewModel()
) {
    val bookCardDataList by bookListViewModel.bookCardDataList.collectAsState(initial = emptyList())

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("My Library") }, actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings"
                )
            }
        })
    }, floatingActionButton = { AddButton() }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
            bookCardDataList.forEach { bookCardData ->
                BookCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    bookCardData = bookCardData
                )
            }
        }
    }
}

@Composable
fun AddButton() {
    FloatingActionButton(
        onClick = {
//            val intent = Intent(context, AddBookActivity::class.java)
//            activityLauncher.launch(intent)
        },
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}

@Composable
@Preview(showBackground = true)
fun BookCardPreview() {
    BookCard(
        bookCardData = BookCardData(
            id = "1",
            coverImage = "https://images-na.ssl-images-amazon.com/images/I/51ZU%2BCvkTyL._SX331_BO1,204,203,200_.jpg",
            title = "The Great Gatsby",
            author = "F. Scott Fitzgerald",
            progress = 0.5
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(modifier: Modifier = Modifier, bookCardData: BookCardData) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        BottomSheet() {
            showSheet = false
        }
    }
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min).clickable {
//                val intent = Intent(context, ReaderActivity::class.java)
//                activityLauncher.launch(intent)
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier.padding(16.dp, 16.dp, 0.dp, 16.dp),
            model = bookCardData.coverImage,
            contentDescription = "Book Cover Image",
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(0.dp, 16.dp, 16.dp, 0.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = bookCardData.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Gabarito, fontWeight = FontWeight.Medium)
                )
                Icon(
                    painter = painterResource(id = R.drawable.cloud_check),
                    contentDescription = "Uploaded",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = bookCardData.author,
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.secondary, fontFamily = Gabarito, fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 4.dp, 0.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier.weight(1f).padding(0.dp, 0.dp, 0.dp, 16.dp),
                    text = "${(bookCardData.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
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
