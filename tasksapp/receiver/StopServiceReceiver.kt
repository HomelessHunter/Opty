package com.example.tasksapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import com.example.tasksapp.database.*
import com.example.tasksapp.service.ChronoService
import com.example.tasksapp.utilities.Prefs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.*

class StopServiceReceiver : BroadcastReceiver() {

    companion object {
        const val STOP_RECEIVER_TAG = "STOP_RECEIVER_TAG"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("com.example.taskapp", Context.MODE_PRIVATE)
        val dataStore = Prefs.getInstance(context)

        val completeTime = SystemClock.elapsedRealtime() - intent.getLongExtra("startTime", 0L)
        val matrixId = intent.getStringExtra("idTaskMatrix") ?: ""
        val taskId = intent.getLongExtra("idTask", 0L)
        val prefsTimeStart = intent.getStringExtra("Prefs_Time")
        val matrixTag = intent.getStringExtra("Matrix_Tag") ?: ""
        val serviceIntent = Intent(context, ChronoService::class.java)
        val MATRIX_SESSION = preferencesKey<Long>("MATRIX_SESSION$taskId")
        val session = dataStore.data.catch { e ->
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }.map { it[MATRIX_SESSION] ?: 0L }.flowOn(Dispatchers.IO)
        var sessionValue = 0L

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                prefs.edit().putInt(STOP_RECEIVER_TAG, 1).apply()
                prefs.edit().putString("Visibility_Permit", "defValue").apply()
                prefs.edit().putLong(prefsTimeStart, 0L).apply()
            }
            val database = TaskDatabase.getInstance(context.applicationContext)
            updateMatrixDatabase(database.matrixResultDao, matrixId, taskId, completeTime, matrixTag)

            launch {
                session.collectLatest {
                    sessionValue = it
                }
            }
            delay(100)
            updateTodaySession(dataStore, MATRIX_SESSION, sessionValue, completeTime)
        }

        context.stopService(serviceIntent)
    }

    private suspend fun updateMatrixDatabase(database: MatrixResultDao, matrixId: String, taskId: Long, completeTime: Long, matrixTag: String) {
        if ( database.getMatrixResultById(matrixId) != null) {
            val matrixResult = database.getMatrixResultById(matrixId)
            val finishTime = matrixResult?.finishMatrixTime ?: 0L
            matrixResult?.finishMatrixTime = finishTime + completeTime
            matrixResult?.matrixName = matrixTag
            database.updateMatrixResult(matrixResult!!)
        } else {
            val matrixResult = TaskMatrixResult(taskMatrixResultId = matrixId,
                taskParentId = taskId, finishMatrixTime = completeTime, matrixName = matrixTag)
            database.insertMatrixResult(matrixResult)
        }
    }

    private suspend fun updateTodaySession(dataStore: DataStore<Preferences>, matrixSession: Preferences.Key<Long>, sessionValue: Long, timeBase: Long) {
        dataStore.edit {
            it[matrixSession] = sessionValue + timeBase
        }
    }
}