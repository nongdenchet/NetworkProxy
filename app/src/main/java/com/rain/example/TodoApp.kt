package com.rain.example

import android.app.Application
import com.rain.networkproxy.NetworkProxy

class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkProxy.init(this, 9000)
    }
}
