package com.example.tasksapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.tasksapp.utilities.sendNotification


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager =
            ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager


        if(intent.action == "Task_Notification") {
            val intentMessage = intent.getStringExtra("Message") ?: "Fail"
            val intentId = intent.getIntExtra("Notification_id", 1)
                notificationManager.sendNotification(
                    intentMessage,
                    context,
                    intentId
                )
        }
        if (intent.action == "Reboot_Notification") {
            val intentMessage = intent.getStringExtra("Message") ?: "Fail"
            val intentId = intent.getIntExtra("Notification_id", 0)

            notificationManager.sendNotification(
                intentMessage,
                context,
                intentId
            )
        }
    }
}
