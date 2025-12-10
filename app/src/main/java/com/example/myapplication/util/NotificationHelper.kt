package com.example.myapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.R

/**
 * 通知帮助类
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "event_reminders"
        const val CHANNEL_NAME = "事件提醒"
        const val CHANNEL_DESCRIPTION = "当到了设定的时间时提醒记录事件"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(eventId: String, eventName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 权限未授予，无法发送通知
                return
            }
        }

        // TODO: 添加点击跳转到详情页的 PendingIntent
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 使用系统图标作为临时图标
            .setContentTitle("该记录 $eventName 了")
            .setContentText("到了您设定的时间，快来记录一下吧！")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // eventId.hashCode() 作为通知 ID
            notify(eventId.hashCode(), builder.build())
        }
    }
}
