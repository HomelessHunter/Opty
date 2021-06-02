package com.example.tasksapp.dailytodo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tasksapp.database.AverageTodoTimeDao
import com.example.tasksapp.database.TodoScheduleDao
import com.example.tasksapp.database.TodoTaskDataDao
import kotlinx.coroutines.ExperimentalCoroutinesApi

class DailyTaskFactory(private val application: Application,
                       private val database: TodoTaskDataDao,
                       private val todoScheduleDao: TodoScheduleDao,
                       private val averageTodoTimeDao: AverageTodoTimeDao
                       ) : ViewModelProvider.Factory {
    @ExperimentalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyTasksViewModel::class.java)) {
            return DailyTasksViewModel(application, database, todoScheduleDao, averageTodoTimeDao) as T
        }
        throw IllegalArgumentException("Unknown class")
    }
}