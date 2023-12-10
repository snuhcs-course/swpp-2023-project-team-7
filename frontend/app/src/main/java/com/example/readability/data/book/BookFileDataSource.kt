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
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

class FileHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun openFileInputStream(fileName: String): InputStream {
        return FileInputStream(context.filesDir.path + fileName)
    }

    fun openFileOutputStream(fileName: String): OutputStream {
        return FileOutputStream(context.filesDir.path + fileName)
    }

    fun deleteFile(fileName: String) {
        File(context.filesDir.path + fileName).delete()
    }

    fun exists(fileName: String): Boolean {
        return File(context.filesDir.path + fileName).exists()
    }

    fun mkdirsIfNotExists(fileName: String) {
        if (!File(context.filesDir.path + fileName).exists()) {
            File(context.filesDir.path + fileName).mkdirs()
        }
    }

    fun resetDirectory(fileName: String) {
        File(context.filesDir.path + fileName).deleteRecursively()
        File(context.filesDir.path + fileName).mkdir()
    }
}

@Singleton
class BookFileDataSource @Inject constructor(
    private val fileHelper: FileHelper,
) {
    init {
        fileHelper.mkdirsIfNotExists("/book_cover")
        fileHelper.mkdirsIfNotExists("/book_content")
    }

    fun contentExists(bookId: Int): Boolean {
        return try {
            fileHelper.exists("/book_content/$bookId.txt")
        } catch (e: Exception) {
            println("BookFileDataSource: contentExists failed: ${e.message}")
            false
        }
    }
    fun readContentFile(bookId: Int): String? {
        return if (contentExists(bookId)) {
            try {
                fileHelper.openFileInputStream("/book_content/$bookId.txt").bufferedReader().use {
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
        try {
            fileHelper.openFileOutputStream("/book_content/$bookId.txt").bufferedWriter().use {
                it.write(content)
            }
        } catch (e: Exception) {
            println("BookFileDataSource: writeContentFile failed: ${e.message}")
        }
    }

    fun deleteContentFile(bookId: Int) {
        try {
            fileHelper.deleteFile("/book_content/$bookId.txt")
        } catch (e: Exception) {
            println("BookFileDataSource: deleteContentFile failed: ${e.message}")
        }
    }

    fun coverImageExists(bookId: Int): Boolean {
        return try {
            fileHelper.exists("/book_cover/$bookId.png")
        } catch (e: Exception) {
            println("BookFileDataSource: coverImageExists failed: ${e.message}")
            false
        }
    }

    fun readCoverImageFile(bookId: Int): ImageBitmap? {
        return if (coverImageExists(bookId)) {
            try {
                fileHelper.openFileInputStream("/book_cover/$bookId.png").use {
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
        try {
            fileHelper.openFileOutputStream("/book_cover/$bookId.png").use {
                coverImage.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } catch (e: Exception) {
            println("BookFileDataSource: writeCoverImageFile failed: ${e.message}")
        }
    }

    fun deleteCoverImageFile(bookId: Int) {
        try {
            fileHelper.deleteFile("/book_cover/$bookId.png")
        } catch (e: Exception) {
            println("BookFileDataSource: deleteCoverImageFile failed: ${e.message}")
        }
    }

    fun deleteAll() {
        try {
            fileHelper.resetDirectory("/book_cover")
            fileHelper.resetDirectory("/book_content")
        } catch (e: Exception) {
            println("BookFileDataSource: clearAll failed: ${e.message}")
        }
    }
}
