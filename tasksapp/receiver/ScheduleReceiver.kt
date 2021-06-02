package com.example.tasksapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.tasksapp.R
import com.example.tasksapp.dailytodo.schedule.TodoSchedule
import com.example.tasksapp.database.TaskDatabase
import kotlinx.coroutines.*

class ScheduleReceiver : BroadcastReceiver() {


    @ExperimentalCoroutinesApi
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager =
            ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

        val intentMessage = intent.getStringExtra(TodoSchedule.SCHEDULE_MESSAGE) ?: ""
        val intentId = intent.getIntExtra(TodoSchedule.SCHEDULE_NOTIFICATION_ID, 1)
        val scheduleId = intent.getStringExtra(TodoSchedule.SCHEDULE_ID)

        val contentPendingIntent = NavDeepLinkBuilder(context).apply {
            setGraph(R.navigation.navigation)
            setDestination(R.id.dailyTasks)
        }.createPendingIntent()

        CoroutineScope(Dispatchers.Main).launch {
            val database = TaskDatabase.getInstance(context).todoScheduleDao
            val schedule = database.getTodoScheduleById(scheduleId!!)
            schedule.reminderDate = 0L
            database.updateSchedule(schedule)
            cancel()
        }

        val notification = NotificationCompat.Builder(context,
            context.getString(R.string.schedule_notification_channel)).apply {
            setSmallIcon(R.drawable.ic_foreground_icon)
            setContentTitle(intentMessage)
            setContentText(context.getString(R.string.schedule_message_text))
            setContentIntent(contentPendingIntent)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()

        notificationManager.notify(intentId, notification)
        Log.e("Schedule_receiver", "Test")

    }
}