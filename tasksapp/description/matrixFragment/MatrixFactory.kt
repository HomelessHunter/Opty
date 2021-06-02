package com.example.tasksapp.description.matrixFragment

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tasksapp.database.SubtaskDataDao
import com.example.tasksapp.database.TodaySessionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.IllegalArgumentException

class MatrixFactory(
    private val subtaskDataDao: SubtaskDataDao,
    private val id: Long,
    private val todaySessionDao: TodaySessionDao,
    private val app: Application
) : ViewModelProvider.Factory {
    @ExperimentalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatrixViewModel::class.java)) {
            return MatrixViewModel(subtaskDataDao, id, todaySessionDao, app) as T
        }
        throw IllegalArgumentException("Unknown class")
    }

}