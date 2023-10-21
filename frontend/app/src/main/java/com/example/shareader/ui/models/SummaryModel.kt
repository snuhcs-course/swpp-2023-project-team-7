package com.example.shareader.ui.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

enum class SummaryLoadState {
    LOADING,
    LOADED,
    ERROR
}

class SummaryModel {
    companion object {
        private var instance: SummaryModel? = null

        fun getInstance(): SummaryModel {
            if (instance == null) {
                instance = SummaryModel()
            }
            return instance!!
        }
    }

    val summaryState = MutableStateFlow("")
    val summaryLoadState = MutableStateFlow(SummaryLoadState.LOADING)
    val summaryLoadScope = CoroutineScope(Dispatchers.IO)

    fun loadSummary(book_id: String, progress: Double) {
        summaryLoadScope.launch {
            summaryState.value = ""
            summaryLoadState.value = SummaryLoadState.LOADING
            withContext(Dispatchers.IO) {
                val requestUrl = "https://swpp.scripter36.com/summary?book_id=$book_id&progress=$progress"
                val conn = URL(requestUrl).openConnection()
                conn.connect()
                val inputStream = conn.getInputStream()
                val reader = inputStream.bufferedReader()

                var myEvent = false
                while (isActive) {
                    val line = reader.readLine() ?: break
                    println(line)
                    if (line.startsWith("event:")) {
                        val eventName = line.split(":")[1].trim()
                        if (eventName == "summary") {
                            myEvent = true
                        }
                    } else if (line.startsWith("data:")) {
                        if (myEvent) {
                            println("summary: $line.substring(6)")
                            summaryState.value += line.substring(6)
                            myEvent = false
                        }
                    }
                }

                summaryLoadState.value = SummaryLoadState.LOADED
            }
        }

    }
}