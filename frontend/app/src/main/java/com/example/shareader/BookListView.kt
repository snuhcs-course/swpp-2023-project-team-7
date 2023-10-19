package com.example.shareader

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import BottomSheet


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookListView() {
    Scaffold (
        floatingActionButton = { AddButton() }
    ){
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(150.dp))
                    Text("My Library", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(120.dp))
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "setting",
                            modifier = Modifier
                                .width(25.dp)
                                .height(25.dp)
                        )
                    }
                }
                Divider(color = Color.Gray, thickness = 1.dp)
                BookCard()
                BookCard()
                BookCard()
            }
        }
    }
}

@Composable
fun AddButton() {
    FloatingActionButton(
        onClick = {  },
    ) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard() {
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }
    Card(
        onClick = { val intent = Intent(context, ReaderActivity::class.java)
            activityLauncher.launch(intent) },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
    ) {
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            Card(
                modifier = Modifier
                    .width(69.dp)
                    .height(92.dp)
            ) {
                Image(
                    painterResource(R.drawable.book_image),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text("Alice's Adventures in Wonderland")
                Text("Lewis Carroll")
                Spacer(modifier = Modifier.height(20.dp))
                Text("32%")
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                var showSheet by remember { mutableStateOf(false) }
                if (showSheet) {
                    BottomSheet() {
                        showSheet = false
                    }
                }
                IconButton(onClick = {

                }) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "uploaded",
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                }
                IconButton(onClick = { showSheet = true }) {
                    Icon(
                        imageVector = Icons.Sharp.MoreVert,
                        contentDescription = "more",
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp)
                    )
                }
            }
        }
    }
}
