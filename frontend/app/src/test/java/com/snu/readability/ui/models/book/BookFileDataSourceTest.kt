package com.snu.readability.ui.models.book

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.snu.readability.data.book.BookFileDataSource
import com.snu.readability.data.book.FileHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class BookFileDataSourceTest {

    private lateinit var fileHelper: FileHelper
    private lateinit var bookFileDataSource: BookFileDataSource

    @Before
    fun setup() {
        fileHelper = mockk(relaxed = true)
        bookFileDataSource = BookFileDataSource(fileHelper)
    }

    @Test
    fun `contentExists should call FileHelper exists`() = runTest {
        // Arrange
        coEvery { fileHelper.exists(any()) } returns true

        // Act
        val result = bookFileDataSource.contentExists(1)

        // Assert
        assert(result)
        coVerify { fileHelper.exists("/book_content/1.txt") }
    }

    @Test
    fun `readContentFile should call FileHelper openFileInputStream and bufferedReader`() = runTest {
        // Arrange
        coEvery { fileHelper.exists(any()) } returns true
        coEvery { fileHelper.openFileInputStream(any()) } returns mockk()
        coEvery { fileHelper.openFileInputStream(any()).bufferedReader() } returns mockk()

        // Act
        bookFileDataSource.readContentFile(1)

        // Assert
        coVerify { fileHelper.openFileInputStream("/book_content/1.txt") }
        coVerify { fileHelper.openFileInputStream("/book_content/1.txt").bufferedReader() }
    }

    @Test
    fun `writeContentFile should call FileHelper openFileOutputStream and bufferedWriter`() = runTest {
        // Arrange
        coEvery { fileHelper.openFileOutputStream(any()) } returns mockk()
        coEvery { fileHelper.openFileOutputStream(any()).bufferedWriter() } returns mockk()

        // Act
        bookFileDataSource.writeContentFile(1, "content")

        // Assert
        coVerify { fileHelper.openFileOutputStream("/book_content/1.txt") }
        coVerify { fileHelper.openFileOutputStream("/book_content/1.txt").bufferedWriter() }
    }

    @Test
    fun `deleteContentFile should call FileHelper deleteFile`() = runTest {
        // Act
        bookFileDataSource.deleteContentFile(1)

        // Assert
        coVerify { fileHelper.deleteFile("/book_content/1.txt") }
    }

    @Test
    fun `coverImageExists should call FileHelper exists`() = runTest {
        // Arrange
        coEvery { fileHelper.exists(any()) } returns true

        // Act
        val result = bookFileDataSource.coverImageExists(1)

        // Assert
        assert(result)
        coVerify { fileHelper.exists("/book_cover/1.png") }
    }

    @Test
    fun `readCoverImageFile should call FileHelper openFileInputStream and use BitmapFactory`() = runTest {
        // Arrange
        val byteArray = ByteArrayOutputStream().use {
            // Create a sample image
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                .compress(Bitmap.CompressFormat.PNG, 100, it)
            it.toByteArray()
        }
        coEvery { fileHelper.exists(any()) } returns true
        coEvery { fileHelper.openFileInputStream(any()) } returns ByteArrayInputStream(byteArray)

        // Act
        bookFileDataSource.readCoverImageFile(1)

        // Assert
        coVerify { fileHelper.openFileInputStream("/book_cover/1.png") }
    }

    @Test
    fun `writeCoverImageFile should call FileHelper openFileOutputStream and compress`() = runTest {
        // Arrange
        val imageBitmap: ImageBitmap = spyk(
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).asImageBitmap(),
        )
        coEvery { imageBitmap.asAndroidBitmap() } returns mockk()
        coEvery { fileHelper.openFileOutputStream(any()) } returns mockk()

        // Act
        bookFileDataSource.writeCoverImageFile(1, imageBitmap)

        // Assert
        coVerify { fileHelper.openFileOutputStream("/book_cover/1.png") }
    }

    @Test
    fun `deleteCoverImageFile should call FileHelper deleteFile`() = runTest {
        // Act
        bookFileDataSource.deleteCoverImageFile(1)

        // Assert
        coVerify { fileHelper.deleteFile("/book_cover/1.png") }
    }

    @Test
    fun `deleteAll should call FileHelper resetDirectory`() = runTest {
        // Act
        bookFileDataSource.deleteAll()

        // Assert
        coVerify { fileHelper.resetDirectory("/book_cover") }
        coVerify { fileHelper.resetDirectory("/book_content") }
    }
}
