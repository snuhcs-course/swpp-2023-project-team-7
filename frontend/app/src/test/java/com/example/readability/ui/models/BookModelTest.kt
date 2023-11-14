package com.example.readability.ui.models

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

class BookModelTest {

    @Test
    @org.junit.jupiter.api.Test
    fun setProgress_succeed() {
        val bookModel = BookModel.getInstance()
        bookModel.bookList.value["1"] = BookData(
            id = "1",
            title = "The Open Boat",
            author = "Stephen Crane",
            content = "content",
            coverImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/TheOpenBoat.jpg/220px-TheOpenBoat.jpg"
        )
        bookModel.setProgress(0.5, "1")
        assertEquals(0.5, bookModel.bookList.value["1"]?.progress)
    }
}