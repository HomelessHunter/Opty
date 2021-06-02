package com.example.tasksapp.dailytodo.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.*
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.dailytodo.timePickerSchedule.TimePickerDialogSchedule
import com.example.tasksapp.databinding.DailyScheduleFragmentBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class TodoSchedule : Fragment() {

    private val viewModel: DailyTasksViewModel by activityViewModels()
    private lateinit var binding: DailyScheduleFragmentBinding

    companion object {
        const val SCHEDULE_MESSAGE = "SCHEDULE_MESSAGE"
        const val SCHEDULE_NOTIFICATION_ID = "SCHEDULE_NOTIFICATION_ID"
        const val SCHEDULE_ACTION = "SCHEDULE_ACTION"
        const val SCHEDULE_ID = "SCHEDULE_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DailyScheduleFragmentBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            viewModel.dSFormat.collectLatest {
                viewModel.setFormat(it)
                Log.e("Schedule_format_flow", "$it")
            }
        }

        val adapter = TodoScheduleAdapter(
            ScheduleListener {
                viewModel.setInfo(it)
            },
            ScheduleArchiveListener {
                viewModel.setScheduleArchive(it)
            },
            ScheduleReminderListener { schedule, position ->
                viewModel.setReminderVal(schedule)
                viewModel.setTodoScheduleAdapterPosition(position)
            },
            ScheduleResetReminderListener { schedule, position ->
                viewModel.setResetReminderVal(schedule)
                viewModel.setTodoScheduleAdapterPosition(position)
            },
            viewModel
        )
        val recyclerView = binding.scheduleRecyclerView
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            viewModel.scheduleWithTodo.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            viewModel.scheduleWithTodoEmptyCheck.collect {
                if (it.isNullOrEmpty()) {
                    binding.emptyScheduleBlob.visibility = View.VISIBLE
                    binding.emptyScheduleText.visibility = View.VISIBLE
                } else {
                    binding.emptyScheduleBlob.visibility = View.GONE
                    binding.emptyScheduleText.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.routineSize.collectLatest {
                binding.routineCounter.text = getString(R.string.todoCounterText, it.size.toString())
            }
        }

        binding.addSchedule.setOnClickListener {
            val dialog = SetScheduleDialog(viewModel)
            if (childFragmentManager.findFragmentByTag(SetScheduleDialog.TAG) == null) {
                dialog.show(childFragmentManager, SetScheduleDialog.TAG)
            }
        }

        binding.closeSchedule.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.archiveFolder.setOnClickListener {
            findNavController().navigate(TodoScheduleDirections.actionTodoScheduleToScheduleArchive())
        }

        binding.dailyRoutineCard.setOnClickListener {
            findNavController().navigate(TodoScheduleDirections.actionTodoScheduleToRoutine())
        }

        viewModel.info.observe(viewLifecycleOwner, {
            it?.let {
                findNavController().navigate(TodoScheduleDirections.actionTodoScheduleToScheduleInfo(it))
                viewModel.setTodoScheduleItem(it)
                viewModel.resetInfo()
            }
        })

        viewModel.scheduleArchive.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.updateSchedule(it, 1)
                viewModel.updateDailyTodo(it)
                viewModel.resetScheduleArchive()
            }
        })

//        TODO setReminder
        viewModel.reminderVal.observe(viewLifecycleOwner, {
            it?.let { dailyTodoSchedule ->
                val bottomSheet = TimePickerDialogSchedule(viewModel, adapter, dailyTodoSchedule, viewModel.dataStore)
                if(childFragmentManager.findFragmentByTag(TimePickerDialogSchedule.TAG) == null) {
                    bottomSheet.show(childFragmentManager, TimePickerDialogSchedule.TAG)
                }
                viewModel.resetReminderVal()
            }
        })

        viewModel.resetReminderVal.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.resetNotification(it, 0L)
                lifecycleScope.launch {
                    viewModel.todoScheduleAdapterPosition.collectLatest { position ->
                        adapter.notifyItemChanged(position)
                    }
                }
                viewModel.resetResetReminderVal()
            }
        })

        createChannel(getString(R.string.schedule_notification_channel),
            getString(R.string.schedule_notification_channel_name))

        return binding.root
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                description = getString(R.string.schedule_notification_channel_name)
            }
            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}