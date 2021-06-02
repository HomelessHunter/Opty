package com.example.tasksapp.adapter

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.example.tasksapp.R
import com.example.tasksapp.utilities.setTint
import com.google.android.material.appbar.AppBarLayout

@BindingAdapter("setTagColor")
fun ImageView.setTagColor(color: Int) {
    imageTintList = when(color) {
        1 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark1))
        2 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark2))
        3 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark3))
        4 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark4))
        5 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark5))
        6 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark6))
        7 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark7))
        8 -> setTint(ContextCompat.getColor(context.applicationContext, R.color.bookmark8))
        else -> setTint(ContextCompat.getColor(context.applicationContext, R.color.secondaryTextColor))
    }
}

@BindingAdapter("setDescriptionBackground")
fun AppBarLayout.setDescBackground(color: Int) {
    background = when(color) {
        1 -> ResourcesCompat.getDrawable(resources, R.drawable.gradient1, null)
        2 -> ResourcesCompat.getDrawable(resources,R.drawable.gradient2, null)
        3 -> ResourcesCompat.getDrawable(resources,R.drawable.gradient3, null)
        4 -> ResourcesCompat.getDrawable(resources, R.drawable.gradient4, null)
        5 -> ResourcesCompat.getDrawable(resources, R.drawable.gradient5, null)
        6 -> ResourcesCompat.getDrawable(resources, R.drawable.gradient6, null)
        7 -> ResourcesCompat.getDrawable(resources, R.drawable.gradient7, null)
        8 -> ResourcesCompat.getDrawable(resources, R.drawable.gradient8, null)
        else -> ResourcesCompat.getDrawable(resources, R.drawable.default_gradient, null)
    }
}


