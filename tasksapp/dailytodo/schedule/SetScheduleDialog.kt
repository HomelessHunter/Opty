package com.example.tasksapp.dailytodo.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.databinding.ScheduleSetterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class SetScheduleDialog (private val viewModel: DailyTasksViewModel) : BottomSheetDialogFragment() {

    private val formatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    companion object {
        const val TAG = "SET_SCHEDULE_DIALOG"
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
        val binding = ScheduleSetterBinding.inflate(inflater, container, false)

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val datePicker = binding.scheduleDatePicker

        datePicker.minDate = calendar.timeInMillis

        binding.setDateButton.setOnClickListener {
            calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth, 0, 0)
            val calendarTime = calendar.timeInMillis
            viewModel.insertSchedule(formatter.format(calendarTime), calendarTime)
            dismiss()
        }

        return binding.root
    }
}