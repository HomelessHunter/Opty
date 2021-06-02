package com.example.tasksapp.timer

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tasksapp.service.OPEN_TIMER
import com.example.tasksapp.service.TimerService
import com.example.tasksapp.utilities.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

val TIMER_TRIGGER = preferencesKey<Long>("TIMER_TRIGGER")
val TIMER_PERCENTAGE = preferencesKey<Long>("TIMER_PERCENTAGE")
val SUM = preferencesKey<Long>("SUM")
val MAIN_TIMER_TRIGGER = preferencesKey<Long>("MAIN_TIMER_TRIGGER")
val MAIN_TIMER_TRIGGER_PAUSE = preferencesKey<Long>("MAIN_TIMER_TRIGGER_PAUSE")
val MAIN_TIMER_PERCENTAGE = preferencesKey<Long>("MAIN_TIMER_PERCENTAGE")
val REST_TIMER_TRIGGER = preferencesKey<Long>("REST_TRIGGER_TIMER")
val REST_TIMER_TRIGGER_PAUSE = preferencesKey<Long>("REST_TIMER_TRIGGER_PAUSE")
val REST_TIMER_PERCENTAGE = preferencesKey<Long>("REST_TIMER_PERCENTAGE")
val PERMISSION_TO_CONTINUE_MAIN_TIMER = preferencesKey<Boolean>("PERMISSION_TO_CONTINUE_MAIN_TIMER")
const val PLAY_BUTTON_VISIBILITY = "PLAY_BUTTON_VISIBILITY"
const val PAUSE_BUTTON_VISIBILITY = "PAUSE_BUTTON_VISIBILITY"
const val STOP_BUTTON_VISIBILITY = "STOP_BUTTON_VISIBILITY"
const val SLIDERS_VISIBILITY = "SLIDERS_VISIBILITY"
const val MAIN_OPEN_TIMER = "MAIN_OPEN_TIMER"
private const val minute = 60_000L

@ExperimentalCoroutinesApi
class TimerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("com.example.taskapp", Context.MODE_PRIVATE)
    var mainCountDownTimer: CountDownTimer? = null
    val service = Intent(app, TimerService::class.java)
    val dataStore = Prefs.getInstance(app.applicationContext)

    val openTimer = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[OPEN_TIMER] ?: "" }

    val triggerTime =  dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[TIMER_TRIGGER] ?: 0L }

    val triggerPercentage = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[TIMER_PERCENTAGE] ?: 100L }

    val sum = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[SUM] ?: 0L }

    val mainTriggerTime = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[MAIN_TIMER_TRIGGER] ?: 0L }

    val mainTriggerTimePause = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[MAIN_TIMER_TRIGGER_PAUSE] ?: 0L }

    val mainTriggerPercentage = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[MAIN_TIMER_PERCENTAGE] ?: 100L }

    val restTriggerTime = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[REST_TIMER_TRIGGER] ?: 0L }

    val restTriggerTimePause = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[REST_TIMER_TRIGGER_PAUSE] ?: 0L }

    val restTriggerPercentage = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[REST_TIMER_PERCENTAGE] ?: 100L }

    val startTimer = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[PERMISSION_TO_CONTINUE_MAIN_TIMER] ?: true }

    val servicePermission = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { it[SERVICE_PERMISSION] ?: false }

    private val loadedTriggerTime = MutableStateFlow(0L)

    val loadedTriggerPercentage = MutableStateFlow(100L)

    private val loadedSum = MutableStateFlow(0L)

    private val loadedMainTriggerTime = MutableStateFlow(0L)

    val loadedMainTriggerTimePause = MutableStateFlow(0L)

    val loadedMainTriggerPercentage = MutableStateFlow(100L)

    private val loadedRestTriggerTime = MutableStateFlow(0L)

    val loadedRestTriggerTimePause = MutableStateFlow(0L)

    val loadedRestTriggerPercentage = MutableStateFlow(100L)

    val loadedStartTimer = MutableStateFlow(true)

    val loadedServicePermission = MutableStateFlow(false)


    fun setLoadedTriggerTime(value: Long) {
        loadedTriggerTime.value = value
    }

    fun setLoadedTriggerPercentage(value: Long) {
        loadedTriggerPercentage.value = value
    }

    fun setLoadedSum(value: Long) {
        loadedSum.value = value
    }

    fun setLoadedMainTriggerTime(value: Long) {
        loadedMainTriggerTime.value = value
    }

    fun setLoadedMainTriggerTimePause(value: Long) {
        loadedMainTriggerTimePause.value = value
    }

    fun setLoadedMainTriggerPercentage(value: Long) {
        loadedMainTriggerPercentage.value = value
    }

    fun setLoadedRestTriggerTime(value: Long) {
        loadedRestTriggerTime.value = value
    }

    fun setLoadedRestTriggerTimePause(value: Long) {
        loadedRestTriggerTimePause.value = value
    }

    fun setLoadedRestTriggerPercentage(value: Long) {
        loadedRestTriggerPercentage.value = value
    }

    fun setLoadedStartTimer(value: Boolean) {
        loadedStartTimer.value = value
    }

    fun setLoadedServicePermission(value: Boolean) {
        loadedServicePermission.value = value
    }



    private val _timerElapsedTime = MutableStateFlow(0L)
        val timerElapsedTime: StateFlow<Long> = _timerElapsedTime

    private val _timerPercentage = MutableStateFlow(100L)
        val timePercentage: StateFlow<Long> = _timerPercentage

    private val _mainTimerElapsedTime = MutableStateFlow(0L)
        val mainTimerElapsedTime: StateFlow<Long> = _mainTimerElapsedTime

    private val _mainTimerPercentage = MutableStateFlow(100L)
        val mainTimerPercentage: StateFlow<Long> = _mainTimerPercentage

    private val _sliderValue = MutableStateFlow(1)

    private val _restTimerElapsedTime = MutableStateFlow(0L)
        val restTimerElapsedTime: StateFlow<Long> = _restTimerElapsedTime

    private val _restTimerPercentage = MutableStateFlow(100L)
        val restTimerPercentage: StateFlow<Long> = _restTimerPercentage

    private val _restSliderValue = MutableStateFlow(1)

    private val _repeatSliderValue = MutableStateFlow(1)


    private val _stopTimerLiveScenario = MutableLiveData<Boolean?>()


    fun setMainTimerValue(value: Long) {
        _mainTimerElapsedTime.value = value
    }

    fun setMainTimerPercentage(value: Long) {
        _mainTimerPercentage.value = value
    }

    fun setRestTimerValue(value: Long) {
        _restTimerElapsedTime.value = value
    }

    fun setRestTimerPercentage(value: Long) {
        _restTimerPercentage.value = value
    }

    fun setTimerPercentage(value: Long) {
        _timerPercentage.value = value
    }


    fun startMainTimer() {
        viewModelScope.launch {
            saveStartTimerPermission(true)

            val mainSlider: Int = _sliderValue.value
            val restSlider: Int = _restSliderValue.value
            val repeatSlider: Int = _repeatSliderValue.value

                val timerValue = mainSlider * minute

                val restValue = restSlider * minute

                val s = loadedTriggerTime.value
                if (s > 0) {
                    saveTriggerTime(s)

                    createTestMainTimer()
                } else {
                    saveMainTriggerTime(timerValue)
                    saveRestTriggerTime(restValue)

                    val timerVar = (timerValue + restValue) * repeatSlider
                    val time = ((timerValue + restValue) * repeatSlider)

                    saveSum(timerVar)
                    saveTriggerTime(time)
                    createTestMainTimer()
                }
        }
    }

    companion object {
        const val TIMER_TRIGGER_TIME = "TIMER_TRIGGER_TIME"
        const val TIMER_SUM = "TIMER_SUM"
        const val TIMER_MAIN_TRIGGER = "TIMER_MAIN_TRIGGER"
        const val TIMER_REST_TRIGGER = "TIMER_REST_TRIGGER"
        const val TIMER_CYCLE = "TIMER_CYCLE"
        const val TIMER_PERCENTAGE_MAIN = "TIMER_PERCENTAGE_MAIN"
        const val TIMER_PERCENTAGE_REST = "TIMER_PERCENTAGE_REST"
        const val TIMER_TRIGGER_PERCENTAGE = "TIMER_TRIGGER_PERCENTAGE"
        val SERVICE_PERMISSION = preferencesKey<Boolean>("SERVICE_PERMISSION")
    }

    fun createTestMainTimer() {
        viewModelScope.launch {

            val loadTime = loadedTriggerTime.value
            service.putExtra(TIMER_TRIGGER_TIME, loadTime)

            val timerTrigger = loadTime + SystemClock.elapsedRealtime()


            val sum = loadedSum.value
            service.putExtra(TIMER_SUM, sum)

            if (loadTime == 0L ) {

                return@launch
            }

            val mainTrigger = loadedMainTriggerTime.value
            service.putExtra(TIMER_MAIN_TRIGGER, mainTrigger)



            val restTrigger = loadedRestTriggerTime.value
            service.putExtra(TIMER_REST_TRIGGER, restTrigger)



            val cycle = mainTrigger + restTrigger
            service.putExtra(TIMER_CYCLE, cycle)


            val percentageMain = mainTrigger.div(100)
            service.putExtra(TIMER_PERCENTAGE_MAIN, percentageMain)

            val percentageRest = restTrigger.div(100)
            service.putExtra(TIMER_PERCENTAGE_REST, percentageRest)

            val timerPercentage = sum.div(100)
            service.putExtra(TIMER_TRIGGER_PERCENTAGE, timerPercentage)



            mainCountDownTimer = object : CountDownTimer(timerTrigger, 1000L) {
                override fun onFinish() {

                    stopTimer()
                }

                override fun onTick(millisUntilFinished: Long) {
                    viewModelScope.launch {
                        _timerElapsedTime.value = timerTrigger - SystemClock.elapsedRealtime()
                        _timerPercentage.value = _timerElapsedTime.value.div(timerPercentage)
                        val finishedCycles = _timerElapsedTime.value.div(cycle.toFloat())

                        if(_timerElapsedTime.value <= 0) {
                            stopTimer()
                        }

                        when(finishedCycles) {
                            in 0.0f..1.0f -> {
                                val offset = 1.0f - finishedCycles
                                val timeUntilFinish = cycle - (offset * cycle)
                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    _mainTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _mainTimerPercentage.value = _mainTimerElapsedTime.value.div(percentageMain)

                                    _restTimerElapsedTime.value = restTrigger
                                    _restTimerPercentage.value = 100L
                                } else {
                                    val trigger = SystemClock.elapsedRealtime() + timeUntilFinish.toLong()
                                    _restTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _restTimerPercentage.value = _restTimerElapsedTime.value.div(percentageRest)

                                    _mainTimerElapsedTime.value = mainTrigger
                                    _mainTimerPercentage.value = 100L
                                }
                            }

                            in 1.0f..2.0f -> {
                                val offset = 2.0f - finishedCycles
                                val timeUntilFinish = cycle - (offset * cycle)
                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    _mainTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _mainTimerPercentage.value = _mainTimerElapsedTime.value.div(percentageMain)

                                    _restTimerElapsedTime.value = restTrigger
                                    _restTimerPercentage.value = 100L

                                } else {
                                    val trigger = SystemClock.elapsedRealtime() + timeUntilFinish.toLong()
                                    _restTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _restTimerPercentage.value = _restTimerElapsedTime.value.div(percentageRest)

                                    _mainTimerElapsedTime.value = mainTrigger
                                    _mainTimerPercentage.value = 100L

                                }

                            }

                            in 2.0f..3.0f -> {
                                val offset = 3.0f - finishedCycles

                                val timeUntilFinish = cycle - (offset * cycle)

                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    _mainTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _mainTimerPercentage.value = _mainTimerElapsedTime.value.div(percentageMain)

                                    _restTimerElapsedTime.value = restTrigger
                                    _restTimerPercentage.value = 100L

                                } else {
                                    val trigger = SystemClock.elapsedRealtime() + timeUntilFinish.toLong()

                                    _restTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _restTimerPercentage.value = _restTimerElapsedTime.value.div(percentageRest)

                                    _mainTimerElapsedTime.value = mainTrigger
                                    _mainTimerPercentage.value = 100L

                                }

                            }
                            in 3.0f..4.0f -> {
                                val offset = 4.0f - finishedCycles

                                val timeUntilFinish = cycle - (offset * cycle)

                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    _mainTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _mainTimerPercentage.value = _mainTimerElapsedTime.value.div(percentageMain)

                                    _restTimerElapsedTime.value = restTrigger
                                    _restTimerPercentage.value = 100L

                                } else {
                                    val trigger = SystemClock.elapsedRealtime() + timeUntilFinish.toLong()

                                    _restTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _restTimerPercentage.value = _restTimerElapsedTime.value.div(percentageRest)

                                    _mainTimerElapsedTime.value = mainTrigger
                                    _mainTimerPercentage.value = 100L

                                }

                            }
                            in 4.0f..5.0f -> {
                                val offset = 5.0f - finishedCycles

                                val timeUntilFinish = cycle - (offset * cycle)

                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    _mainTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _mainTimerPercentage.value = _mainTimerElapsedTime.value.div(percentageMain)

                                    _restTimerElapsedTime.value = restTrigger
                                    _restTimerPercentage.value = 100L

                                } else {
                                    val trigger = SystemClock.elapsedRealtime() + timeUntilFinish.toLong()

                                    _restTimerElapsedTime.value = trigger - SystemClock.elapsedRealtime()
                                    _restTimerPercentage.value = _restTimerElapsedTime.value.div(percentageRest)

                                    _mainTimerElapsedTime.value = mainTrigger
                                    _mainTimerPercentage.value = 100L

                                }

                            }
                        }

                    }
                }
            }
            if (loadedStartTimer.value) {
                mainCountDownTimer?.start()
            }
        }
    }

    fun stopTimer() {
        viewModelScope.launch {

            mainCountDownTimer?.cancel()
            mainCountDownTimer = null
            _timerElapsedTime.value = 0
            _timerPercentage.value = 100
            _mainTimerElapsedTime.value = 0
            _mainTimerPercentage.value = 100
            _restTimerElapsedTime.value = 0
            _restTimerPercentage.value = 100

            saveSlidersVisibility(true)
            saveTriggerTime(0)
            saveRestTriggerTime(0)
            saveRestTriggerTimePause(0)
            saveMainTriggerTime(0)

            saveMainTriggerTimePause(0)
            saveTriggerPercentage(100L)
            saveMainTriggerPercentage(100L)
            saveRestTriggerPercentage(100L)
            saveStartTimerPermission(true)
            saveServicePermission(false)
            _stopTimerLiveScenario.value = true
            saveStopButtonVisibility(false)
            savePlayButtonVisibility(true)
            savePauseButtonVisibility(false)

            dataStore.edit {
                it[OPEN_TIMER] = MAIN_OPEN_TIMER
            }

        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            mainCountDownTimer?.cancel()
            mainCountDownTimer = null
            saveTriggerTime(_timerElapsedTime.value)
            saveTriggerPercentage(_timerPercentage.value)
            saveMainTriggerTimePause(_mainTimerElapsedTime.value)
            saveMainTriggerPercentage(_mainTimerPercentage.value)
            saveRestTriggerTimePause(_restTimerElapsedTime.value)
            saveRestTriggerPercentage(_restTimerPercentage.value)
            saveStartTimerPermission(false)
            saveServicePermission(false)
        }
    }

    fun setSliderValue(value: Int) {
        _sliderValue.value = value
    }

    fun setRestSlider(value: Int) {
        _restSliderValue.value = value
    }

    fun setRepeatSlider(value: Int) {
        _repeatSliderValue.value = value
    }


    private suspend fun saveTriggerTime(triggerTime: Long) {
        dataStore.edit {
            it[TIMER_TRIGGER] = triggerTime
        }
    }

    private suspend fun saveTriggerPercentage(percents: Long) {
        dataStore.edit {
            it[TIMER_PERCENTAGE] = percents
        }
    }


    private suspend fun saveSum(value: Long) {
        dataStore.edit {
            it[SUM] = value
        }
    }


    private suspend fun saveMainTriggerTime(mainTriggerTime: Long) {
        dataStore.edit {
            it[MAIN_TIMER_TRIGGER] = mainTriggerTime
        }
     }

    private suspend fun saveMainTriggerTimePause(mainTriggerTime: Long) {
        dataStore.edit {
            it[MAIN_TIMER_TRIGGER_PAUSE] = mainTriggerTime
        }

    }

    private suspend fun saveMainTriggerPercentage(percents: Long) {
        dataStore.edit {
            it[MAIN_TIMER_PERCENTAGE] = percents
        }
    }


    private suspend fun saveRestTriggerTime(restTriggerTime: Long) {
        dataStore.edit {
            it[REST_TIMER_TRIGGER] = restTriggerTime
        }
    }


    private suspend fun saveRestTriggerTimePause(restTriggerTime: Long) {
        dataStore.edit {
            it[REST_TIMER_TRIGGER_PAUSE] = restTriggerTime
        }
    }



    private suspend fun saveRestTriggerPercentage(percents: Long) {
        dataStore.edit {
            it[REST_TIMER_PERCENTAGE] = percents
        }
    }

    private suspend fun saveStartTimerPermission(permission: Boolean) {
    dataStore.edit { preferences ->
        preferences[PERMISSION_TO_CONTINUE_MAIN_TIMER] = permission
    }
    }


    suspend fun savePlayButtonVisibility(visibility: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(PLAY_BUTTON_VISIBILITY, visibility).apply()
        }
    }
    suspend fun getPlayButtonVisibility() = withContext(Dispatchers.IO) {
        prefs.getBoolean(PLAY_BUTTON_VISIBILITY, true)
    }

    suspend fun savePauseButtonVisibility(visibility: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(PAUSE_BUTTON_VISIBILITY, visibility).apply()
        }
    }
    suspend fun getPauseButtonVisibility() = withContext(Dispatchers.IO) {
        prefs.getBoolean(PAUSE_BUTTON_VISIBILITY, false)
    }

    suspend fun saveStopButtonVisibility(visibility: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(STOP_BUTTON_VISIBILITY, visibility).apply()
        }
    }
    suspend fun getStopButtonVisibility() = withContext(Dispatchers.IO) {
        prefs.getBoolean(STOP_BUTTON_VISIBILITY, false)
    }

    suspend fun saveSlidersVisibility(visibility: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(SLIDERS_VISIBILITY, visibility).apply()
        }
    }

    suspend fun getSlidersVisibility() = withContext(Dispatchers.IO) {
        prefs.getBoolean(SLIDERS_VISIBILITY, true)
    }

    private suspend fun saveServicePermission(value: Boolean) {
        dataStore.edit {
            it[SERVICE_PERMISSION] = value
        }
    }

    fun setServicePermit(value: Boolean) {
        viewModelScope.launch {
            saveServicePermission(value)
        }
    }
}
