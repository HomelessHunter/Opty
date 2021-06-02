package com.example.tasksapp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.navigation.NavDeepLinkBuilder
import com.example.tasksapp.R
import com.example.tasksapp.service.ID_TIMER
import com.example.tasksapp.service.OPEN_TIMER
import com.example.tasksapp.service.TimerService
import com.example.tasksapp.timer.*
import com.example.tasksapp.utilities.Prefs
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import kotlin.system.measureTimeMillis

const val ACTION_PAUSE = "ACTION_PAUSE"
class PauseTimerReceiver : BroadcastReceiver() {

    private lateinit var dTriggerTime: Flow<Long>

    private lateinit var dTriggerTimePercentage: Flow<Long>

    private lateinit var dMainTrigger: Flow<Long>

    private lateinit var dPercentageMain: Flow<Long>

    private lateinit var dRestTrigger: Flow<Long>

    private lateinit var dPercentageRest: Flow<Long>

    private lateinit var dCycle: Flow<Long>

    private lateinit var dTimerPercentage: Flow<Long>

    private lateinit var dMainTriggerToPause: Flow<Long>

    private lateinit var dMainTriggerPercentageToPause: Flow<Long>

    private lateinit var dRestTriggerToPause: Flow<Long>

    private lateinit var dRestTriggerPercentageToPause: Flow<Long>

    private lateinit var dCycleName: Flow<String>

    private lateinit var serviceIntent: Intent

    private lateinit var notificationManager: NotificationManager

    companion object {
        const val PAUSE_OPEN_TIMER = "PAUSE_OPEN_TIMER"
    }

    @ExperimentalCoroutinesApi
    override fun onReceive(context: Context, intent: Intent) {

        createChannel(context.getString(R.string.pause_timer_channel), "Pause timer", context)
        val dataStore = Prefs.getInstance(context)

        serviceIntent = Intent(context, TimerService::class.java)
        context.stopService(serviceIntent)
        serviceIntent.action = ACTION_PAUSE

        notificationManager =
            ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

        dTriggerTime = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.PAUSE_TIMER_TRIGGER] ?: 0L }.flowOn(Dispatchers.IO)

        dTriggerTimePercentage = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.PAUSE_TIMER_TRIGGER_PERCENTAGE] ?: 0L }.flowOn(Dispatchers.IO)

        dMainTrigger = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.RETURN_MAIN_TRIGGER] ?: 0L }.flowOn(Dispatchers.IO)

        dPercentageMain = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.RETURN_MAIN_PERCENTAGE] ?: 0L }.flowOn(Dispatchers.IO)

        dRestTrigger = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.RETURN_REST_TRIGGER] ?: 0L }.flowOn(Dispatchers.IO)

        dPercentageRest = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.RETURN_REST_PERCENTAGE] ?: 0L }.flowOn(Dispatchers.IO)

        dCycle = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.RETURN_CYCLE] ?: 0L }.flowOn(Dispatchers.IO)

        dTimerPercentage = dataStore.data.catch { e ->
                if (e is IOException){
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.RETURN_TIMER_PERCENTAGE] ?: 0L }.flowOn(Dispatchers.IO)

        dMainTriggerToPause = dataStore.data.catch { e ->
                if ( e is IOException ) {
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.PAUSE_TIMER_MAIN] ?: 0 }.flowOn(Dispatchers.IO)

        dMainTriggerPercentageToPause = dataStore.data.catch { e ->
                if ( e is IOException ) {
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.PAUSE_TIMER_MAIN_PERCENTAGE] ?: 0 }.flowOn(Dispatchers.IO)

        dRestTriggerToPause = dataStore.data.catch { e ->
                if ( e is IOException ) {
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.PAUSE_TIMER_REST] ?: 0 }.flowOn(Dispatchers.IO)

        dRestTriggerPercentageToPause = dataStore.data.catch { e ->
                if ( e is IOException ) {
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.PAUSE_TIMER_REST_PERCENTAGE] ?: 0 }.flowOn(Dispatchers.IO)

        dCycleName = dataStore.data.catch { e ->
                if ( e is IOException ) {
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.map { it[TimerService.CYCLE_NAME] ?: "" }.flowOn(Dispatchers.IO)

        GlobalScope.launch {

            var triggerTimeCurrent = 0L

            var triggerTimePercentage = 0L

            var mainTriggerToPause = 0L

            var mainTriggerPercentageToPause = 0L

            var restTriggerToPause = 0L

            var restTriggerPercentageToPause = 0L

            var cycleName = ""

            launch {
                dTriggerTime.collectLatest {
                    triggerTimeCurrent = it
                }
            }

            launch {
                dTriggerTimePercentage.collectLatest {
                    triggerTimePercentage = it
                }
            }


            launch {
                dMainTriggerToPause.collectLatest {
                    mainTriggerToPause = it
                }
            }

            launch {
                dMainTriggerPercentageToPause.collectLatest {
                    mainTriggerPercentageToPause = it
                }
            }

            launch {
                dRestTriggerToPause.collectLatest {
                    restTriggerToPause = it
                }
            }

            launch {
                dRestTriggerPercentageToPause.collectLatest {
                    restTriggerPercentageToPause = it
                }
            }

            launch {
                dCycleName.collectLatest {
                    cycleName = it
                }
            }
                val mainTrigger: Long = intent.getLongExtra("TIMER_MAIN_TRIGGER_PAUSE", 0L)
                val percentageMain: Long = intent.getLongExtra("TIMER_PERCENTAGE_MAIN_PAUSE", 0L)
                val restTrigger: Long = intent.getLongExtra("TIMER_REST_TRIGGER_PAUSE", 0L)
                val percentageRest: Long = intent.getLongExtra("TIMER_PERCENTAGE_REST_PAUSE", 0L)
                val cycle: Long = intent.getLongExtra("TIMER_CYCLE_PAUSE", 0L)
                val timerPercentage: Long = intent.getLongExtra("TIMER_TRIGGER_PERCENTAGE_PAUSE", 1L)

            launch {
                delay(100)
                serviceIntent.putExtra(TimerViewModel.TIMER_TRIGGER_TIME, triggerTimeCurrent)
                serviceIntent.putExtra(TimerViewModel.TIMER_MAIN_TRIGGER, mainTrigger)
                serviceIntent.putExtra(TimerViewModel.TIMER_PERCENTAGE_MAIN, percentageMain)
                serviceIntent.putExtra(TimerViewModel.TIMER_REST_TRIGGER, restTrigger)
                serviceIntent.putExtra(TimerViewModel.TIMER_PERCENTAGE_REST, percentageRest)
                serviceIntent.putExtra(TimerViewModel.TIMER_CYCLE, cycle)
                serviceIntent.putExtra(TimerViewModel.TIMER_TRIGGER_PERCENTAGE, timerPercentage)

                dataStore.edit {
                    it[TIMER_TRIGGER] = triggerTimeCurrent
                    it[TIMER_PERCENTAGE] = triggerTimePercentage
                    it[MAIN_TIMER_TRIGGER_PAUSE] = mainTriggerToPause
                    it[MAIN_TIMER_PERCENTAGE] = mainTriggerPercentageToPause
                    it[REST_TIMER_TRIGGER_PAUSE] = restTriggerToPause
                    it[REST_TIMER_PERCENTAGE] = restTriggerPercentageToPause
                    it[PERMISSION_TO_CONTINUE_MAIN_TIMER] = false
                    it[TimerViewModel.SERVICE_PERMISSION] = false
                    it[OPEN_TIMER] = PAUSE_OPEN_TIMER
                }

                val name = if (cycleName == context.getString(R.string.activity_notification_title)) {
                   mainTriggerToPause
                } else {
                   restTriggerToPause
                }

                val contentPendingIntent = NavDeepLinkBuilder(context).apply {
                    setGraph(R.navigation.navigation)
                    setDestination(R.id.timer)
                }.createPendingIntent()
                val notificationLayout = RemoteViews(context.packageName, R.layout.notification_timer)
                notificationLayout.setTextViewText(R.id.title_timer, cycleName)
                notificationLayout.setTextViewText(R.id.timer_notification, setElapsedTime(name))
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PendingIntent.getForegroundService(context, ID_TIMER, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                } else {
                    PendingIntent.getService(context, ID_TIMER, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                val notification = NotificationCompat.Builder(context, context.getString(R.string.pause_timer_channel)).apply {
                    setSmallIcon(R.drawable.ic_foreground_icon)
                    setShowWhen(false)
                    setOngoing(true)
                    setAutoCancel(true)
                    setContentIntent(contentPendingIntent)
                    setCustomContentView(notificationLayout)
                    setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    addAction(R.drawable.ic_play_arrow_black_24dp, context.getString(R.string.resume), pendingIntent)
                    priority = NotificationCompat.PRIORITY_LOW
                }.build()

                notificationManager.notify(ID_TIMER, notification)

            }
        }
    }

        private fun createChannel(channelId: String, channelName: String, context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Pause timer"
                }
                val notificationManager = context.getSystemService(
                    NotificationManager::class.java
                ) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
