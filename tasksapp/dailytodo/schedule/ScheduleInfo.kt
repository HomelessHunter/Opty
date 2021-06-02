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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.ScheduleInfoAdapter
import com.example.tasksapp.adapter.SwipeEraser
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.dailytodo.addTodoDialog.AddTodoDialog
import com.example.tasksapp.database.DailyTodoTask
import com.example.tasksapp.databinding.ScheduleInfoFragmentBinding
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
class ScheduleInfo : Fragment() {

    private val args: ScheduleInfoArgs by navArgs()
    private val viewModel: DailyTasksViewModel by activityViewModels()
    private lateinit var binding: ScheduleInfoFragmentBinding
    private lateinit var todoList: Flow<PagingData<DailyTodoTask>?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ScheduleInfoFragmentBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                todoList = Pager(
                        PagingConfig(
                            pageSize = 50
                        )
                        ) {
                    viewModel.database.getAllTodoTasksByParentID(args.id)
                }.flow.cachedIn(lifecycleScope)
            }
        }

       val emptyListCheck: Flow<List<DailyTodoTask>> = viewModel.database.getEmptyListCheckByParentID(args.id)
        val adapter = ScheduleInfoAdapter()
        val recyclerView = binding.scheduleInfoRecyclerView

        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            emptyListCheck.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyScheduleInfoText.text = getString(R.string.you_have_no_scheduled_task_for_this_day)
                } else {
                    binding.emptyScheduleInfoText.text = ""
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

        val swipeEraser = object : SwipeEraser(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as ScheduleInfoAdapter.InfoTodoTaskViewHolder).todoDaily?.let {
                    viewModel.deleteTodo(it)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeEraser)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        binding.addScheduleTodo.setOnClickListener {
            val dialog = AddTodoDialog(viewModel, args.id)
            if (childFragmentManager.findFragmentByTag(AddTodoDialog.TAG) == null) {
                dialog.show(childFragmentManager, AddTodoDialog.TAG)
            }
        }

        binding.closeScheduleInfo.setOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launch {

            viewModel.todoScheduleItem.collectLatest { scheduleWithTodo ->
                val list = scheduleWithTodo.todoList.map { dailyTodoTask -> dailyTodoTask.todoFinishTime }.filter { it > 0L }
                val listSize = when(list.size) {
                    0 -> 1
                    else -> list.size
                }
                val listSum = list.sum()
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