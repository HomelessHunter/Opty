package com.example.tasksapp.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.text.Editable
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R


object Prefs {
    @Volatile
    private var INSTANCE: DataStore<Preferences>? = null

    fun getInstance(context: Context): DataStore<Preferences> {
        synchronized(this) {

            var instance = INSTANCE

            if (instance == null) {
                instance = context.createDataStore(
                    name = "prefs"
                )
                INSTANCE = instance
            }
            return instance
        }
    }
}



fun isTextCorrect(text: Editable?): Boolean {
    return text != null && text.isNotEmpty()
}

fun setElapsedTime(time: Long): String {
    val seconds = time / 1000
    return DateUtils.formatElapsedTime(seconds)
}

fun NotificationManager.sendNotification(messageBody: String,
                                         applicationContext: Context,
                                         notificationId: Int) {

    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        notificationId,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.subtask_notification_channel)
    )
        .setSmallIcon(R.drawable.ic_foreground_icon)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(notificationId, builder.build())
}

fun setTint(color: Int): ColorStateList {
    return ColorStateList.valueOf(color)
}
