package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.database.DailyTodoSchedule
import com.example.tasksapp.database.ScheduleWithTodo
import com.example.tasksapp.databinding.ListDailyScheduleBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class TodoScheduleAdapter(private val scheduleListener: ScheduleListener,
                          private val scheduleArchiveListener: ScheduleArchiveListener,
                          private val scheduleSetReminderListener: ScheduleReminderListener,
                          private val scheduleResetReminderListener: ScheduleResetReminderListener,
                          private val viewModel: DailyTasksViewModel) : PagingDataAdapter<ScheduleWithTodo, TodoScheduleAdapter.ScheduleTodoTaskViewHolder>(
    DIFF_CALLBACK) {

    class ScheduleTodoTaskViewHolder private constructor(private val binding: ListDailyScheduleBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(scheduleWithTodo: ScheduleWithTodo?,
                 scheduleListener: ScheduleListener,
                 scheduleArchiveListener: ScheduleArchiveListener,
                 scheduleSetReminderListener: ScheduleReminderListener,
                 scheduleResetReminderListener: ScheduleResetReminderListener,
                 position: Int,
                 viewModel: DailyTasksViewModel) {
            binding.scheduleVar = scheduleWithTodo
            binding.listener = scheduleListener
            binding.archiveListener = scheduleArchiveListener
            binding.reminderListener = scheduleSetReminderListener
            binding.resetReminderListener = scheduleResetReminderListener
            binding.position = position
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ScheduleTodoTaskViewHolder {
                val binding = ListDailyScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                return ScheduleTodoTaskViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ScheduleTodoTaskViewHolder, position: Int) {
        val scheduleWithTodo = getItem(position)
        holder.bind(scheduleWithTodo, scheduleListener, scheduleArchiveListener, scheduleSetReminderListener, scheduleResetReminderListener, position, viewModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleTodoTaskViewHolder{
        return ScheduleTodoTaskViewHolder.from(parent)
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ScheduleWithTodo>() {
            override fun areItemsTheSame(oldItem: ScheduleWithTodo, newItem: ScheduleWithTodo): Boolean {
                return oldItem.schedule.todoScheduleId == newItem.schedule.todoScheduleId
            }

            override fun areContentsTheSame(oldItem: ScheduleWithTodo, newItem: ScheduleWithTodo): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class ScheduleListener(val listener: (showInfo: String) -> Unit) {
    fun showInfo(value: String) = listener(value)
}

class ScheduleArchiveListener(val listener: (archiveVal: DailyTodoSchedule) -> Unit) {
    fun setArchiveVal(value: DailyTodoSchedule) = listener(value)
}

class ScheduleReminderListener(val listener: (reminderVal: DailyTodoSchedule, position: Int) -> Unit) {
    fun setReminder(value: DailyTodoSchedule, position: Int) = listener(value, position)
}

class ScheduleResetReminderListener(val listener: (resetReminderVal: DailyTodoSchedule, position: Int) -> Unit) {
    fun resetReminder(value: DailyTodoSchedule, position: Int) = listener(value, position)
}
