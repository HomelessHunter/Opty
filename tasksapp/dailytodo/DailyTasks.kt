package com.example.tasksapp.dailytodo

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.*
import com.example.tasksapp.dailytodo.schedule.Routine
import com.example.tasksapp.database.AverageTodoTime
import com.example.tasksapp.database.TaskDatabase
import com.example.tasksapp.databinding.DailyTasksFragmentBinding
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalCoroutinesApi
class DailyTasks : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val app = requireActivity().application
        val database = TaskDatabase.getInstance(app)
        factory = DailyTaskFactory(app, database.todoTaskDao, database.todoScheduleDao, database.averageTodoTimeDao)
    }

    private lateinit var factory: DailyTaskFactory
    private val viewModel: DailyTasksViewModel by activityViewModels { factory }
    private lateinit var binding: DailyTasksFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DailyTasksFragmentBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val addButton = binding.addTodo

        val adapterRoutineHeader = RoutineHeaderAdapter(routineHeader = RoutineHeader(title = resources.getString(R.string.routine)))

        val adapterScheduleHeader = ScheduleHeaderAdapter(scheduleHeader = ScheduleHeader(title = resources.getString(R.string.schedule)))

        val adapterSchedule = TodoTaskAdapter(TodoCompletionListener {
            viewModel.completeTodo(it)
        })

        lifecycleScope.launch {
            if (viewModel.database.getAllTodo().isNullOrEmpty()) {
                binding.emptyTodoBlob.visibility = View.VISIBLE
                binding.emptyTodoText.visibility = View.VISIBLE
            } else {
                binding.emptyTodoBlob.visibility = View.GONE
                binding.emptyTodoText.visibility = View.GONE
            }
        }

        val adapterRoutine = TodoRoutineTaskAdapter(TodoRoutineCompletionListener {
            viewModel.completeTodo(it)
        })

        lifecycleScope.launch {
            viewModel.routineList.collectLatest {
                adapterRoutine.submitData(it)
            }
        }

        lifecycleScope.launch {
            viewModel.scheduleList.collectLatest {
                adapterSchedule.submitData(it)
            }
        }

        val adapter = ConcatAdapter(adapterRoutineHeader, adapterRoutine, adapterScheduleHeader, adapterSchedule)

        val todoRecyclerView = binding.todoRecycleView
        todoRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        todoRecyclerView.layoutManager = layoutManager

        viewModel.isTodoCompleted.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.onUpdateTodo(it)
                viewModel.resetCompleteTodo()
            }
        })

        addButton.setOnClickListener {
            findNavController().navigate(DailyTasksDirections.actionDailyTasksToTodoSchedule())
        }

        binding.startFAB.setOnClickListener {
            viewModel.startTimer()
        }

        lifecycleScope.launch {
            viewModel.elapsedTime.collectLatest {
                if (it > 0) {
                    delay(100)
                    binding.startFAB.hide()
                    delay(300)
                    binding.stopFAB.show()
                } else {
                    delay(100)
                    binding.stopFAB.hide()
                    delay(300)
                    binding.startFAB.show()
                }
                binding.stopFAB.text = setElapsedTime(it)
            }
        }

        binding.stopFAB.setOnClickListener {
            viewModel.resetTimer()
        }


        binding.todayDateText.text = viewModel.todayScheduleID

        lifecycleScope.launchWhenResumed {
            delay(400)
            binding.todoRecycleView.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            viewModel.routineSize.collectLatest { list ->
                val finishedList = list.map { dailyTodoTask -> dailyTodoTask.todoFinishTime }.filter { it > 0L }
                val listSize = when(finishedList.size) {
                    0 -> 1
                    else -> finishedList.size
                }
                val avTime = finishedList.sum().div(listSize)

                val todayDate = Calendar.getInstance(Locale.getDefault()).timeInMillis
                val id = Routine.ROUTINE_ID + viewModel.formatter.format(todayDate)
                val avTodoTime = AverageTodoTime(avTimeId = id, time = avTime, finishDate = todayDate)
                viewModel.averageTodoTimeDao.insertAverageTime(avTodoTime)
            }
        }

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(binding.topBar)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.topbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            android.R.id.home -> {
                this.findNavController().navigateUp()
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)
    }
}

