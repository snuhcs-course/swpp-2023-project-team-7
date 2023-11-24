package com.example.readability.data.user

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
import androidx.room.Update
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Entity
data class User(
    @ColumnInfo(name = "username") val userName: String?,
    @PrimaryKey @ColumnInfo(name = "email") val userEmail: String,
    @ColumnInfo(name = "refresh_token") val refreshToken: String?,
    @ColumnInfo(name = "refresh_token_life") val refreshTokenLife: Long?,
    @ColumnInfo(name = "access_token") val accessToken: String?,
    @ColumnInfo(name = "access_token_life") val accessTokenLife: Long?,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "verified") val verified: Int?,
)

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun get(): Flow<User?>

    @Insert
    fun insert(user: User)

    @Query("DELETE FROM User")
    fun deleteAll()

    @Update
    fun update(user: User)

    @Query("UPDATE User SET access_token = :accessToken, access_token_life = :accessTokenLife")
    fun updateAccessToken(accessToken: String, accessTokenLife: Long)

    @Query("UPDATE User SET username = :username, email = :email, created_at = :createdAt, verified = :verified")
    fun updateUserInfo(username: String, email: String, createdAt: String, verified: Int)
}

@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@InstallIn(SingletonComponent::class)
@Module
class UserDatabaseModule {
    @Provides
    fun provideUserDao(userDatabase: UserDatabase): UserDao {
        return userDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext appContext: Context): UserDatabase {
        return Room.databaseBuilder(
            appContext,
            UserDatabase::class.java,
            "User",
        ).build()
    }
}
