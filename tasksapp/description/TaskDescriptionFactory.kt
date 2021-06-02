package com.example.tasksapp.description

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tasksapp.database.SubtaskDataDao
import com.example.tasksapp.database.TaskDataDao
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TaskDescriptionFactory(private val id: Long,
                             private val database: TaskDataDao,
                             private val subtaskDataDao: SubtaskDataDao,
                             private val app: Application) : ViewModelProvider.Factory {

    @ExperimentalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDescriptionViewModel::class.java)) {
            return TaskDescriptionViewModel(id, database, subtaskDataDao, app) as T
        }
        throw IllegalArgumentException("Unknown class")
    }
}