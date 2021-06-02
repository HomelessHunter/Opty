package com.example.tasksapp.description.dataandtimepickers

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.emptyPreferences
import androidx.lifecycle.lifecycleScope
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.databinding.DateDialogBinding
import com.example.tasksapp.description.TaskDescriptionViewModel
import com.example.tasksapp.settings.TIME_FORMAT
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

@ExperimentalCoroutinesApi
class DateDialog constructor(private val viewModel: TaskDescriptionViewModel, private val dataStore: DataStore<Preferences>) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "DateDialogTag"
    }
    private lateinit var calendar: Calendar

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val d = dialog as BottomSheetDialog
            val s: FrameLayout? = d.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(s!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DateDialogBinding.inflate(inflater, container, false)

        calendar = Calendar.getInstance(TimeZone.getDefault())
        binding.datePicker.minDate = calendar.timeInMillis

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
                    binding.timePick.setIs24HourView(false)
                } else {
                    binding.timePick.setIs24HourView(true)
                }
            }
        }

        binding.saveDateButton.setOnClickListener {
            calendar.set(binding.datePicker.year, binding.datePicker.month, binding.datePicker.dayOfMonth,
                binding.timePick.hour, binding.timePick.minute)
            viewModel.startTimer(calendar, calendar.timeInMillis)
            viewModel.getDate()
            dismiss()
        }
       return binding.root
    }
}