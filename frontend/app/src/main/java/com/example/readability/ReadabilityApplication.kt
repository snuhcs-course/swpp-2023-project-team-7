package com.example.readability

import android.app.Application

class ReadabilityApplication: Application() {
    init {
        instance = this
    }

    companion object {
        var instance: ReadabilityApplication? = null
    }
}