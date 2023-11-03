package com.example.readability.ui.screens.book

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.readability.MainActivity
import com.example.readability.R
import com.example.readability.ui.theme.ReadabilityTheme
import com.example.readability.ui.viewmodels.AddBookViewModel

@Composable
@Preview(showBackground = true, device = "id:pixel_5", showSystemUi = true)
fun AddBookViewPreview() {
    ReadabilityTheme {
        AddBookView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookView(
    addBookViewModel: AddBookViewModel = viewModel()
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var content by remember { mutableStateOf("") }

    val snackbarMessage by addBookViewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val bookUploaded by addBookViewModel.bookUploaded.collectAsState()

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(snackbarMessage)
            addBookViewModel.clearSnackbar()
        }
    }

    LaunchedEffect(bookUploaded) {
        if (bookUploaded) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }

    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
    }

    val imageSelectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            if (bitmap != null) {
                addBookViewModel.uploadImage(bitmap!!)
            }

        }
    }

    val textFileSelectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // get content
        if (uri != null) {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                content = inputStream.bufferedReader().use { it.readText() }
                println(content)
                inputStream.close()
            } else {
                // Go back to main activity
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(Unit) {
        textFileSelectLauncher.launch("text/*")
    }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }, topBar = {
        TopAppBar(title = {
            Text(text = "Add Book")
        }, navigationIcon = {
            IconButton(onClick = {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activityLauncher.launch(intent)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Arrow Back"
                )
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text(text = "Book Title") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.book),
                        contentDescription = "Book Icon"
                    )
                })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                value = author,
                onValueChange = { author = it },
                label = { Text(text = "Author (Optional)") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Book Icon"
                    )
                })
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    bitmap?.let {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(12.dp)
                                ),
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                        )
                    }
                    FilledTonalButton(modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            imageSelectLauncher.launch("image/*")
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.file_upload),
                            contentDescription = "File Upload Icon"
                        )
                        Text(text = if (imageUri == null) "Select Image" else "Change Image")
                    }
                }
                Text(modifier = Modifier
                    .layout { measurable, constraints ->
                        // at the text center
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(
                                x = 8.dp
                                    .toPx()
                                    .toInt(), y = (-placeable.height) / 2
                            )
                        }
                    }
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 4.dp),
                    text = "Book Cover (Optional)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    addBookViewModel.addBook(title, author, "")
                }) {
                Text(text = "Add Book")
            }
        }
    }
}