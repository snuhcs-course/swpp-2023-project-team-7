package com.example.readability.data.viewer

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepository @Inject constructor(
    private val settingDao: SettingDao,
    @ApplicationContext private val context: Context,
) {
    val sampleText = MutableStateFlow("")
    val viewerStyle = settingDao.get().map {
        println("SettingRepository: viewerStyle: $it")
        it ?: ViewerStyleBuilder().build()
    }.stateIn(CoroutineScope(Dispatchers.Main), SharingStarted.Eagerly, ViewerStyleBuilder().build())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (settingDao.get().firstOrNull() == null) {
                println("SettingRepository: viewerStyle is null")
                resetViewerStyle()
            }
            // sample text
            try {
                context.assets.open("sample_text.txt").bufferedReader().use {
                    sampleText.value = it.readText()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetViewerStyle() {
        settingDao.delete()
        settingDao.insert(ViewerStyleBuilder().build())
    }

    fun updateViewerStyle(viewerStyle: ViewerStyle) {
        settingDao.update(viewerStyle)
    }
}
