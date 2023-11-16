package com.example.readability.ui.models

import android.content.Context
import com.example.readability.ReadabilityApplication
import com.example.readability.ui.PageSplitter
import com.example.readability.ui.ViewerStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PageSplitData(
    var pageSplits: List<Int>,
    val width: Int,
    val height: Int,
    val viewerStyle: ViewerStyle
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
    val pageSplitter = PageSplitter()
    private val splitScope = CoroutineScope(Dispatchers.Default)
    private var splitJob: Job? = null

    companion object {
        @Volatile
        private var instance: BookModel? = null

        fun getInstance(): BookModel {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = BookModel()
                    }
                }
            }
            return instance!!
        }
    }

    var bookList = MutableStateFlow<MutableMap<String, BookData>>(HashMap<String, BookData>())

    private fun readAsset(context: Context, fileName: String): String {
        val reader = context.assets.open(fileName).bufferedReader()
        val content = reader.readText()
        reader.close()
        return content
    }

    init {
        // TODO: load book list from local storage
        if (ReadabilityApplication.instance != null) {
            val context = ReadabilityApplication.instance!!.applicationContext

            // add sample book
            bookList.value["1"] = BookData(
                id = "1",
                title = "The Open Boat",
                author = "Stephen Crane",
                content = readAsset(context, "the_open_boat.txt"),
                coverImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/TheOpenBoat.jpg/220px-TheOpenBoat.jpg"
            )
            bookList.value["2"] = BookData(
                id = "2",
                title = "An introduction to the study of fishes",
                author = "Albert Carl Ludwig Gotthilf Günther",
                content = readAsset(context, "an_introduction_to_the_study_of_fishes.txt"),
                coverImage = "https://www.gutenberg.org/cache/epub/72060/pg72060.cover.medium.jpg"
            )
        }
    }

    fun setPageSize(width: Int, height: Int, bookId: String) {
        val bookData = bookList.value[bookId] ?: return
        val pageSplitData = bookData.pageSplitData
        if (pageSplitData != null && pageSplitData.width == width && pageSplitData.height == height && pageSplitData.viewerStyle == pageSplitter.viewerStyle.value) {
            return
        }

        bookList.update {
            it.toMutableMap().apply {
                this[bookId] = this[bookId]!!.copy(
                    pageSplitData = PageSplitData(
                        pageSplits = emptyList(), width = width, height = height, viewerStyle = pageSplitter.viewerStyle.value
                    )
                )
            }
        }


        val lastJob = splitJob
        splitJob = splitScope.launch {
            lastJob?.cancelAndJoin()
            pageSplitter.splitPage(width, height, bookData) { pageSplitData ->
                bookList.update {
                    it.toMutableMap().apply {
                        this[bookId] = this[bookId]!!.copy(pageSplitData = pageSplitData.copy())
                    }
                }
            }
        }
    }

    fun setProgress(progress: Double, bookId: String) {
        bookList.update {
            val bookData = bookList.value[bookId] ?: return
            it.toMutableMap().apply {
                this[bookId] = bookData.copy(progress = progress)
            }
        }
    }
}