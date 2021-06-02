package com.example.tasksapp.maintask.bookmarksFragment.tagInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tasksapp.database.TaskDataDao
import com.example.tasksapp.database.TodaySessionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TagInfoFactory(private val taskDataDao: TaskDataDao, private val tag: String, private val todaySessionDao: TodaySessionDao) : ViewModelProvider.Factory {
    @ExperimentalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagInfoViewModel::class.java)) {
            return TagInfoViewModel(taskDataDao, tag, todaySessionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}