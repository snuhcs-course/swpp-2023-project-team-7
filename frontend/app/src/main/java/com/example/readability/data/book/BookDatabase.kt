package com.example.readability.data.book

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.ByteArrayOutputStream
import java.util.Date
import javax.inject.Singleton

class BookTypeConverters {
    @TypeConverter
    fun toByteArray(imageBitmap: ImageBitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toImageBitmap(byteArray: ByteArray): ImageBitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Entity
data class Book(
    @PrimaryKey @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "progress") val progress: Double,
    @ColumnInfo(name = "cover_image") val coverImage: String?,
    @ColumnInfo(
        name = "cover_image_data",
    ) val coverImageData: ImageBitmap? = null,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "content_data") val contentData: String? = null,
    @ColumnInfo(name = "last_read") val lastRead: Date = Date(0),
)

@Dao
interface BookDao {
    @Query("SELECT * FROM Book")
    fun getAll(): List<Book>

    @Query("SELECT * FROM Book WHERE book_id = :bookId")
    fun getBook(bookId: Int): Book?

    @Insert
    fun insert(book: Book)

    @Insert
    fun insertAll(vararg books: Book)

    @Update
    fun update(book: Book)

    @Delete
    fun delete(book: Book)

    @Query("DELETE FROM Book")
    fun deleteAll()

    @Query("UPDATE Book SET progress = :progress WHERE book_id = :bookId")
    fun updateProgress(bookId: Int, progress: Double)

    @Query("UPDATE Book SET cover_image_data = :coverImageData WHERE book_id = :bookId")
    fun updateCoverImageData(bookId: Int, coverImageData: ImageBitmap?)

    @Query("UPDATE Book SET content_data = :contentData WHERE book_id = :bookId")
    fun updateContentData(bookId: Int, contentData: String?)
}

@Database(entities = [Book::class], version = 2)
@TypeConverters(BookTypeConverters::class)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}

@InstallIn(SingletonComponent::class)
@Module
class BookDatabaseModule {
    @Provides
    fun provideBookDao(bookDatabase: BookDatabase): BookDao {
        return bookDatabase.bookDao()
    }

    @Provides
    @Singleton
    fun provideBookDatabase(@ApplicationContext appContext: Context): BookDatabase {
        return Room.databaseBuilder(
            appContext,
            BookDatabase::class.java,
            "Book",
        ).build()
    }
}
