package com.example.tasksapp.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tasksapp.database.MatrixResultDao
import com.example.tasksapp.database.TaskDataDao
import com.example.tasksapp.database.TodaySessionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.IllegalArgumentException

class ChartStatisticsFactory(private val idTask: Long,
                             private val matrixResultDao: MatrixResultDao,
                             private val todaySessionDao: TodaySessionDao,
                             private val taskDataDao: TaskDataDao) : ViewModelProvider.Factory {
    @ExperimentalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChartStatisticsViewModel::class.java)) {
            return ChartStatisticsViewModel(idTask, matrixResultDao, todaySessionDao, taskDataDao) as T
        }
        throw IllegalArgumentException("Unknown class")
    }


}