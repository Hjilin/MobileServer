package com.mobileserver

import android.app.Application
import android.util.Log
import com.mobileserver.core.Paths
import com.mobileserver.util.LogManager
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MobileServerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Paths.init(this)
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            try {
                LogManager.append("crash", "[crash] ${t.name}: $sw")
            } catch (_: Exception) {}
            Log.e("MobileServer", "CRASH", e)
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}
