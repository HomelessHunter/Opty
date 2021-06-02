package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.Subtask
import com.example.tasksapp.databinding.ListItemTaskdescriptionBinding


class TaskDescriptionAdapter(private val matrixDescriptionListener: MatrixDescriptionListener) :
    PagingDataAdapter<Subtask, TaskDescriptionAdapter.ViewHolder>(DIFF_CALLBACK) {


    class ViewHolder private constructor(private val binding: ListItemTaskdescriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        var subtask: Subtask? = null

        fun bind(task: Subtask?, matrixDescriptionListener: MatrixDescriptionListener) {
            subtask = task
            binding.task = task
            binding.matrixListener = matrixDescriptionListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = ListItemTaskdescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subtask = getItem(position)
        holder.bind(subtask, matrixDescriptionListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder.from(parent)

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Subtask>() {
            override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem.subtaskId == newItem.subtaskId
            }

            override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem == newItem
            }
        }
    }
}
class MatrixDescriptionListener(val listener: (setMatrixValue: Long) -> Unit) {
    fun setMatrixValue(value: Long) = listener(value)
}