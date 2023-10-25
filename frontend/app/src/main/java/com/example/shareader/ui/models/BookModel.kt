package com.example.shareader.ui.models

import com.example.shareader.SHAReaderApplication
import kotlinx.coroutines.flow.MutableStateFlow

data class PageSplitData(
    val pageSplits: List<Int>,
    val width: Int,
    val height: Int,
)
data class BookData(
    val id: String,
    val title: String,
    val author: String,
    val content: String,
    val coverImage: String,
    val progress: Double = 0.0,
    val pageSplitData: PageSplitData? = null,
)

data class BookCardData(
    val id: String,
    val title: String,
    val author: String,
    val progress: Double,
    val coverImage: String,
)

class BookModel {
    companion object {
        private var instance: BookModel? = null

        fun getInstance(): BookModel {
            if (instance == null) {
                instance = BookModel()
            }
            return instance!!
        }
    }

    val bookList = MutableStateFlow(listOf<BookData>())

    init {
        // TODO: load book list from local storage
        if (SHAReaderApplication.instance != null) {
            val context = SHAReaderApplication.instance!!.applicationContext
            // load the_open_boat.txt from assets folder
            val reader = context.assets.open("the_open_boat.txt").bufferedReader()
            val content = reader.readText()
            reader.close()

            println("added book")

            // add sample book
            bookList.value += BookData(
                id = "1",
                title = "The Open Boat",
                author = "Stephen Crane",
                content = content,
                coverImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/TheOpenBoat.jpg/220px-TheOpenBoat.jpg"
            )
        }
    }
}