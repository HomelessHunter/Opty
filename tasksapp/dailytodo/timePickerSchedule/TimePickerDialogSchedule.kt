package com.example.tasksapp.dailytodo.timePickerSchedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.emptyPreferences
import androidx.lifecycle.lifecycleScope
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.TodoScheduleAdapter
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.database.DailyTodoSchedule
import com.example.tasksapp.databinding.ScheduleTimePickerBinding
import com.example.tasksapp.settings.TIME_FORMAT
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

@ExperimentalCoroutinesApi
class TimePickerDialogSchedule constructor(private val viewModel: DailyTasksViewModel,
                                           private val adapter: TodoScheduleAdapter,
                                           private val dailyTodoSchedule: DailyTodoSchedule,
                                           private val dataStore: DataStore<Preferences>) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "TimePicker_Dialog_Schedule_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ScheduleTimePickerBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            val timeFormat = dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.map { preferences ->
                    preferences[TIME_FORMAT] ?: false
                }

            timeFormat.collectLatest {
                if (it) {
                    binding.timePickerSchedule.setIs24HourView(false)
                } else {
                    binding.timePickerSchedule.setIs24HourView(true)
                }
            }
        }

            binding.timePickerSchedule.setOnTimeChangedListener { _, hourOfDay, minute ->

                binding.saveDateButtonSchedule.setOnClickListener {
                val date = (hourOfDay * 3_600_000L) + (minute * 60_000L)
                viewModel.setNotification(dailyTodoSchedule, date)
                lifecycleScope.launch {
                    viewModel.todoScheduleAdapterPosition.collectLatest { position ->
                        adapter.notifyItemChanged(position)
                    }
                }
                    dismiss()
            }
        }

        return binding.root
    }
}