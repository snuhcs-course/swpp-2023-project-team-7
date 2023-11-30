package com.example.readability.data.book

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
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
import java.util.Date
import javax.inject.Singleton

class BookTypeConverters {
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
data class BookEntity(
    @PrimaryKey @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "progress") val progress: Double,
    @ColumnInfo(name = "cover_image") val coverImage: String?,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "last_read") val lastRead: Date = Date(0),
    @ColumnInfo(name = "summary_progress") val summaryProgress: Double = 0.0,
)

@Dao
interface BookDao {
    @Query("SELECT * FROM BookEntity")
    fun getAll(): List<BookEntity>

    @Query("SELECT * FROM BookEntity WHERE book_id = :bookId")
    fun getBook(bookId: Int): BookEntity?

    @Query("SELECT summary_progress FROM BookEntity WHERE book_id = :bookId")
    fun getSummaryProgress(bookId: Int): Double?

    @Insert
    fun insert(book: BookEntity)

    @Insert
    fun insertAll(vararg books: BookEntity)

    @Update
    fun update(book: BookEntity)

    @Query("DELETE FROM BookEntity WHERE book_id = :bookId")
    fun delete(bookId: Int)

    @Query("DELETE FROM BookEntity")
    fun deleteAll()

    @Query("UPDATE BookEntity SET progress = :progress WHERE book_id = :bookId")
    fun updateProgress(bookId: Int, progress: Double)

    @Query("UPDATE BookEntity SET summary_progress = :summaryProgress WHERE book_id = :bookId")
    fun updateSummaryProgress(bookId: Int, summaryProgress: Double)
}

@Database(entities = [BookEntity::class], version = 3)
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
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
