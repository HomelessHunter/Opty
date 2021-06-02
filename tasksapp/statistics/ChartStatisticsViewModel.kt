package com.example.tasksapp.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasksapp.database.MatrixResultDao
import com.example.tasksapp.database.TaskDataDao
import com.example.tasksapp.database.TaskMatrixResult
import com.example.tasksapp.database.TodaySessionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PieChartObj(
    val idMatrix: String,
    val finishTime: Float,
    val name: String
)

data class LineChartObj(
    val duration: Float,
    val sessionIndex: Int
)

@ExperimentalCoroutinesApi
class ChartStatisticsViewModel(private val idTask: Long, matrixResultDao: MatrixResultDao, private val todaySessionDao: TodaySessionDao, taskDataDao: TaskDataDao) : ViewModel() {

    init {
        getFinishedTime()
        getBarChartResults()
    }

    private val _mainTask = taskDataDao.getFlowTaskWithKey(idTask).flowOn(Dispatchers.IO)
        val mainTask = _mainTask

    private val _matrixResultList: Flow<List<TaskMatrixResult>> = matrixResultDao.getAllResultsById(idTask).flowOn(Dispatchers.IO)
        val matrixResultList = _matrixResultList

    private val _todaySessionList = todaySessionDao.getSessionFlowList(idTask).flowOn(Dispatchers.IO)
        val todaySessionList = _todaySessionList

    private val _pieChartResultList = MutableStateFlow<List<PieChartObj>?>(listOf())
        val pieChartResultList: StateFlow<List<PieChartObj>?> = _pieChartResultList

    private val _lineChartResultList = MutableStateFlow<List<LineChartObj>?>(mutableListOf())
        val lineChartResultList: StateFlow<List<LineChartObj>?> = _lineChartResultList

    private val _lineChartDateResultList = MutableStateFlow<List<Long>>(mutableListOf())
        val lineChartDateResultList: StateFlow<List<Long>> = _lineChartDateResultList

    private val _xZoom = MutableStateFlow(0f)
        val xZoom = _xZoom

    private val _yZoom = MutableStateFlow(0f)
        val yZoom = _yZoom

    private fun getFinishedTime() {
        viewModelScope.launch {
            matrixResultList.collectLatest {
                withContext(Dispatchers.Default) {
                    val matrixTimeSum = it.map { it.finishMatrixTime }.sum().div(100.0f)
                    val chartObjList = it.map { PieChartObj(idMatrix = it.taskMatrixResultId,
                        finishTime = ( it.finishMatrixTime / matrixTimeSum),
                        name = it.matrixName) }
                    _pieChartResultList.value = chartObjList
                }
            }
        }
    }

    private fun getBarChartResults() {
        viewModelScope.launch {
            val sevenElementList = todaySessionDao.getSessionListASC(idTask)?.toMutableList()
                withContext(Dispatchers.Default) {
                    val barChartObjList = sevenElementList
                        ?.mapIndexed { index, todaySession ->
                        LineChartObj(sessionIndex = index,
                            duration = todaySession.sessionDuration.toFloat()) }
                        _lineChartResultList.value = barChartObjList
                        _lineChartDateResultList.value = sevenElementList!!.map { it.sessionDate }
                }
        }
    }
}