package com.example.tasksapp.dailytodo.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.ScheduleInfoAdapter
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.database.DailyTodoTask
import com.example.tasksapp.databinding.ScheduleArchiveInfoBinding
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
class ScheduleArchiveInfo: Fragment() {

    private val args: ScheduleArchiveInfoArgs by navArgs()
    private val viewModel: DailyTasksViewModel by activityViewModels()
    private lateinit var binding: ScheduleArchiveInfoBinding
    private lateinit var todoList: Flow<PagingData<DailyTodoTask>?>
    private lateinit var emptyListCheck: Flow<List<DailyTodoTask>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ScheduleArchiveInfoBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                todoList = Pager(
                    PagingConfig(
                        pageSize = 50
                    )
                ) {
                    viewModel.database.getAllTodoTasksByParentID(args.id)
                }.flow.cachedIn(lifecycleScope)
                emptyListCheck = viewModel.database.getEmptyListCheckByParentID(args.id)
            }
        }

        val adapter = ScheduleInfoAdapter()
        val recyclerView = binding.scheduleInfoRecyclerView

        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launchWhenCreated {
            emptyListCheck.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyArchiveInfoText.text = getString(R.string.you_have_no_archived_tasks_for_this_day)
                } else {
                    binding.emptyArchiveInfoText.text = ""
                }
            }
        }

        lifecycleScope.launch {
            todoList.collectLatest {
                it?.let {
                    adapter.submitData(it)
                }
            }
        }

        binding.closeScheduleInfo.setOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launch {

            viewModel.todoScheduleItem.collectLatest { scheduleWithTodo ->
                val list = scheduleWithTodo.todoList
                val listSize = when(list.size) {
                    0 -> 1
                    else -> list.size
                }
                val listSum = list.map { it.todoFinishTime }.sum()
                val averageList = listSum.div(listSize)
                binding.scheduleInfoTitle.text = scheduleWithTodo.schedule.todoScheduleId
                binding.averageTimeValue.text = setElapsedTime(averageList)
                binding.totalTimeValue.text = setElapsedTime(listSum)
            }
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