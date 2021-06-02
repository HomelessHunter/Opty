package com.example.tasksapp.maintask

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tasksapp.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainTaskFactory(private val database: TaskDataDao,
                      private val tagsDao: TagsDao,
                      private val subtaskDataDao: SubtaskDataDao,
                      private val todaySessionDao: TodaySessionDao,
                      private val app: Application) : ViewModelProvider.Factory {
    @ExperimentalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainTaskViewModel::class.java)) {
            return MainTaskViewModel(database, tagsDao, subtaskDataDao, todaySessionDao, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}