package com.example.shareader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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

@Composable
fun BookListView() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {


                Text("My Library")
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Sharp.Settings,
                        contentDescription = "setting",
                        modifier = Modifier
                            .width(25.dp)
                            .height(25.dp)
                    )
                }
            }
            BookCard()
        }
    }
}

@Composable
fun BookCard() {
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        // image
        Column {
            Text("Alice's Adventures in Wonderland")
            Text("Lewis Carroll")
            Text("32%")
        }
        Column {
            var showSheet by remember { mutableStateOf(false) }

            if (showSheet) {
                BottomSheet() {
                    showSheet = false
                }
            }

            IconButton(onClick = { }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(onDismiss: () -> Unit) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Text(text = "test")
    }
}
