package com.example.readability.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import java.util.Date

@Entity
data class User(
    @ColumnInfo(name = "username") val userName: String?,
    @PrimaryKey @ColumnInfo(name = "useremail") val userEmail: String,
    @ColumnInfo(name = "refresh_token") val refreshToken: String?,
    @ColumnInfo(name = "refresh_token_life") val refreshTokenLife: Long?,
    @ColumnInfo(name = "access_token") val accessToken: String?,
    @ColumnInfo(name = "access_token_life") val accessTokenLife: Long?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Insert
    fun insert(user: User)

    @Query("DELETE FROM user")
    fun deleteAll()

    @Update
    fun update(user: User)

    @Query("UPDATE user SET access_token = :accessToken")
    fun updateAccessToken(accessToken: String)
}

@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}