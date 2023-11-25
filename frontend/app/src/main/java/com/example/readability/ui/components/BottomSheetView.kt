import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.data.book.BookCardData
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(bookCardData: BookCardData, onDismiss: () -> Unit, onProgressChanged: (Int, Double) -> Unit, onBookDeleted: (Int) -> Unit = {}) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val closeScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },

    ) {
        BottomSheetContent(
            modifier = Modifier.fillMaxWidth(),
            bookCardData = bookCardData,
            onDismiss = {
                closeScope.launch {
                    modalBottomSheetState.hide()
                    onDismiss()
                }
            },
            onProgressChanged = onProgressChanged,
            onBookDeleted = onBookDeleted,
        )
    }
}

@Composable
@Preview(showBackground = false)
fun BottomSheetPreview() {
    ReadabilityTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clip(RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp))
                .padding(0.dp, 48.dp, 0.dp, 0.dp),
        ) {
            BottomSheetContent(
                modifier = Modifier.fillMaxSize(),
                bookCardData = BookCardData(
                    id = 1,
                    title = "The Open Boat",
                    author = "Stephen Crane",
                    progress = 0.5,
                    coverImageData = null,
                    content = "asd",
                ),
            )
        }
    }
}

@Composable
fun BottomSheetContent(
    modifier: Modifier = Modifier,
    bookCardData: BookCardData,
    onDismiss: () -> Unit = {},
    onProgressChanged: (Int, Double) -> Unit = { _, _ -> },
    onBookDeleted: (Int) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BookInfo(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            coverImage = bookCardData.coverImageData,
            title = bookCardData.title,
            author = bookCardData.author,
        )
        SyncStatus(
            modifier = Modifier.fillMaxWidth(),
        )
        Progress(
            modifier = Modifier.fillMaxWidth(),
            progress = bookCardData.progress,
        )
        Actions(modifier = Modifier.fillMaxWidth(), onActionClicked = {
            when (it) {
                BookAction.ClearProgress -> {
                    onProgressChanged(bookCardData.id, 0.0)
                    onDismiss()
                }

                BookAction.MarkAsCompleted -> {
                    onProgressChanged(bookCardData.id, 1.0)
                    onDismiss()
                }

                BookAction.DeleteFromMyLibrary -> {
                    // TODO
                    onBookDeleted(bookCardData.id)
                    onDismiss()
                }
            }
        })
    }
}

@Composable
fun BookInfo(modifier: Modifier = Modifier, coverImage: ImageBitmap?, title: String, author: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (coverImage == null) {
            Box(
                modifier = Modifier.height(120.dp),
            )
        } else {
            Image(
                bitmap = coverImage,
                contentDescription = "Book Image",
                modifier = Modifier.width(90.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = author,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun SyncStatus(modifier: Modifier) {
    Row(
        modifier = modifier.padding(16.dp, 16.dp, 24.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Sync Status",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Synced",
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}

@Composable
fun Progress(modifier: Modifier = Modifier, progress: Double) {
    Row(
        modifier = modifier.padding(16.dp, 16.dp, 24.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Progress",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "${(progress * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}

enum class BookAction {
    ClearProgress,
    MarkAsCompleted,
    DeleteFromMyLibrary,


}

@Composable
fun Actions(modifier: Modifier = Modifier, onActionClicked: (BookAction) -> Unit = {}) {
    Column(modifier = modifier) {
        Text(
            "Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 8.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onActionClicked(BookAction.ClearProgress)
                }
                .padding(16.dp),
        ) {
            Icon(
                painterResource(id = R.drawable.arrow_bend_down_left),
                contentDescription = "clear progress",
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Clear Progress",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onActionClicked(BookAction.MarkAsCompleted)
                }
                .padding(16.dp),
        ) {
            Icon(
                painterResource(id = R.drawable.check),
                contentDescription = "mark",
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Mark As Completed",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onActionClicked(BookAction.DeleteFromMyLibrary)
                }
                .padding(16.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.trash),
                contentDescription = "delete",
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Delete From My library",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error),
            )
        }
    }
}
