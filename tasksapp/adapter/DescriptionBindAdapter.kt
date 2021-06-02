package com.example.tasksapp.adapter

import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.tasksapp.R
import com.example.tasksapp.database.Subtask
import com.example.tasksapp.database.Task
import com.example.tasksapp.utilities.setTint
import com.google.android.material.switchmaterial.SwitchMaterial

@BindingAdapter("isCheckedValue")
fun CheckBox.checked(subtask: Subtask) {
    isChecked = subtask.isCompleted == 1
}

@BindingAdapter("isSwitched")
fun SwitchMaterial.switched(task: Task) {
    isChecked = task.importance == 1
}

@BindingAdapter("setPriorityTint")
fun ImageView.setPriorityTint(matrixValue: Int) {
    imageTintList = when(matrixValue) {
        1 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark1))
        2 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark3))
        3 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark2))
        4 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark4))
        else -> setTint(ContextCompat.getColor(context.applicationContext, R.color.secondaryTextColor))
    }
}
