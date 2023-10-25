package com.example.shareader

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//import androidx.paging.Pager
//import com.google.accompanist.pager.ExperimentalPagerApi
//import com.google.accompanist.pager.PagerState
//import com.google.accompanist.pager.rememberPagerState

@Composable
fun EbookView() {
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }

    var page = "2";
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopBar(name = "")
            BookPager()
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FloatingActionButton(
                    onClick = {
                    val intent = Intent(context, SummaryActivity::class.java)
                    activityLauncher.launch(intent)
                    },
                    modifier = Modifier
                        .width(180.dp)
                        .height(50.dp)
                    ) {
                    Text("Generate Summary")
                }
                Spacer(modifier = Modifier.width(10.dp))
                FloatingActionButton( onClick = {
                    val intent = Intent(context, QuizActivity::class.java)
                    activityLauncher.launch(intent)
                },
                    modifier = Modifier
                        .width(180.dp)
                        .height(50.dp)
                ) {
                    Text("Generate Quiz")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookPager() {
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        10
    }
    val coroutineScope = rememberCoroutineScope()
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        HorizontalPager(
            state = pagerState
        ) { pageIndex ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        "Alice was beginning to get very tired of sitting by her sister on the bank, " +
                                "and of having nothing to do: once or twice she had peeped into the book her " +
                                "sister was reading, but it had no pictures or conversations in it, 'and what is the " +
                                "use of a book, thought Alice 'without pictures or conversation?'" +
                                "\n\n So she was considering in her own mind (as well as she could, for the hot day" +
                                " made her feel very sleepy and stupid), whether the pleasure of making a daisy-chain" +
                                " would be worth the trouble of getting up and picking the daisies, when suddenly" +
                                " a White Rabbit with pink eyes ran close by her.",
                        fontSize = 20.sp,
                        lineHeight = 23.sp,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = (pageIndex+1).toString()+ " / 611")
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(name: String) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }
    TopAppBar(
        title = { Text(text = name) },
        navigationIcon = {
            IconButton(onClick = {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activityLauncher.launch(intent)
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Arrow Back"
                )
            }
        },
        actions = {
            IconButton(onClick = {expanded = !expanded}) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }
        }
    )

}