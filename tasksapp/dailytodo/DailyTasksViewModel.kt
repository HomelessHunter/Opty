package com.example.tasksapp.dailytodo

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.datastore.preferences.emptyPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.tasksapp.dailytodo.schedule.Routine
import com.example.tasksapp.dailytodo.schedule.TodoSchedule
import com.example.tasksapp.database.*
import com.example.tasksapp.receiver.ScheduleReceiver
import com.example.tasksapp.settings.TIME_FORMAT
import com.example.tasksapp.utilities.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TRIGGER_TIME = "TRIGGER_AT"
private const val START_TIME = "START_TIME"
private const val ROUTINE_TIME = "ROUTINE_TIME"


@ExperimentalCoroutinesApi
class DailyTasksViewModel(
    val app: Application,
    val database: TodoTaskDataDao,
    private val todoScheduleDao: TodoScheduleDao,
    val averageTodoTimeDao: AverageTodoTimeDao) : AndroidViewModel(app) {

    private lateinit var timer: CountDownTimer
    private val prefs = app.getSharedPreferences("com.example.taskapp", Context.MODE_PRIVATE)
    private var listItemTimePermit: Boolean = false
    val formatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val _todayScheduleID = formatter.format(Calendar.getInstance(Locale.getDefault()).timeInMillis)
        val todayScheduleID: String = _todayScheduleID

    val dataStore = Prefs.getInstance(app.applicationContext)

    val dSFormat = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[TIME_FORMAT] ?: false }

    private val _timeFormat = MutableLiveData<Boolean>()
        val timeFormat: LiveData<Boolean> = _timeFormat

    val routineList = Pager(
        PagingConfig(
            pageSize = 50
        )
    ) {
        database.getAllTodoTasksByParentID(Routine.ROUTINE_ID)
    }.flow.cachedIn(viewModelScope)

    val scheduleList = Pager(
        PagingConfig(
            pageSize = 50
        )
    ) {
        database.getAllTodoTasksByParentID(todayScheduleID)
    }.flow.cachedIn(viewModelScope)

    val routineSize = database.getRoutineTasksForCounter(Routine.ROUTINE_ID).flowOn(Dispatchers.IO)

    private val _elapsedTime = MutableStateFlow(0L)
        val elapsedTime: StateFlow<Long> = _elapsedTime


    private val _isTodoCompleted = MutableLiveData<Long?>()
        val isTodoCompleted: LiveData<Long?> = _isTodoCompleted

    init {
        createTimer()
    }

    fun setFormat(format: Boolean) {
        _timeFormat.value = format
    }

    fun onInsertTodoTask(scheduleID: String, todoText: String) {
        viewModelScope.launch {
            val maxPosition = database.getMaxPosition()?.inc() ?: 1
            val todoTask = DailyTodoTask(parentId = scheduleID, position = maxPosition, todoTaskText = todoText)
            database.insertTodoTask(todoTask)
        }
    }

    fun startTimer() {
        viewModelScope.launch {
            val c = Calendar.getInstance(Locale.getDefault())
            val cHour = Calendar.getInstance(Locale.getDefault())
                cHour.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 24, 0, 0)
            val triggerTime = SystemClock.elapsedRealtime() + (cHour.timeInMillis - c.timeInMillis)

                saveTime(triggerTime)
                saveStartTime(c.timeInMillis)
            createTimer()
        }
    }

    private suspend fun checkRoutineDate() {
        val routineList = database.getRoutineList(Routine.ROUTINE_ID)
        val calendar = Calendar.getInstance(Locale.getDefault())
        if (calendar.timeInMillis >= loadRoutineLastDate()) {
            withContext(Dispatchers.Default) {
                routineList.map {
                    it.isCompleted = 0
                    it.todoFinishTime = 0
                    database.updateTodo(it)
                }
            }
        } else return
    }

    private fun createTimer() {
        viewModelScope.launch {
            val triggerTimer = loadTime()

            if (triggerTimer <= 0) {
                listItemTimePermit = false
                checkRoutineDate()
                return@launch
            }

            listItemTimePermit = true
            timer = object : CountDownTimer(triggerTimer, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTimer - SystemClock.elapsedRealtime()
                    if (_elapsedTime.value <= 0 ) {
                        resetTimer()
                    }
                }
                override fun onFinish() {
                    resetTimer()
                }
            }
            timer.start()
        }
    }

    fun resetTimer() {
        viewModelScope.launch {
            timer.cancel()

            listItemTimePermit = false
            _elapsedTime.value = 0

            saveTime(0)
            saveStartTime(0)
        }
    }


    fun completeTodo(value: Long) {
        _isTodoCompleted.value = value
    }
    fun resetCompleteTodo() {
        _isTodoCompleted.value = null
    }

    fun onUpdateTodo(idTodo: Long) {
        viewModelScope.launch {
            val timeNow = Calendar.getInstance(Locale.getDefault())
            val todo = database.getTodoByID(idTodo)
            val maxPosition = database.getMaxPosition()?.inc() ?: 1
            val minPosition = database.getMinPosition()?.dec() ?: 1
            if (todo.isCompleted == 0) {
                todo.isCompleted = 1
                todo.position = minPosition
                when(listItemTimePermit) {
                    false -> todo.todoFinishTime = 0L
                    else -> {
                        todo.todoFinishTime = timeNow.timeInMillis - loadStartTime()
                        saveStartTime(timeNow.timeInMillis)
                    }
                }

                timeNow.set(Calendar.HOUR_OF_DAY, 24)
                timeNow.set(Calendar.MINUTE, 0)
                saveRoutineLastDate(timeNow.timeInMillis)
            } else {
                todo.isCompleted = 0
                todo.position = maxPosition
                todo.todoFinishTime = 0L
                saveStartTime(timeNow.timeInMillis)
            }
            database.updateTodo(todo)
        }
    }

    fun deleteTodo(todoTask: DailyTodoTask) {
        viewModelScope.launch {
            database.deleteTodoTask(todoTask)
        }
    }

    private suspend fun saveRoutineLastDate(lastDate: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(ROUTINE_TIME, lastDate).apply()
    }

    private suspend fun loadRoutineLastDate(): Long = withContext(Dispatchers.IO) {
        prefs.getLong(ROUTINE_TIME, 0L)
    }

    private suspend fun saveStartTime(startTime: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(START_TIME, startTime).apply()
    }

    private suspend fun loadStartTime(): Long = withContext(Dispatchers.IO) {
        prefs.getLong(START_TIME, 0L)
    }

    private suspend fun saveTime(triggerTimer: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(TRIGGER_TIME, triggerTimer).apply()
    }

    private suspend fun loadTime(): Long = withContext(Dispatchers.IO) {
        prefs.getLong(TRIGGER_TIME, 0L)
    }

    val scheduleWithTodo = Pager(
        PagingConfig(
            pageSize = 50
        )
    ) {
        todoScheduleDao.getScheduleWithTodoFlowList()
    }.flow.cachedIn(viewModelScope)

    val scheduleWithTodoEmptyCheck = todoScheduleDao.getScheduleWithTodoFlow()

    private val notifyIntent = Intent(app, ScheduleReceiver::class.java)
    private lateinit var notifyPendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager

    private val _scheduleArchive = MutableLiveData<DailyTodoSchedule?>()
        val scheduleArchive: LiveData<DailyTodoSchedule?> = _scheduleArchive

    private val _info = MutableLiveData<String?>()
        val info: LiveData<String?> = _info

    private val _reminderVal = MutableLiveData<DailyTodoSchedule?>()
        val reminderVal: LiveData<DailyTodoSchedule?> = _reminderVal

    private val _resetReminderVal = MutableLiveData<DailyTodoSchedule?>()
        val resetReminderVal: LiveData<DailyTodoSchedule?> = _resetReminderVal

    private val _todoScheduleItem = MutableStateFlow(ScheduleWithTodo(DailyTodoSchedule(), emptyList()))
        val todoScheduleItem: StateFlow<ScheduleWithTodo> = _todoScheduleItem

    private val _todoScheduleAdapterPosition = MutableStateFlow(0)
        val todoScheduleAdapterPosition: StateFlow<Int> = _todoScheduleAdapterPosition

    fun setTodoScheduleItem(id: String) {
        viewModelScope.launch {
            _todoScheduleItem.value = todoScheduleDao.getInfoScheduleWithTodo(id)
        }
    }

    fun setTodoScheduleAdapterPosition(position: Int) {
        _todoScheduleAdapterPosition.value = position
    }

    fun setReminderVal(dailyTodoSchedule: DailyTodoSchedule) {
        _reminderVal.value = dailyTodoSchedule
    }

    fun resetReminderVal() {
        _reminderVal.value = null
    }

    fun setNotification(dailyTodoSchedule: DailyTodoSchedule, date: Long) {
        viewModelScope.launch {
            val scheduleDateNotification = dailyTodoSchedule.scheduleDate + date
            dailyTodoSchedule.reminderDate = scheduleDateNotification
            todoScheduleDao.updateSchedule(dailyTodoSchedule)
            val id = dailyTodoSchedule.scheduleDate.toInt()
            notifyIntent.putExtra(TodoSchedule.SCHEDULE_MESSAGE,
                dailyTodoSchedule.todoScheduleId)
            notifyIntent.putExtra(TodoSchedule.SCHEDULE_NOTIFICATION_ID, id)
            notifyIntent.putExtra(TodoSchedule.SCHEDULE_ID, dailyTodoSchedule.todoScheduleId)
            notifyIntent.action = TodoSchedule.SCHEDULE_ACTION
            notifyPendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                id,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                scheduleDateNotification,
                notifyPendingIntent
            )
        }
    }

    fun resetNotification(dailyTodoSchedule: DailyTodoSchedule, date: Long) {
        viewModelScope.launch {
            dailyTodoSchedule.reminderDate = date
            todoScheduleDao.updateSchedule(dailyTodoSchedule)
            val id = dailyTodoSchedule.scheduleDate.toInt()
            notifyIntent.putExtra(TodoSchedule.SCHEDULE_NOTIFICATION_ID, id)
            notifyPendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                id,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                date,
                notifyPendingIntent
            )
            alarmManager.cancel(notifyPendingIntent)
        }
    }

    fun setResetReminderVal(dailyTodoSchedule: DailyTodoSchedule) {
        _resetReminderVal.value = dailyTodoSchedule
    }

    fun resetResetReminderVal() {
        _resetReminderVal.value = null
    }

    fun setInfo(id: String) {
        _info.value = id
    }

    fun resetInfo() {
        _info.value = null
    }

    fun setScheduleArchive(todoSchedule: DailyTodoSchedule) {
        _scheduleArchive.value = todoSchedule
    }

    fun resetScheduleArchive() {
        _scheduleArchive.value = null
    }

    fun insertSchedule(id: String, date: Long) {
        viewModelScope.launch {
            val schedule = DailyTodoSchedule(todoScheduleId = id, scheduleDate = date)

            todoScheduleDao.insertTodoSchedule(schedule)
        }
    }

    fun updateSchedule(todoSchedule: DailyTodoSchedule, archiveVal: Int) {
        viewModelScope.launch {
            todoSchedule.archiveVal = archiveVal
            todoScheduleDao.updateSchedule(todoSchedule)
        }
    }

    fun updateDailyTodo(todoSchedule: DailyTodoSchedule) {
        viewModelScope.launch {
            val list = database.getTodoList(todoSchedule.todoScheduleId)
            withContext(Dispatchers.Default) {
                list.map {
                    it.archive = 1
                    database.updateTodo(it)
                }
            }
        }
    }

    val archive = Pager(
        PagingConfig(
            pageSize = 50
        )
    ) {
        todoScheduleDao.getArchivedScheduleWithTodoFlowList()
    }.flow.cachedIn(viewModelScope)

    val emptyArchiveCheck = todoScheduleDao.getArchiveScheduleWithTodoFlow()

    private val _deleteValue = MutableLiveData<DailyTodoSchedule?>()
        val deleteValue: LiveData<DailyTodoSchedule?> = _deleteValue

    fun setDeleteValue(scheduleWithTodo: DailyTodoSchedule) {
        _deleteValue.value = scheduleWithTodo
    }

    fun resetDeleteValue() {
        _deleteValue.value = null
    }

    fun deleteTodoTasks(id: String) {
        viewModelScope.launch {
            database.deleteTodoTasksById(id)
        }
    }

    fun deleteSchedule(todoSchedule: DailyTodoSchedule) {
        viewModelScope.launch {
            todoScheduleDao.deleteSchedule(todoSchedule)
        }
    }

    val averageTodoTimeList = averageTodoTimeDao.getAverageTimeList().flowOn(Dispatchers.IO)
    val todoList = Pager(
    PagingConfig(
    pageSize = 50
    )
    ) {
        database.getAllTodoTasksByParentID(Routine.ROUTINE_ID)
    }.flow.cachedIn(viewModelScope)

    val emptyListCheckRoutine = database.getEmptyListCheckByParentID(Routine.ROUTINE_ID)

}
