package com.example.tasksapp.description.matrixFragment

import android.app.Application
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasksapp.database.Subtask
import com.example.tasksapp.database.SubtaskDataDao
import com.example.tasksapp.database.TodaySessionDao
import com.example.tasksapp.database.TodaySessionResult
import com.example.tasksapp.utilities.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@ExperimentalCoroutinesApi
class MatrixViewModel(
    subtaskDataDao: SubtaskDataDao,
    private val id: Long,
    private val todaySessionDao: TodaySessionDao,
    app: Application
) : AndroidViewModel(app) {

    val dataStore = Prefs.getInstance(app)
    private val MATRIX_SESSION = preferencesKey<Long>("MATRIX_SESSION$id")

    private val _subtaskListMatrix = subtaskDataDao.getAllMatrixSubtasks(id).flowOn(Dispatchers.IO)
        val subtaskListMatrix = _subtaskListMatrix

    private val _impUrgList = MutableStateFlow<List<Subtask>>(listOf())
        val impUrgList: StateFlow<List<Subtask>> = _impUrgList

    private val _impNotUrgList = MutableStateFlow<List<Subtask>>(listOf())
        val impNotUrgList: StateFlow<List<Subtask>> = _impNotUrgList

    private val _notImpUrgList = MutableStateFlow<List<Subtask>>(listOf())
        val notImpUrgList: StateFlow<List<Subtask>> = _notImpUrgList

    private val _notImpNotUrgList = MutableStateFlow<List<Subtask>>(listOf())
        val notImpNotUrgList: StateFlow<List<Subtask>> = _notImpNotUrgList

    private val _isMatrixSubtaskCompleted = MutableStateFlow(0L)
        val isMatrixSubtaskCompleted: StateFlow<Long> = _isMatrixSubtaskCompleted

    private val _sessionValue = MutableStateFlow(0L)
        val sessionValue: StateFlow<Long>
            get() = _sessionValue

    fun setSessionValue(value: Long) {
        _sessionValue.value = value
    }

    fun setMatrixSubtaskCompletion(id: Long) {
    _isMatrixSubtaskCompleted.value = id
}
    fun resetMatrixCompletionValue() {
        _isMatrixSubtaskCompleted.value = 0L
    }

    fun setMatrixLists(list: List<Subtask>) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _impUrgList.value = list.filter { it.matrixValue == 1 }
                _impNotUrgList.value = list.filter { it.matrixValue == 2 }
                _notImpUrgList.value = list.filter { it.matrixValue == 3 }
                _notImpNotUrgList.value = list.filter { it.matrixValue == 4 }
            }
        }
    }

    suspend fun saveAndClearTodaySession(sessionTime: Long) {
        val date = Calendar.getInstance(Locale.getDefault()).timeInMillis
        _sessionValue.value = 0L
        dataStore.edit {
            it[MATRIX_SESSION] = 0L
        }

        val newSession = TodaySessionResult(parentTaskId = id, sessionDuration = sessionTime, sessionDate = date)
        todaySessionDao.insertTodaySession(newSession)

    }
}