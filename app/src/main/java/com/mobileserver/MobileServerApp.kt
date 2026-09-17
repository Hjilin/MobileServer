package com.mobileserver

import android.app.Application
import com.mobileserver.core.Paths

class MobileServerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Paths.init(this)
    }
}
