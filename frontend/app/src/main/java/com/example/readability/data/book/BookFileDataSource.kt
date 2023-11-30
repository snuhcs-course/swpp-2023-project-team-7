package com.example.readability.data.book

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    init {
        if (!File(context.filesDir.path + "/book_cover").exists()) {
            File(context.filesDir.path + "/book_cover").mkdir()
        }
        if (!File(context.filesDir.path + "/book_content").exists()) {
            File(context.filesDir.path + "/book_content").mkdir()
        }
    }
    fun contentExists(bookId: Int): Boolean {
        val bookContentPath = context.filesDir.path + "/book_content/$bookId.txt"
        return try {
            File(bookContentPath).exists()
        } catch (e: Exception) {
            println("BookFileDataSource: contentExists failed: ${e.message}")
            false
        }
    }
    fun readContentFile(bookId: Int): String? {
        val bookContentPath = context.filesDir.path + "/book_content/$bookId.txt"
        return if (contentExists(bookId)) {
            try {
                FileInputStream(bookContentPath).bufferedReader().use {
                    it.readText()
                }
            } catch (e: Exception) {
                println("BookFileDataSource: readContentFile failed: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    fun writeContentFile(bookId: Int, content: String) {
        val bookContentPath = context.filesDir.path + "/book_content/$bookId.txt"
        try {
            FileOutputStream(bookContentPath).bufferedWriter().use {
                it.write(content)
            }
        } catch (e: Exception) {
            println("BookFileDataSource: writeContentFile failed: ${e.message}")
        }
    }

    fun deleteContentFile(bookId: Int) {
        val bookContentPath = context.filesDir.path + "/book_content/$bookId.txt"
        try {
            File(bookContentPath).delete()
        } catch (e: Exception) {
            println("BookFileDataSource: deleteContentFile failed: ${e.message}")
        }
    }

    fun coverImageExists(bookId: Int): Boolean {
        val coverImagePath = context.filesDir.path + "/book_cover/$bookId.png"
        println("BookFileDataSource: check for exist: $coverImagePath")
        return try {
            File(coverImagePath).exists()
        } catch (e: Exception) {
            println("BookFileDataSource: coverImageExists failed: ${e.message}")
            false
        }
    }

    fun readCoverImageFile(bookId: Int): ImageBitmap? {
        val coverImagePath = context.filesDir.path + "/book_cover/$bookId.png"
        return if (coverImageExists(bookId)) {
            try {
                FileInputStream(coverImagePath).use {
                    val byteArray = it.readBytes()
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
                }
            } catch (e: Exception) {
                println("BookFileDataSource: readCoverImageFile failed: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    fun writeCoverImageFile(bookId: Int, coverImage: ImageBitmap) {
        val coverImagePath = context.filesDir.path + "/book_cover/$bookId.png"
        println("BookFileDataSource: write cover image: $coverImagePath")
        try {
            FileOutputStream(coverImagePath).use {
                coverImage.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } catch (e: Exception) {
            println("BookFileDataSource: writeCoverImageFile failed: ${e.message}")
        }
    }

    fun deleteCoverImageFile(bookId: Int) {
        val coverImagePath = context.filesDir.path + "/book_cover/$bookId.png"
        try {
            File(coverImagePath).delete()
        } catch (e: Exception) {
            println("BookFileDataSource: deleteCoverImageFile failed: ${e.message}")
        }
    }

    fun deleteAll() {
        try {
            File(context.filesDir.path + "/book_cover").deleteRecursively()
            File(context.filesDir.path + "/book_cover").mkdir()
            File(context.filesDir.path + "/book_content").deleteRecursively()
            File(context.filesDir.path + "/book_content").mkdir()
        } catch (e: Exception) {
            println("BookFileDataSource: clearAll failed: ${e.message}")
        }
    }
}
