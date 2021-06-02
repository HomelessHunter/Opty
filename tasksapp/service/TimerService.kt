package com.example.tasksapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import androidx.navigation.NavDeepLinkBuilder
import com.example.tasksapp.R
import com.example.tasksapp.receiver.ACTION_PAUSE
import com.example.tasksapp.receiver.ID_SIGNAL
import com.example.tasksapp.receiver.PauseTimerReceiver
import com.example.tasksapp.receiver.TimerSignalReceiver
import com.example.tasksapp.timer.*
import com.example.tasksapp.utilities.Prefs
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import java.io.IOException

const val ID_TIMER = 121
val OPEN_TIMER = preferencesKey<String>("OPEN_TIMER")

class TimerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    companion object {

        val PAUSE_TIMER_TRIGGER = preferencesKey<Long>("PAUSE_TIMER_TRIGGER")
        val PAUSE_TIMER_TRIGGER_PERCENTAGE = preferencesKey<Long>("PAUSE_TIMER_TRIGGER_PERCENTAGE")
        val PAUSE_TIMER_MAIN = preferencesKey<Long>("PAUSE_TIMER_MAIN")
        val PAUSE_TIMER_MAIN_PERCENTAGE = preferencesKey<Long>("PAUSE_TIMER_MAIN_PERCENTAGE")
        val PAUSE_TIMER_REST = preferencesKey<Long>("PAUSE_TIMER_REST")
        val PAUSE_TIMER_REST_PERCENTAGE = preferencesKey<Long>("PAUSE_TIMER_REST_PERCENTAGE")

        val RETURN_MAIN_TRIGGER = preferencesKey<Long>("RETURN_MAIN_TRIGGER")
        val RETURN_MAIN_PERCENTAGE = preferencesKey<Long>("RETURN_MAIN_PERCENTAGE")
        val RETURN_REST_TRIGGER = preferencesKey<Long>("RETURN_REST_TRIGGER")
        val RETURN_REST_PERCENTAGE = preferencesKey<Long>("RETURN_REST_PERCENTAGE")
        val RETURN_CYCLE = preferencesKey<Long>("RETURN_CYCLE")
        val RETURN_TIMER_PERCENTAGE = preferencesKey<Long>("RETURN_TIMER_PERCENTAGE")
        val CYCLE_NAME = preferencesKey<String>("CYCLE_NAME")

        const val SIGNAL_TITLE = "SIGNAL_TITLE"

        const val SERVICE_OPEN_TIMER = "SERVICE_OPEN_TIMER"
        const val SERVICE_COMPLETED = "SERVICE_COMPLETED"
    }

    private var countDownTimer: CountDownTimer? = null
    private var notificationManager: NotificationManager? = null
    private lateinit var notification: Notification
    private lateinit var notificationLayout: RemoteViews
    private lateinit var pauseIntent: Intent
    private lateinit var pendingPauseIntent: PendingIntent
    private lateinit var signalIntent: Intent
    private lateinit var contentPendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager

    private var triggerTime = 1000L
    private var sum = 0L
    private var mainTrigger = 0L
    private var percentageMain = 0L
    private var restTrigger = 0L
    private var percentageRest = 0L
    private var cycle = 0L
    private var timerPercentage = 100L


    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var dataStore: DataStore<Preferences>

    override fun onCreate() {

        createChannel(getString(R.string.timer_notification_channel), getString(R.string.timer_notification_channel_name))
        alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        signalIntent = Intent(this, TimerSignalReceiver::class.java)
        pauseIntent = Intent(this, PauseTimerReceiver::class.java)
        notificationLayout = RemoteViews(packageName, R.layout.notification_timer)
        dataStore = Prefs.getInstance(applicationContext)
    }

    @ExperimentalCoroutinesApi
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

                triggerTime = intent.getLongExtra(TimerViewModel.TIMER_TRIGGER_TIME, 0L)
                sum = intent.getLongExtra(TimerViewModel.TIMER_SUM, 0L)
                mainTrigger = intent.getLongExtra(TimerViewModel.TIMER_MAIN_TRIGGER, 0L)
                percentageMain = intent.getLongExtra(TimerViewModel.TIMER_PERCENTAGE_MAIN, 0L)
                restTrigger = intent.getLongExtra(TimerViewModel.TIMER_REST_TRIGGER, 0L)
                percentageRest = intent.getLongExtra(TimerViewModel.TIMER_PERCENTAGE_REST, 0L)
                cycle = intent.getLongExtra(TimerViewModel.TIMER_CYCLE, 0L)
                timerPercentage = intent.getLongExtra(TimerViewModel.TIMER_TRIGGER_PERCENTAGE, 1L)


            notificationManager =
                ContextCompat.getSystemService(
                    applicationContext,
                    NotificationManager::class.java
                ) as NotificationManager


        pauseIntent.putExtra("TIMER_TRIGGER_TIME_PAUSE", triggerTime)
        pauseIntent.putExtra("TIMER_MAIN_TRIGGER_PAUSE", mainTrigger)
        pauseIntent.putExtra("TIMER_PERCENTAGE_MAIN_PAUSE", percentageMain)
        pauseIntent.putExtra("TIMER_REST_TRIGGER_PAUSE", restTrigger)
        pauseIntent.putExtra("TIMER_PERCENTAGE_REST_PAUSE", percentageRest)
        pauseIntent.putExtra("TIMER_CYCLE_PAUSE", cycle)
        pauseIntent.putExtra("TIMER_TRIGGER_PERCENTAGE_PAUSE", timerPercentage)

                coroutineScope.launch {
                    withContext(Dispatchers.IO){
                        dataStore.edit {
                            it[RETURN_MAIN_TRIGGER] = mainTrigger
                            it[RETURN_MAIN_PERCENTAGE] = percentageMain
                            it[RETURN_REST_TRIGGER] = restTrigger
                            it[RETURN_REST_PERCENTAGE] = percentageRest
                            it[RETURN_CYCLE] = cycle
                            it[RETURN_TIMER_PERCENTAGE] = timerPercentage
                            it[OPEN_TIMER] = SERVICE_OPEN_TIMER
                            it[PERMISSION_TO_CONTINUE_MAIN_TIMER] = true
                        }
                    }
                }




        val timerValue = triggerTime + SystemClock.elapsedRealtime()
            countDownTimer = object : CountDownTimer(timerValue, 1000) {

                override fun onFinish() {
                    countDownTimer?.cancel()
                    stopSelf()
                }

                override fun onTick(millisUntilFinished: Long) {
                    coroutineScope.launch {

                        val timerElapsedTime = timerValue - SystemClock.elapsedRealtime()

                        val timerPercentageToPause = timerElapsedTime.div(timerPercentage)

                        withContext(Dispatchers.IO) {
                            dataStore.edit {
                                it[TIMER_TRIGGER] = timerElapsedTime
                                it[PAUSE_TIMER_TRIGGER] = timerElapsedTime
                                it[PAUSE_TIMER_TRIGGER_PERCENTAGE] = timerPercentageToPause
                                it[TimerViewModel.SERVICE_PERMISSION] = true
                            }
                        }

                        if (timerElapsedTime <= 0) {
                            withContext(Dispatchers.IO) {
                                dataStore.edit {
                                    it[TimerViewModel.SERVICE_PERMISSION] = false
                                    it[OPEN_TIMER] = SERVICE_COMPLETED
                                }
                            }
                            countDownTimer?.cancel()
                            stopSelf()
                        }

                        val finishedCycles = timerElapsedTime.div(cycle.toFloat())

                        when (finishedCycles) {
                            in 0.0f..1.0f -> {
                                val offset = 1.0f - finishedCycles
                                val timeUntilFinish = cycle - (offset * cycle)
                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.activity_notification_title)
                                    )
                                    val mainTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val mainTriggerPercentageToPause =
                                        mainTriggerToPause.div(percentageMain)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )
                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (mainTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_rest)
                                        )

                                        sendBroadcast(signalIntent)
                                    }

                                    withContext(Dispatchers.IO) {
                                    activityTime(mainTriggerToPause, mainTriggerPercentageToPause, restTrigger)
                                    }


                                } else {
                                    val trigger =
                                        SystemClock.elapsedRealtime() + timeUntilFinish.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.timeout_notification_title)
                                    )
                                    val restTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val restTriggerPercentageToPause =
                                        restTriggerToPause.div(percentageRest)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )
                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (restTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_work)
                                        )
                                        sendBroadcast(signalIntent)
                                    }


                                    withContext(Dispatchers.IO){
                                    restTime(restTriggerToPause, restTriggerPercentageToPause, mainTrigger)
                                    }
                                }
                            }

                            in 1.0f..2.0f -> {
                                val offset = 2.0f - finishedCycles
                                val timeUntilFinish = cycle - (offset * cycle)
                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.activity_notification_title)
                                    )
                                    val mainTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val mainTriggerPercentageToPause =
                                        mainTriggerToPause.div(percentageMain)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (mainTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_rest)
                                        )
                                        sendBroadcast(signalIntent)
                                    }

                                    withContext(Dispatchers.IO) {
                                    activityTime(mainTriggerToPause, mainTriggerPercentageToPause, restTrigger)
                                    }

                                } else {
                                    val trigger =
                                        SystemClock.elapsedRealtime() + timeUntilFinish.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.timeout_notification_title)
                                    )
                                    val restTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val restTriggerPercentageToPause =
                                        restTriggerToPause.div(percentageRest)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (restTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_work)
                                        )
                                        sendBroadcast(signalIntent)
                                    }

                                    withContext(Dispatchers.IO) {
                                        restTime(
                                            restTriggerToPause,
                                            restTriggerPercentageToPause,
                                            mainTrigger
                                        )
                                    }

                                }
                            }

                            in 2.0f..3.0f -> {
                                val offset = 3.0f - finishedCycles
                                val timeUntilFinish = cycle - (offset * cycle)
                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.activity_notification_title)
                                    )
                                    val mainTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val mainTriggerPercentageToPause =
                                        mainTriggerToPause.div(percentageMain)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (mainTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_rest)
                                        )
                                        sendBroadcast(signalIntent)
                                    }

                                    withContext(Dispatchers.IO) {
                                        activityTime(
                                            mainTriggerToPause,
                                            mainTriggerPercentageToPause,
                                            restTrigger
                                        )
                                    }

                                } else {
                                    val trigger =
                                        SystemClock.elapsedRealtime() + timeUntilFinish.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.timeout_notification_title)
                                    )
                                    val restTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val restTriggerPercentageToPause =
                                        restTriggerToPause.div(percentageRest)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (restTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_work)
                                        )
                                        sendBroadcast(signalIntent)
                                    }
                                    withContext(Dispatchers.IO) {
                                        restTime(
                                            restTriggerToPause,
                                            restTriggerPercentageToPause,
                                            mainTrigger
                                        )
                                    }

                                }
                            }

                            in 3.0f..4.0f -> {
                                val offset = 4.0f - finishedCycles
                                val timeUntilFinish = cycle - (offset * cycle)
                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.activity_notification_title)
                                    )
                                    val mainTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val mainTriggerPercentageToPause =
                                        mainTriggerToPause.div(percentageMain)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (mainTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_rest)
                                        )
                                        sendBroadcast(signalIntent)
                                    }
                                    withContext(Dispatchers.IO) {
                                        activityTime(
                                            mainTriggerToPause,
                                            mainTriggerPercentageToPause,
                                            restTrigger
                                        )
                                    }

                                } else {
                                    val trigger =
                                        SystemClock.elapsedRealtime() + timeUntilFinish.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.timeout_notification_title)
                                    )
                                    val restTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val restTriggerPercentageToPause =
                                        restTriggerToPause.div(percentageRest)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (restTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_work)
                                        )
                                        sendBroadcast(signalIntent)
                                    }
                                    withContext(Dispatchers.IO) {
                                        restTime(
                                            restTriggerToPause,
                                            restTriggerPercentageToPause,
                                            mainTrigger
                                        )
                                    }

                                }
                            }

                            in 4.0f..5.0f -> {
                                val offset = 5.0f - finishedCycles
                                val timeUntilFinish = cycle - (offset * cycle)
                                if (timeUntilFinish > restTrigger) {
                                    val t = timeUntilFinish - restTrigger
                                    val trigger = SystemClock.elapsedRealtime() + t.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.activity_notification_title)
                                    )
                                    val mainTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val mainTriggerPercentageToPause =
                                        mainTriggerToPause.div(percentageMain)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (mainTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_rest)
                                        )
                                        sendBroadcast(signalIntent)
                                    }
                                    withContext(Dispatchers.IO) {
                                        activityTime(
                                            mainTriggerToPause,
                                            mainTriggerPercentageToPause,
                                            restTrigger
                                        )
                                    }

                                } else {
                                    val trigger =
                                        SystemClock.elapsedRealtime() + timeUntilFinish.toLong()
                                    notificationLayout.setTextViewText(
                                        R.id.title_timer,
                                        getString(R.string.timeout_notification_title)
                                    )
                                    val restTriggerToPause = trigger - SystemClock.elapsedRealtime()
                                    val restTriggerPercentageToPause =
                                        restTriggerToPause.div(percentageRest)
                                    notificationLayout.setTextViewText(
                                        R.id.timer_notification,
                                        setElapsedTime(trigger - SystemClock.elapsedRealtime())
                                    )

                                    notificationManager?.notify(ID_TIMER, notification)

                                    if (restTriggerToPause <= 2000) {
                                        signalIntent.putExtra(
                                            SIGNAL_TITLE,
                                            getString(R.string.time_to_work)
                                        )
                                        sendBroadcast(signalIntent)
                                    }
                                    withContext(Dispatchers.IO) {
                                        restTime(
                                            restTriggerToPause,
                                            restTriggerPercentageToPause,
                                            mainTrigger
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }

        pendingPauseIntent = PendingIntent.getBroadcast(this, ID_TIMER, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        contentPendingIntent = NavDeepLinkBuilder(applicationContext).apply {
            setGraph(R.navigation.navigation)
            setDestination(R.id.timer)
        }.createPendingIntent()
        notification = NotificationCompat.Builder(applicationContext, getString(R.string.timer_notification_channel)).apply {
            setSmallIcon(R.drawable.ic_foreground_icon)
            setShowWhen(false)
            setAutoCancel(true)
            setCustomContentView(notificationLayout)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setContentIntent(contentPendingIntent)
            addAction(R.drawable.ic_pause_black_24dp, getString(R.string.pause), pendingPauseIntent)
            priority = NotificationCompat.PRIORITY_LOW
        }.build()

        startForeground(ID_TIMER, notification)
        countDownTimer?.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        stopSelf()
        notificationManager?.cancelAll()
        coroutineScope.cancel()
    }

    private suspend fun activityTime(mainTriggerToPause: Long,
                                     mainTriggerPercentageToPause: Long,
                                     restTrigger: Long) {
        dataStore.edit {
            it[CYCLE_NAME] = getString(R.string.activity_notification_title)
            it[PAUSE_TIMER_MAIN] = mainTriggerToPause
            it[PAUSE_TIMER_MAIN_PERCENTAGE] = mainTriggerPercentageToPause
            it[PAUSE_TIMER_REST] = restTrigger
            it[PAUSE_TIMER_REST_PERCENTAGE] = 100L
        }
    }

    private suspend fun restTime(restTriggerToPause: Long,
                                 restTriggerPercentageToPause: Long,
                                 mainTrigger: Long) {
        dataStore.edit {
            it[CYCLE_NAME] = getString(R.string.timeout_notification_title)
            it[PAUSE_TIMER_REST] = restTriggerToPause
            it[PAUSE_TIMER_REST_PERCENTAGE] = restTriggerPercentageToPause
            it[PAUSE_TIMER_MAIN] = mainTrigger
            it[PAUSE_TIMER_MAIN_PERCENTAGE] = 100L
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Timer"
            }
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
