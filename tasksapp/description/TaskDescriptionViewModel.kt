package com.example.tasksapp.description

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.tasksapp.database.Subtask
import com.example.tasksapp.database.SubtaskDataDao
import com.example.tasksapp.database.Task
import com.example.tasksapp.database.TaskDataDao
import com.example.tasksapp.receiver.AlarmReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


@ExperimentalCoroutinesApi
class TaskDescriptionViewModel(private val id: Long,
                               private val database: TaskDataDao,
                               private val subtaskDataDao: SubtaskDataDao, private val app: Application) : AndroidViewModel(app){

    private val _mainTask = MutableStateFlow(Task())
        val mainTask: StateFlow<Task> = _mainTask

    private val _colorChanger = database.getFlowTaskWithKey(id).flowOn(Dispatchers.IO)
        val colorChanger = _colorChanger

    private val _task = MutableLiveData<Task>()
        val task: LiveData<Task> = _task

    private val _subtaskText = MutableLiveData<String?>()
        val subtaskText: LiveData<String?>
            get() = _subtaskText

    private val _mainDate = MutableStateFlow(0L)
        val mainDate: StateFlow<Long> = _mainDate

    val subtaskList = Pager(
        PagingConfig(
            pageSize = 50
            )
    ) {
        subtaskDataDao.getAllSubtasks(id)
    }.flow

    val additionalList = subtaskDataDao.getAllMatrixSubtasks(id)

    private val _matrixSetter = MutableLiveData<Long?>()
        val matrixSetter: LiveData<Long?> = _matrixSetter

    private val notifyIntent = Intent(app, AlarmReceiver::class.java)
    private lateinit var notifyPendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager

    init {
        getTask()
        getDate()
    }

    fun startTimer(calendar: Calendar, date: Long) {
        viewModelScope.launch {
            mainTask.value.let {
                val mainTask = it
                it.date = date
                database.update(mainTask)
                val taskId = it.taskId.toInt()
                notifyIntent.putExtra("Message", it.taskTag)
                notifyIntent.putExtra("Notification_id", taskId)
                notifyIntent.action = "Task_Notification"
                notifyPendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    taskId,
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    notifyPendingIntent
                )
            }
        }
    }

    fun cancelNotification(date: Long, calendar: Calendar) {
        viewModelScope.launch {
            mainTask.value.let {
                val mainTask = it
                it.date = date
                database.update(mainTask)
                val taskId = it.taskId.toInt()
                notifyIntent.putExtra("Notification_id", taskId)
                notifyPendingIntent = PendingIntent.getBroadcast(
                    getApplication(),
                    taskId,
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    notifyPendingIntent
                )
                alarmManager.cancel(notifyPendingIntent)
            }
        }
    }

    fun onGetSubtaskText(text: String) {
        _subtaskText.value = text
    }

    fun setMatrixValue(idSubtask: Long, matrixValue: Int) {
         viewModelScope.launch {
             val subtask = subtaskDataDao.getSubtaskById(idSubtask)
                subtask.matrixValue = matrixValue
             subtaskDataDao.updateSubtask(subtask)
         }
    }

    fun resetReminderDate() {
        viewModelScope.launch {
            val task = database.getTaskWithKey(id)
            task.date = 0L
            database.update(task)
        }
    }

    fun onInsertSubtask() {
        viewModelScope.launch {
            val max = subtaskDataDao.getMaxPosition()?.inc() ?: 1
            val subtask = Subtask(parentId = id, position = max,
                subtaskText = subtaskText.value?.toString() ?: return@launch)
            subtaskDataDao.putSubtask(subtask)
            _subtaskText.value = null
        }
    }

    fun onDeleteDescTask(subtask: Subtask) {
        viewModelScope.launch {
            subtaskDataDao.deleteSubtask(subtask)
        }
    }

    private fun getTask() {
        viewModelScope.launch {
            _mainTask.value = database.getTaskWithKey(id)
        }
    }

   suspend fun getSubtask(id: Long): Subtask = withContext(Dispatchers.IO) {
       subtaskDataDao.getSubtaskById(id)
   }

    fun getDate() {
        viewModelScope.launch {
            mainTask.collectLatest { _mainDate.value = it.date }
        }
    }

    fun setMatrixSetter(value: Long) {
        _matrixSetter.value = value
    }

    fun resetMatrixSetter() {
        _matrixSetter.value = null
    }
}
