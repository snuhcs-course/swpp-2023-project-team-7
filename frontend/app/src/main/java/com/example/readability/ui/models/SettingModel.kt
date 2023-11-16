package com.example.readability.ui.models

import android.content.Context
import com.example.readability.ReadabilityApplication
import kotlinx.coroutines.flow.MutableStateFlow

class SettingModel {
    val sampleText = MutableStateFlow("")

    private fun readAsset(context: Context, fileName: String): String {
        val reader = context.assets.open(fileName).bufferedReader()
        val content = reader.readText()
        reader.close()
        return content
    }
    companion object {
        @Volatile
        private var instance: SettingModel? = null

        fun getInstance(): SettingModel {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SettingModel()
                    }
                }
            }
            return instance!!
        }
    }

    init {
        if (ReadabilityApplication.instance != null) {
            val context = ReadabilityApplication.instance!!.applicationContext

            // add sample book
            sampleText.value = readAsset(context, "sample_text.txt")
        }
    }
}