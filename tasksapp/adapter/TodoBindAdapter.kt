package com.example.tasksapp.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.example.tasksapp.R
import com.example.tasksapp.database.DailyTodoSchedule
import com.example.tasksapp.database.DailyTodoTask
import com.example.tasksapp.database.ScheduleWithTodo
import com.example.tasksapp.utilities.setElapsedTime
import com.example.tasksapp.utilities.setTint
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("isCheckedValueTodo")
fun CheckBox.checkedTodo(todoTask: DailyTodoTask) {
    isChecked = todoTask.isCompleted == 1
}

@BindingAdapter("setTodoListTime")
fun TextView.setTodoListTime(todoTask: DailyTodoTask) {
    text = if(todoTask.todoFinishTime > 0) {
        setElapsedTime(todoTask.todoFinishTime)
    } else {
        "00:00"
    }
}

@BindingAdapter("setTodoCounter")
fun TextView.setTodoCounter(scheduleWithTodo: ScheduleWithTodo) {
    text = resources.getString(R.string.todoCounterText, scheduleWithTodo.todoList.size.toString())
}

@BindingAdapter(value = ["setScheduleReminderText", "setScheduleReminderFormat"])
fun TextView.setScheduleReminderText(date: Long, format: Boolean) {
    Log.e("Schedule_format_binding", "$format")
    text = if (format) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("H:mm", Locale.getDefault()).format(date)
    }
}

@BindingAdapter("setScheduleReminderVisibility")
fun TextView.setScheduleReminderVisibility(todoSchedule: DailyTodoSchedule) {
    visibility = when(todoSchedule.reminderDate) {
        0L -> View.GONE
        else -> View.VISIBLE
    }
}

@BindingAdapter("setScheduleResetButtonVisibility")
fun ImageButton.setScheduleResetButtonVisibility(todoSchedule: DailyTodoSchedule) {
    visibility = when(todoSchedule.reminderDate) {
        0L -> View.GONE
        else -> View.VISIBLE
    }
}

@BindingAdapter("setScheduleNotificationButtonVisibility")
fun ImageButton.setScheduleNotificationButtonVisibility(todoSchedule: DailyTodoSchedule) {
    visibility = when(todoSchedule.reminderDate) {
        0L -> View.VISIBLE
        else -> View.GONE
    }
}

@SuppressLint("UseCompatTextViewDrawableApis")
@BindingAdapter("setDrawableTint")
fun TextView.setDrawableTint(archiveVal: Int) {
    compoundDrawableTintList = when(archiveVal) {
        0 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.secondaryColor))
        1 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.blind_green))
        else -> throw IllegalArgumentException("Unknown archiveVal")
    }
}
