package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.Tag
import com.example.tasksapp.databinding.ListItemTagsBinding


class TagsAdapter(private val tagListener: TagListener) : ListAdapter<Tag, TagsAdapter.TagViewHolder>(DIFF_CALLBACK) {

    class TagViewHolder private constructor(private val binding: ListItemTagsBinding) : RecyclerView.ViewHolder(binding.root) {
        var deleteTagValue: Tag? = null

        fun bind(tag: Tag, tagListener: TagListener) {
            deleteTagValue = tag
            binding.tag = tag
            binding.listener = tagListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TagViewHolder {
                val binding = ListItemTagsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TagViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position), tagListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder.from(parent)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Tag>() {
            override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem.tagsId == newItem.tagsId
            }

            override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class TagListener(val listener: (tagName: String) -> Unit) {
    fun passTagName(value: String) = listener(value)
}