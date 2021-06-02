package com.example.tasksapp.adapter

import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.example.tasksapp.database.Task


@BindingAdapter("setImportanceVisibility")
fun ImageView.setImportance(task: Task) {
    visibility = if (task.importance == 1) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter(value = ["setOnLong", "setOnLongId"])
fun CardView.setOnLong(listener: Any, id: Long) {
    setOnLongClickListener {
        when(listener) {
            is ShowOptionsDialog -> listener.onShowDialog(id)
            is RestoreClickListener -> listener.onRestored(id)
            else -> throw ClassCastException("Unknown class")
        }
        true
    }
}



