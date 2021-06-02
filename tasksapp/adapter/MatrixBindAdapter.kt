package com.example.tasksapp.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.tasksapp.database.Subtask
import com.example.tasksapp.utilities.setElapsedTime

@BindingAdapter("setMatrixListTime")
fun TextView.setMatrixListTime(subtask: Subtask) {
    text = setElapsedTime(subtask.subtaskFinishTime)
}