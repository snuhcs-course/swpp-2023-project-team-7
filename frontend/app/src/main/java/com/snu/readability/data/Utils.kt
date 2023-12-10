package com.snu.readability.data

import okhttp3.ResponseBody
import org.json.JSONObject

fun parseErrorBody(errorBody: ResponseBody?): String {
    return try {
        val errorObject = JSONObject(errorBody!!.string())
        errorObject.getString("detail")
    } catch (e: Exception) {
        errorBody?.string() ?: "Unknown Error"
    }
}
