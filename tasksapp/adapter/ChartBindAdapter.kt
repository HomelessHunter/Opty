package com.example.tasksapp.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.tasksapp.database.TodaySessionResult
import com.example.tasksapp.utilities.setElapsedTime
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("setChartDate")
fun TextView.setChartDate(todaySessionResult: TodaySessionResult) {
    val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
    text = formatter.format(todaySessionResult.sessionDate)
}

@BindingAdapter("setChartTime")
fun TextView.setChartTime(todaySessionResult: TodaySessionResult) {
    text = setElapsedTime(todaySessionResult.sessionDuration)
}