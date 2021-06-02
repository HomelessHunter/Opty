package com.example.tasksapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.tasksapp.R
import com.example.tasksapp.receiver.StopServiceReceiver
import java.util.*

class ChronoService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val startTime = SystemClock.elapsedRealtime()
        val idTaskMatrix = intent.getStringExtra("Matrix_ID")
        val idTask = intent.getLongExtra("ID_Task", 0L)
        val input = intent.getStringExtra("TitleServiceNotification")
        val id = intent.getIntExtra("ID", 1)
        val prefsTimeStars = intent.getStringExtra("Prefs_Time_Start")
        val matrixName = intent.getStringExtra("Matrix_Name")
        val taskName = intent.getStringExtra("Task_Name")
        createChannel(getString(R.string.chrono_notification_channel),
            getString(R.string.chrono_notification_channel_name)
        )
        val bundle = bundleOf(Pair("id", idTask), Pair("task_name", taskName))
        val pendingIntent = NavDeepLinkBuilder(applicationContext).apply {
            setGraph(R.navigation.navigation)
            setArguments(bundle)
            setDestination(R.id.matrix)
        }.createPendingIntent()

        val stopIntent = Intent(this, StopServiceReceiver::class.java).apply {
            putExtra("startTime", startTime)
            putExtra("idTaskMatrix", idTaskMatrix)
            putExtra("idTask", idTask)
            putExtra("Prefs_Time", prefsTimeStars)
            putExtra("Matrix_Tag", matrixName)
            setFlags(Intent.FLAG_RECEIVER_FOREGROUND and Intent.FLAG_RECEIVER_REPLACE_PENDING)
        }
        val pendingStopIntent = PendingIntent.getBroadcast(this, id, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationLayout = RemoteViews(packageName, R.layout.chrono_layout)
        notificationLayout.setChronometer(R.id.chronometer_matrix, SystemClock.elapsedRealtime(), null, true)
        notificationLayout.setTextViewText(R.id.chrono_title, taskName)
        notificationLayout.setTextViewText(R.id.chrono_importance, input)

        val notification = NotificationCompat.Builder(this, getString(R.string.chrono_notification_channel)).apply {
            setSmallIcon(R.drawable.ic_foreground_icon)
            setCustomContentView(notificationLayout)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            addAction(R.drawable.ic_stop_black_24dp, getString(R.string.fab_stop), pendingStopIntent)
            setContentIntent(pendingIntent)
            priority = NotificationCompat.PRIORITY_LOW
        }.build()

        startForeground(id, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.chonometer)
            }
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}