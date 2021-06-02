package com.example.tasksapp.receiver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.example.tasksapp.dailytodo.schedule.TodoSchedule
import com.example.tasksapp.database.TaskDatabase
import kotlinx.coroutines.*

class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @ExperimentalCoroutinesApi
    override fun onReceive(context: Context, intent: Intent) {
        
        val notifyIntent = Intent(context, AlarmReceiver::class.java)
        val scheduleIntent = Intent(context, ScheduleReceiver::class.java)
        var notifyPendingIntent: PendingIntent
        var alarmManager: AlarmManager
        val scope = CoroutineScope(Dispatchers.Main)


            scope.launch {
                val database = TaskDatabase.getInstance(context)
                val taskList = database.taskDataDao.getAllTaskWithDate()
                val scheduleTodoList = database.todoScheduleDao.getScheduleList()
                withContext(Dispatchers.Default) {
                    taskList.map {
                        val taskId = it.taskId.toInt()
                        val date = it.date
                        notifyIntent.putExtra("Message", it.taskTag)
                        notifyIntent.putExtra("Notification_id", taskId)
                        notifyIntent.action = "Reboot_Notification"
                        notifyPendingIntent = PendingIntent.getBroadcast(
                            context,
                            taskId,
                            notifyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            AlarmManagerCompat.setExactAndAllowWhileIdle(
                                alarmManager,
                                AlarmManager.RTC_WAKEUP,
                                date,
                                notifyPendingIntent
                            )
                    }

                    scheduleTodoList.map {
                        val reminderDate = it.reminderDate
                        val id = it.scheduleDate.toInt()
                        scheduleIntent.putExtra(
                            TodoSchedule.SCHEDULE_MESSAGE,
                            "${it.todoScheduleId}\nYou have scheduled tasks for today")
                        scheduleIntent.putExtra(TodoSchedule.SCHEDULE_NOTIFICATION_ID, id)
                        scheduleIntent.putExtra(TodoSchedule.SCHEDULE_ID, it.todoScheduleId)
                        scheduleIntent.action = TodoSchedule.SCHEDULE_ACTION
                        notifyPendingIntent = PendingIntent.getBroadcast(
                            context,
                            id,
                            scheduleIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        AlarmManagerCompat.setExactAndAllowWhileIdle(
                            alarmManager,
                            AlarmManager.RTC_WAKEUP,
                            reminderDate,
                            notifyPendingIntent
                        )
                        
                    }
                }
                cancel()
            }

    }
}
