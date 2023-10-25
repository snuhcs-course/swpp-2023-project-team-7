package com.example.shareader.ui.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONTokener
import java.net.URL

data class Quiz(
    val id: String,
    val question: String,
    val answer: String,
)

enum class QuizLoadState {
    LOADING,
    LOADED,
    ERROR
}

class QuizModel {
    val quizList = MutableStateFlow(listOf<Quiz>())
    val quizSize = MutableStateFlow(0)
    val quizLoadState = MutableStateFlow(QuizLoadState.LOADING)
    val quizLoadScope = CoroutineScope(Dispatchers.IO)

    val quizLoadUrl = "https://swpp.scripter36.com/quiz"

    companion object {
        private var instance: QuizModel? = null

        fun getInstance(): QuizModel {
            if (instance == null) {
                instance = QuizModel()
            }
            return instance!!
        }
    }

    fun loadQuiz(book_id: String, progress: Double) {
        // SSE
        quizLoadScope.launch {
            quizList.value = listOf()
            quizLoadState.value = QuizLoadState.LOADING
            withContext(Dispatchers.IO) {
                val requestUrl = "$quizLoadUrl?book_id=$book_id&progress=$progress"
                val conn = URL(requestUrl).openConnection()
                conn.connect()
                val inputStream = conn.getInputStream()
                val reader = inputStream.bufferedReader()

                var myEvent = false
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.startsWith("event:")) {
                        val eventName = line.split(":")[1].trim()
                        if (eventName == "quiz") {
                            myEvent = true
                        }
                    } else if (line.startsWith("data:") && myEvent) {
                        val data = line.split(":").drop(1).joinToString(":").trim()
                        val jsonObject = JSONTokener(data).nextValue() as org.json.JSONObject
                        val quizId = jsonObject.getString("quiz_id")
                        val quiz = jsonObject.getJSONObject("quiz")
                        val question = quiz.getString("question")
                        val answer = quiz.getString("answer")
                        val quizLen = jsonObject.getInt("quiz_len")

                        val newQuiz = Quiz(quizId, question, answer)
                        quizList.value += newQuiz
                        quizSize.value = quizLen
                        println("New Quiz Added! id: $quizId, question: $question, answer: $answer, quizLen: $quizLen")
                    }
                }

                quizLoadState.value = QuizLoadState.LOADED
            }
        }
    }
}