package com.mobileserver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.mobileserver.R
import com.mobileserver.engine.EngineController

/**
 * 前台服务：保活所有后端服务进程，Android 12+必需
 */
class ServerForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "mobile_server_channel"
        const val NOTIFY_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, ServerForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ServerForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFY_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        EngineController.stopAll()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "本地服务器服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持本地Nginx/PHP/数据库服务后台运行"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("MobileServer 运行中")
            .setContentText("Nginx + PHP + MariaDB + OpenList 已启动")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }
}
