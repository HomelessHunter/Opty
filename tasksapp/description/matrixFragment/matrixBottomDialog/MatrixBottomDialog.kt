package com.example.tasksapp.description.matrixFragment.matrixBottomDialog

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.core.content.ContextCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.MatrixAdapter
import com.example.tasksapp.adapter.MatrixItemCompletionListener
import com.example.tasksapp.adapter.setPriorityTint
import com.example.tasksapp.database.MatrixResultDao
import com.example.tasksapp.database.SubtaskDataDao
import com.example.tasksapp.database.TaskMatrixResult
import com.example.tasksapp.databinding.MatrixListBottomDialogBinding
import com.example.tasksapp.description.matrixFragment.MatrixViewModel
import com.example.tasksapp.service.ChronoService
import com.example.tasksapp.utilities.Prefs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


@ExperimentalCoroutinesApi
class MatrixBottomDialog(
    private val idTask: Long,
    private val taskName: String,
    private val idMatrix: Int,
    private val viewModel: MatrixViewModel,
    private val app: Application,
    private val matrixResultDao: MatrixResultDao,
    private val subtaskDataDao: SubtaskDataDao
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "MATRIX_DIALOG"
        const val TIME_BASE = "TIME_BASE"
        const val CHRONO_PERMIT = "CHRONO_PERMIT"
    }

    private val MATRIX_SESSION = preferencesKey<Long>("MATRIX_SESSION$idTask")

    private lateinit var binding: MatrixListBottomDialogBinding
    private lateinit var chronometer: Chronometer
    private val prefs = app.getSharedPreferences("com.example.taskapp", Context.MODE_PRIVATE)
    private val serviceIntent = Intent(app, ChronoService::class.java)

    private var listItemTimePermit: Boolean = false
    private var sessionValue: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MatrixListBottomDialogBinding.inflate(inflater, container, false)

        val dataStore = Prefs.getInstance(requireContext())

        val session = dataStore.data.catch { e ->
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }.map { it[MATRIX_SESSION] ?: 0L }

        lifecycleScope.launch {
            session.collectLatest {
                sessionValue = it
            }
        }

        chronometer = binding.chronometerMatrix

        lifecycleScope.launch {
            if (loadStartChronoPermit()) startChrono()
        }

        val recyclerView = binding.matrixList
        val adapter = MatrixAdapter(MatrixItemCompletionListener { viewModel.setMatrixSubtaskCompletion(it) })
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.isSmoothScrollbarEnabled = true
        recyclerView.adapter = adapter
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        when(idMatrix) {
            1 -> {
                binding.matrixBottomTitle.text = getString(R.string.d_important_urgent)
                binding.playButton.setPriorityTint(1)
                binding.stopButton.setPriorityTint(1)
            }
            2 -> {
                binding.matrixBottomTitle.text = getString(R.string.d_important_not_urgent)
                binding.playButton.setPriorityTint(2)
                binding.stopButton.setPriorityTint(2)
            }
            3 -> {
                binding.matrixBottomTitle.text = getString(R.string.d_not_important_urgent)
                binding.playButton.setPriorityTint(3)
                binding.stopButton.setPriorityTint(3)
            }
            4 -> {
                binding.matrixBottomTitle.text = getString(R.string.d_not_important_not_urgent)
                binding.playButton.setPriorityTint(4)
                binding.stopButton.setPriorityTint(4)
            }
        }

        lifecycleScope.launch {
            when(idMatrix) {
                1 -> viewModel.impUrgList.collectLatest {
                    adapter.submitList(it)
                }
                2 -> viewModel.impNotUrgList.collectLatest {
                    adapter.submitList(it)
                }
                3 -> viewModel.notImpUrgList.collectLatest {
                    adapter.submitList(it)
                }
                4 -> viewModel.notImpNotUrgList.collectLatest {
                    adapter.submitList(it)
                }
            }
        }

        lifecycleScope.launch {
            when(idMatrix) {
                1 -> {
                    checkVisibility(idTask, 1)
                }
                2 -> {
                    checkVisibility(idTask, 2)
                }
                3 -> {
                    checkVisibility(idTask, 3)
                }
                4 -> {
                    checkVisibility(idTask, 4)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isMatrixSubtaskCompleted.collectLatest {
                if (it > 0L) {
                    onUpdateSubtask(it)
                    viewModel.resetMatrixCompletionValue()
                }
            }
        }

        binding.playButton.setOnClickListener {
            setChrono()
        }

        binding.stopButton.setOnClickListener {
            stopChrono(dataStore)
        }


        return binding.root
    }

    private fun setChrono() {
        lifecycleScope.launch {
        val base = SystemClock.elapsedRealtime()
            when(idMatrix) {
                1 -> { saveTime("TIME1${idTask}", base)
                    serviceIntent.apply {
                        putExtra("Matrix_ID", "${idTask}1")
                        putExtra("Prefs_Time_Start", "TIME1${idTask}")
                        putExtra("Matrix_Name", getString(R.string.d_important_urgent))
                    }

                    saveVisibilityPermit("${idTask}1")
                    setPlayVisibility()
                }
                2 -> { saveTime("TIME2${idTask}", base)
                    serviceIntent.apply {
                        putExtra("Matrix_ID", "${idTask}2")
                        putExtra("Prefs_Time_Start", "TIME2${idTask}")
                        putExtra("Matrix_Name", getString(R.string.d_important_not_urgent))
                    }

                    saveVisibilityPermit("${idTask}2")
                    setPlayVisibility()
                }
                3 -> { saveTime("TIME3${idTask}", base)
                    serviceIntent.apply {
                        putExtra("Matrix_ID", "${idTask}3")
                        putExtra("Prefs_Time_Start", "TIME3${idTask}")
                        putExtra("Matrix_Name", getString(R.string.d_not_important_urgent))
                    }

                    saveVisibilityPermit("${idTask}3")
                    setPlayVisibility()
                }
                4 -> { saveTime("TIME4${idTask}", base)
                    serviceIntent.apply {
                        putExtra("Matrix_ID", "${idTask}4")
                        putExtra("Prefs_Time_Start", "TIME4${idTask}")
                        putExtra("Matrix_Name", getString(R.string.d_not_important_not_urgent))
                    }

                    saveVisibilityPermit("${idTask}4")
                    setPlayVisibility()
                }
            }
            serviceIntent.putExtra("TitleServiceNotification", when(idMatrix) {
                1 -> getString(R.string.d_important_urgent)
                2 -> getString(R.string.d_important_not_urgent)
                3 -> getString(R.string.d_not_important_urgent)
                4 -> getString(R.string.d_not_important_not_urgent)
                else -> return@launch
            })
            serviceIntent.apply {
                putExtra("ID", idMatrix + idTask)
                putExtra("ID_Task", idTask)
                putExtra("Task_Name", taskName)
            }
            ContextCompat.startForegroundService(app, serviceIntent)
            startChrono()
        }
    }

    private fun startChrono() {
        lifecycleScope.launch {
            val t = when(idMatrix) {
                1 -> loadTime("TIME1${idTask}")
                2 -> loadTime("TIME2${idTask}")
                3 -> loadTime("TIME3${idTask}")
                4 -> loadTime("TIME4${idTask}")
                else -> 0L
            }
            if (t == 0L) {
                listItemTimePermit = false
                return@launch
            }
            listItemTimePermit = true
            chronometer.base = t
            chronometer.start()
            if (!loadStartChronoPermit()) saveTimeBase(t)
            saveStartChronoPermit(true)
        }
    }

    private fun stopChrono(dataStore: DataStore<Preferences>) {
        lifecycleScope.launch {
            when(idMatrix) {
                1 -> { saveTime("TIME1${idTask}", 0L)
                    updateMatrixDatabase(idTask, 1, getString(R.string.important_urgent))
                    updateTodaySession(dataStore)
                    setStopVisibility()
                }
                2 -> { saveTime("TIME2${idTask}", 0L)
                    updateMatrixDatabase(idTask, 2, getString(R.string.important_not_urgent))
                    updateTodaySession(dataStore)
                    setStopVisibility()
                }
                3 -> { saveTime("TIME3${idTask}", 0L)
                    updateMatrixDatabase(idTask, 3, getString(R.string.not_important_urgent))
                    updateTodaySession(dataStore)
                    setStopVisibility()
                }
                4 -> { saveTime("TIME4${idTask}", 0L)
                    updateMatrixDatabase(idTask, 4, getString(R.string.not_important_not_urgent))
                    updateTodaySession(dataStore)
                    setStopVisibility()
                }
            }
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime()
            saveStartChronoPermit(false)

            stopService()
            saveVisibilityPermit("defValue")
        }
    }

    private fun stopService() {
        requireActivity().stopService(serviceIntent)
    }

    private fun onUpdateSubtask(idSubtask: Long) {
        lifecycleScope.launch {
            val timeBase = SystemClock.elapsedRealtime()
            val subtask = subtaskDataDao.getSubtaskById(idSubtask)
            val maxPosition = subtaskDataDao.getMaxPosition()?.inc() ?: 1
            val minPosition = subtaskDataDao.getMinPosition()?.dec() ?: 1
            if (subtask.isCompleted == 0) {
                subtask.isCompleted = 1
                subtask.position = minPosition
                when(listItemTimePermit) {
                    false -> subtask.subtaskFinishTime = 0L
                    else -> {
                        subtask.subtaskFinishTime = timeBase - loadTimeBase()
                        saveTimeBase(timeBase)
                    }
                }
            } else {
                subtask.isCompleted = 0
                subtask.position = maxPosition
                subtask.subtaskFinishTime = 0L
                saveTimeBase(timeBase)
            }
            subtaskDataDao.updateSubtask(subtask)
        }
    }

    private suspend fun saveTimeBase(time: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(TIME_BASE, time).apply()
    }


    private suspend fun loadTimeBase(): Long = withContext(Dispatchers.IO) {
        prefs.getLong(TIME_BASE, 0L)
    }

    private suspend fun updateTodaySession(dataStore: DataStore<Preferences>) {
        val timeBase = SystemClock.elapsedRealtime() - chronometer.base

            dataStore.edit {
                it[MATRIX_SESSION] = sessionValue + timeBase
            }
    }

    private suspend fun updateMatrixDatabase(idTask: Long, idMatrix: Int, matrixName: String) {
        val timeBase = SystemClock.elapsedRealtime() - chronometer.base

        if ( matrixResultDao.getMatrixResultById("$idTask$idMatrix") != null) {
            val matrixResult = matrixResultDao.getMatrixResultById("$idTask$idMatrix")
            val finishTime = matrixResult?.finishMatrixTime ?: 0L
            matrixResult?.finishMatrixTime = finishTime + timeBase
            matrixResult?.matrixName = matrixName
            matrixResultDao.updateMatrixResult(matrixResult!!)
        } else {
            val matrixResult = TaskMatrixResult(taskMatrixResultId = "$idTask$idMatrix",
                taskParentId = idTask, finishMatrixTime = timeBase, matrixName = matrixName)
            matrixResultDao.insertMatrixResult(matrixResult)
        }
    }

    private suspend fun checkVisibility(idTask: Long, idMatrix: Int) {
        when(loadVisibilityPermit()) {
            "$idTask$idMatrix" -> setPlayVisibility()
            "defValue" -> setStopVisibility()
            else -> setFullGoneVisibility()
        }
    }

    private fun setPlayVisibility() {
        binding.playButton.visibility = View.GONE
        binding.stopButton.visibility = View.VISIBLE
    }
    private fun setStopVisibility() {
        binding.stopButton.visibility = View.GONE
        binding.playButton.visibility = View.VISIBLE
    }

    private fun setFullGoneVisibility() {
        binding.playButton.visibility = View.GONE
        binding.stopButton.visibility = View.GONE
    }

    private suspend fun saveStartChronoPermit(value: Boolean) = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(CHRONO_PERMIT, value).apply()
    }

    private suspend fun loadStartChronoPermit() = withContext(Dispatchers.IO) {
        prefs.getBoolean(CHRONO_PERMIT, false)
    }

    private suspend fun saveVisibilityPermit(value: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString("Visibility_Permit", value).apply()
    }

    private suspend fun loadVisibilityPermit(): String? = withContext(Dispatchers.IO) {
        prefs.getString("Visibility_Permit", "defValue")
    }

    private suspend fun saveTime(tag: String, triggerTimer: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(tag, triggerTimer).apply()
    }

    private suspend fun loadTime(tag: String): Long = withContext(Dispatchers.IO) {
        prefs.getLong(tag, 0L)
    }
}