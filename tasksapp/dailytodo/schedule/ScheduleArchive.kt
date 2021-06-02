package com.example.tasksapp.dailytodo.schedule

import android.os.Bundle
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
import com.example.tasksapp.adapter.ArchiveAdapter
import com.example.tasksapp.adapter.ArchiveListener
import com.example.tasksapp.adapter.DeleteArchiveListener
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.databinding.ArchiveFragmentBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ScheduleArchive : Fragment() {

    private val viewModel: DailyTasksViewModel by activityViewModels()
    private lateinit var binding: ArchiveFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ArchiveFragmentBinding.inflate(inflater, container, false)


        val adapter = ArchiveAdapter(
            ArchiveListener {
                viewModel.setInfo(it)
            },
            DeleteArchiveListener {
                viewModel.setDeleteValue(it)
            }
        )
        val recyclerView = binding.archiveRecyclerView
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            viewModel.emptyArchiveCheck.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyArchiveBlob.visibility = View.VISIBLE
                    binding.emptyArchiveText.visibility = View.VISIBLE
                } else {
                    binding.emptyArchiveText.visibility = View.GONE
                    binding.emptyArchiveBlob.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.archive.collectLatest {
                adapter.submitData(it)
            }
        }

        viewModel.info.observe(viewLifecycleOwner, {
            it?.let {
                findNavController().navigate(ScheduleArchiveDirections.actionScheduleArchiveToScheduleArchiveInfo(it))
                viewModel.setTodoScheduleItem(it)
                viewModel.resetInfo()
            }
        })

        viewModel.deleteValue.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.deleteSchedule(it)
                viewModel.deleteTodoTasks(it.todoScheduleId)
                viewModel.resetDeleteValue()
            }
        })

        binding.closeArchive.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)
    }
}