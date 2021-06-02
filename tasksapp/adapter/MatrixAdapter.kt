package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.Subtask
import com.example.tasksapp.databinding.MatrixListItemBinding

class MatrixAdapter(private val matrixItemCompletionListener: MatrixItemCompletionListener) : ListAdapter<Subtask, MatrixAdapter.MatrixViewHolder>(DIFF_CALLBACK) {

    class MatrixViewHolder private constructor(private val binding: MatrixListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subtask: Subtask, matrixItemCompletionListener: MatrixItemCompletionListener) {
            binding.subtask = subtask
            binding.listener = matrixItemCompletionListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MatrixViewHolder {
                val binding = MatrixListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MatrixViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: MatrixViewHolder, position: Int) {
        holder.bind(getItem(position), matrixItemCompletionListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatrixViewHolder {
        return MatrixViewHolder.from(parent)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Subtask>() {
            override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem.subtaskId == newItem.subtaskId
            }

            override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class MatrixItemCompletionListener(val listener: (setCompletion: Long) -> Unit) {
    fun setCompletion(value: Long) = listener(value)
}