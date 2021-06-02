package com.example.tasksapp.maintask.bookmarksFragment.tagInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasksapp.database.TaskDataDao
import com.example.tasksapp.database.TodaySessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow


data class TagPieChartObj(
    val finishTimer: Float,
    val name: String
)
@ExperimentalCoroutinesApi
class TagInfoViewModel(private val taskDataDao: TaskDataDao, tag: String, private val todaySessionDao: TodaySessionDao) : ViewModel() {

    init {
        setPieChartValue()
    }

    private val tasksWithSubtasks = taskDataDao.getAllTasksWithSubtasks(tag).flowOn(Dispatchers.IO)
    private val _tagPieChartList = MutableStateFlow<List<TagPieChartObj>>(listOf())
        val tagPieChartList: StateFlow<List<TagPieChartObj>> = _tagPieChartList

    private val _longestTask = MutableStateFlow(0L)
        val longestProject: StateFlow<Long> = _longestTask

    private val _shortestTask = MutableStateFlow(0L)
        val shortestProject: StateFlow<Long> = _shortestTask

    private val _totalTime = MutableStateFlow(0L)
        val totalTime: StateFlow<Long> = _totalTime

    private val _tasksPerHour = MutableStateFlow(0f)
        val tasksPerHour: StateFlow<Float> = _tasksPerHour

    private val _timeForOneTask = MutableStateFlow(0L)
        val timeForOneTask: StateFlow<Long> = _timeForOneTask


    fun setTotalTime(taskTag: String) {
        viewModelScope.launch {
            val task = taskDataDao.getTaskByTaskTag(taskTag)
            val sessions = todaySessionDao.getSessionListASC(task.taskId)
            withContext(Dispatchers.Default) {
                _totalTime.value = sessions?.sumOf { it.sessionDuration} ?: 0
            }
        }
    }

    fun setTasksPerHour(tag: String) {
        viewModelScope.launch {
            val list = taskDataDao.getTaskWithSubtaskskByTag(tag)
                withContext(Dispatchers.Default) {
                    val size = list.map { taskWithSubtasks ->
                        taskWithSubtasks.subtasks.map { it }
                            .filter { it.subtaskFinishTime > 0 }.size
                    }.reduce { _, i -> i }

                    val timeList = list.map { taskWithSubtasks ->
                        taskWithSubtasks.subtasks.map {
                            it.subtaskFinishTime}.filter { it > 0 }}.reduce { _, list -> list }
                    if (timeList.isNotEmpty()) {
                       val s = timeList.reduce { acc, l -> acc * l }.toDouble()

                        val timeForOneTask = s.pow(1/size.toDouble())
                        _timeForOneTask.value = timeForOneTask.toLong()

                        val tasksPerHour = 3_600_000 / timeForOneTask
                        _tasksPerHour.value = tasksPerHour.toFloat()
                    } else {
                        _timeForOneTask.value = 0L
                        _tasksPerHour.value = 0.0f
                    }
                }
        }
    }


    fun setMinMaxInfo(tag: String) {
        viewModelScope.launch {
            val list = taskDataDao.getTaskWithSubtaskskByTag(tag)
            withContext(Dispatchers.Default) {

                val max = list.map { taskWithSubtasks ->
                    taskWithSubtasks
                        }.map { taskWithSubtasks -> taskWithSubtasks.subtasks.map { it.subtaskFinishTime }.filter { it > 0 } }.reduce { _, list -> list }
                    .maxOrNull()

                val min = list.map { taskWithSubtasks ->
                                taskWithSubtasks
                            }.map { taskWithSubtasks -> taskWithSubtasks.subtasks.map { it.subtaskFinishTime }. filter { it > 0 } }.reduce { _, list -> list }
                    .minOrNull()

                _longestTask.value = max ?: 0L
                _shortestTask.value = min ?: 0L
            }
        }
    }

    private fun setPieChartValue() {
        viewModelScope.launch {
            tasksWithSubtasks.collectLatest { tasksWithSubtasksList ->
                withContext(Dispatchers.Default) {

                    val dSum = tasksWithSubtasksList.map { taskWithSubtasks ->
                        taskWithSubtasks.task
                    }.map {
                        todaySessionDao.getSessionListASC(it.taskId)
                    }.map { list ->
                        list!!.sumOf { it.sessionDuration }
                    }.sum().div(100.0f)

                    val dList = tasksWithSubtasksList.map { taskWithSubtasks ->
                        val id = taskWithSubtasks.task.taskId
                        val s = todaySessionDao.getSessionListASC(id)?.map {
                            it.sessionDuration }?.sum()
                        TagPieChartObj(s!!.div(dSum), taskWithSubtasks.task.taskTag)
                    }
                    _tagPieChartList.value = dList
                }
            }
        }
    }
}