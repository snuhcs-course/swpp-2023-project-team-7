package com.example.shareader

import android.app.Application

class SHAReaderApplication: Application() {
    init {
        instance = this
    }

    companion object {
        var instance: SHAReaderApplication? = null
    }
}