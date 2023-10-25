package com.example.shareader.ui.models

import com.example.shareader.SHAReaderApplication
import com.example.shareader.ui.PageSplitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class PageSplitData(
    var pageSplits: List<Int>,
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
    private val pageSplitter = PageSplitter()
    private val splitScope = CoroutineScope(Dispatchers.Default)
    private var splitJob: Job? = null

    companion object {
        private var instance: BookModel? = null

        fun getInstance(): BookModel {
            if (instance == null) {
                instance = BookModel()
            }
            return instance!!
        }
    }

    var bookList = MutableStateFlow<MutableMap<String, BookData>>(HashMap<String, BookData>())

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
            bookList.value["1"] = BookData(
                id = "1",
                title = "The Open Boat",
                author = "Stephen Crane",
                content = content,
                coverImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/TheOpenBoat.jpg/220px-TheOpenBoat.jpg"
            )
        }
    }

    fun setPageSize(width: Int, height: Int, bookId: String) {
        val bookData = bookList.value[bookId] ?: return
        val pageSplitData = bookData.pageSplitData
        println("Compare: ${pageSplitData?.width}, ${pageSplitData?.height}, $width, $height")
        if (pageSplitData != null && pageSplitData.width == width && pageSplitData.height == height) {
            return
        }


        val lastJob = splitJob
        splitJob = splitScope.launch {
            lastJob?.cancelAndJoin()
            pageSplitter.splitPage(width, height, bookData) {
                bookList.value = bookList.value.toMutableMap().apply {
                    this[bookId] = it
                }
            }
        }
    }
}