import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shareader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(onDismiss: () -> Unit) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp, bottom = 60.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ){
            BookInfo()
            SyncStatus()
            Progress()
            Actions()
        }
    }
}

@Composable
fun BookInfo(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ){
        Card(
            modifier = Modifier
                .width(90.dp)
                .height(120.dp)
        ) {
            Image(
                painterResource(R.drawable.book_image),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(text = "Alice's Adventures in Wonderland", fontWeight = FontWeight.Bold, fontSize = 20.sp,
            modifier = Modifier.padding(top = 15.dp))
        Text(text = "Lewis Caroll")
    }
}

@Composable
fun SyncStatus(){
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Sync Status", fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text("Synced")
        }
    }

}

@Composable
fun Progress(){
    Row(
        modifier = Modifier.fillMaxWidth().padding(all = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Progress", fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text("13/193 pages(8%)")
        }
    }
}


@Composable
fun Actions(){
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(bottom = 20.dp)
    ){
        Text("Actions", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(10.dp))
        Text("Clear Progress", modifier = Modifier.padding(10.dp))
        Text("Mark as completed", modifier = Modifier.padding(10.dp))
        Text("Delete From My library", modifier = Modifier.padding(10.dp))


    }
}