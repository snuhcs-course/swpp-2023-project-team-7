package com.example.readability.data.viewer

import android.content.Context
import androidx.compose.ui.graphics.toArgb
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
import com.example.readability.ui.theme.md_theme_dark_background
import com.example.readability.ui.theme.md_theme_dark_onBackground
import com.example.readability.ui.theme.md_theme_light_background
import com.example.readability.ui.theme.md_theme_light_onBackground
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Entity
data class ViewerStyle(
    @PrimaryKey @ColumnInfo("id") val id: Int = 0,
    @ColumnInfo("text_size") val textSize: Float = 24f,
    @ColumnInfo("line_height") val lineHeight: Float = 1.2f,
    @ColumnInfo("letter_spacing") val letterSpacing: Float = 0f,
    @ColumnInfo("paragraph_spacing") val paragraphSpacing: Float = 1.2f,
    @ColumnInfo("font_family") val fontFamily: String = "garamond",
    @ColumnInfo("vertical_padding") val verticalPadding: Float = 16f,
    @ColumnInfo("horizontal_padding") val horizontalPadding: Float = 16f,
    @ColumnInfo("bright_background_color") val brightBackgroundColor: Int = md_theme_light_background.toArgb(),
    @ColumnInfo("dark_background_color") val darkBackgroundColor: Int = md_theme_dark_background.toArgb(),
    @ColumnInfo("bright_text_color") val brightTextColor: Int = md_theme_light_onBackground.toArgb(),
    @ColumnInfo("dark_text_color") val darkTextColor: Int = md_theme_dark_onBackground.toArgb(),
)

class ViewerStyleBuilder(private var viewerStyle: ViewerStyle = ViewerStyle()) {
    fun id(id: Int) = apply { viewerStyle = viewerStyle.copy(id = id) }
    fun textSize(textSize: Float) = apply { viewerStyle = viewerStyle.copy(textSize = textSize) }
    fun lineHeight(lineHeight: Float) = apply { viewerStyle = viewerStyle.copy(lineHeight = lineHeight) }
    fun letterSpacing(letterSpacing: Float) = apply { viewerStyle = viewerStyle.copy(letterSpacing = letterSpacing) }
    fun paragraphSpacing(paragraphSpacing: Float) = apply { viewerStyle = viewerStyle.copy(paragraphSpacing = paragraphSpacing) }
    fun fontFamily(fontFamily: String) = apply { viewerStyle = viewerStyle.copy(fontFamily = fontFamily) }
    fun verticalPadding(verticalPadding: Float) = apply { viewerStyle = viewerStyle.copy(verticalPadding = verticalPadding) }
    fun horizontalPadding(horizontalPadding: Float) = apply { viewerStyle = viewerStyle.copy(horizontalPadding = horizontalPadding) }
    fun brightBackgroundColor(brightBackgroundColor: Int) = apply { viewerStyle = viewerStyle.copy(brightBackgroundColor = brightBackgroundColor) }
    fun darkBackgroundColor(darkBackgroundColor: Int) = apply { viewerStyle = viewerStyle.copy(darkBackgroundColor = darkBackgroundColor) }
    fun brightTextColor(brightTextColor: Int) = apply { viewerStyle = viewerStyle.copy(brightTextColor = brightTextColor) }
    fun darkTextColor(darkTextColor: Int) = apply { viewerStyle = viewerStyle.copy(darkTextColor = darkTextColor) }
    fun build() = viewerStyle
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM ViewerStyle")
    fun get(): Flow<ViewerStyle?>

    @Insert
    fun insert(viewerStyle: ViewerStyle)

    @Update
    fun update(viewerStyle: ViewerStyle)

    @Query("DELETE FROM ViewerStyle")
    fun delete()
}

@Database(entities = [ViewerStyle::class], version = 1)
abstract class SettingDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingDao
}

@InstallIn(SingletonComponent::class)
@Module
class SettingDatabaseProviderModule {
    @Provides
    fun provideSettingDao(settingDatabase: SettingDatabase): SettingDao {
        return settingDatabase.settingDao()
    }

    @Provides
    @Singleton
    fun provideSettingDatabase(@ApplicationContext context: Context): SettingDatabase {
        return Room.databaseBuilder(
            context,
            SettingDatabase::class.java,
            "Setting",
        ).build()
    }
}
