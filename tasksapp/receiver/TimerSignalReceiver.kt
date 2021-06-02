package com.example.tasksapp.receiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.tasksapp.R
import com.example.tasksapp.service.TimerService


const val ID_SIGNAL = 102
class TimerSignalReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager =
            ContextCompat.getSystemService(context,
                NotificationManager::class.java
            ) as NotificationManager

        val title = intent.getStringExtra(TimerService.SIGNAL_TITLE)

        createChannel(context.getString(R.string.timer_signal_channel),
            context.getString(R.string.signal_name), context)

        val notification = NotificationCompat.Builder(context, context.getString(R.string.timer_signal_channel)).apply {
            setSmallIcon(R.drawable.ic_foreground_icon)
            setContentTitle(title)
            setCategory(NotificationCompat.CATEGORY_ALARM)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }.build()

        notificationManager.notify(ID_SIGNAL, notification)
        Log.e("Timer_Signal", "Broadcast was launched")
    }

    private fun createChannel(channelId: String, channelName: String, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Timer signal"
            }

            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}