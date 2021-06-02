package com.example.tasksapp.description

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.emptyPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.*
import com.example.tasksapp.database.TaskDatabase
import com.example.tasksapp.databinding.TaskDescriptionFragmentBinding
import com.example.tasksapp.description.addTaskDialog.AddTaskDialog
import com.example.tasksapp.description.dataandtimepickers.DateDialog
import com.example.tasksapp.maintask.MainTaskViewModel
import com.example.tasksapp.settings.TIME_FORMAT
import com.example.tasksapp.utilities.Prefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class TaskDescription : Fragment() {

    private val args: TaskDescriptionArgs by navArgs()
    private lateinit var factory: TaskDescriptionFactory
    private val viewModel: TaskDescriptionViewModel by viewModels { factory }
    private val mainTaskViewModel by activityViewModels<MainTaskViewModel>()
    private lateinit var adapter: TaskDescriptionAdapter
    private lateinit var binding: TaskDescriptionFragmentBinding

    private lateinit var formatter: SimpleDateFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val app = requireActivity().application
        val database = TaskDatabase.getInstance(app)

        factory = TaskDescriptionFactory(args.taskId, database.taskDataDao, database.subtaskDataDao, app)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TaskDescriptionFragmentBinding.inflate(inflater, container, false)
        lifecycleScope.launchWhenResumed {
            binding.setReminder.visibility = View.VISIBLE
            binding.importantSwitch.visibility = View.VISIBLE
            binding.textFieldSetTag.visibility = View.VISIBLE
            binding.bottomNavigationDescription.visibility = View.VISIBLE
        }

        val dataStore = Prefs.getInstance(requireContext())

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

        lifecycleScope.launch {
            timeFormat.collectLatest {
                formatter = if (it) {
                    SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
                } else {
                    SimpleDateFormat("d MMM, H:mm", Locale.getDefault())
                }
            }
        }

        val addButton = binding.addSubtask
        val dateText = binding.setReminder
        val switch = binding.importantSwitch
        val deleteReminderButton = binding.resetReminderButton
        val recyclerView = binding.descriptionRecycleView


        val matrixArray = arrayOf(
            resources.getString(R.string.d_important_urgent),
            resources.getString(R.string.d_important_not_urgent),
            resources.getString(R.string.d_not_important_urgent),
            resources.getString(R.string.d_not_important_not_urgent))

        lifecycleScope.launch {
            binding.autoText.setOnItemClickListener { _, _, position, _ ->
                mainTaskViewModel.updateTaskBookmark(args.taskId, position)
                lifecycleScope.launch {
                    viewModel.colorChanger.collectLatest {
                        binding.appBarLayoutDescription.setDescBackground(it.bookMarkColor)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.mainTask.collectLatest {
                binding.projectTitle.text = it.taskTag
            }
        }

        lifecycleScope.launch {
            viewModel.mainTask.collectLatest {
                binding.autoText.setText(it.tag, false)
            }
        }

        lifecycleScope.launch {
            viewModel.mainDate.collectLatest {
                if (it < Calendar.getInstance(Locale.getDefault()).timeInMillis) {
                    dateText.text = resources.getString(R.string.dateText)
                    deleteReminderButton.visibility = View.GONE
                    viewModel.resetReminderDate()
                } else {
                    withContext(Dispatchers.Default) {
                        dateText.text = resources.getString(R.string.formatted_date, formatter.format(it))
                    }
                    deleteReminderButton.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.mainTask.collectLatest {
                switch.switched(it)
            }
        }

        viewModel.matrixSetter.observe(viewLifecycleOwner, {
            lifecycleScope.launch {
                it?.let { id ->
                    val subtask = viewModel.getSubtask(id)
                    MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                        .setTitle(getString(R.string.d_matrix_title))
                        .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }.setSingleChoiceItems(matrixArray, subtask.matrixValue - 1) { dialog, which ->
                            when(which) {
                                0 -> viewModel.setMatrixValue(id, 1)
                                1 -> viewModel.setMatrixValue(id, 2)
                                2 -> viewModel.setMatrixValue(id, 3)
                                3 -> viewModel.setMatrixValue(id, 4)
                            }
                            dialog.dismiss()
                        }.show()
                    viewModel.resetMatrixSetter()
                }
            }
        })

        val swipeEraser = object : SwipeEraser(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as TaskDescriptionAdapter.ViewHolder).subtask?.let {
                    viewModel.onDeleteDescTask(it)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeEraser)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addButton.setOnClickListener {
            val bottomSheetDialog = AddTaskDialog(viewModel)
            if (childFragmentManager.findFragmentByTag(AddTaskDialog.TAG) == null) {
                bottomSheetDialog.show(childFragmentManager, AddTaskDialog.TAG)
            }
        }

        deleteReminderButton.setOnClickListener {
            viewModel.cancelNotification(0L, Calendar.getInstance(Locale.getDefault()))
            viewModel.getDate()
        }

        binding.bottomNavigationDescription.setOnNavigationItemSelectedListener{
            when(it.itemId) {
                R.id.info_item -> {
                    infoView()
                    true
                }
                R.id.tasks_item -> {
                    listView()
                    true
                }
                R.id.matrix_item -> {
                    it.isCheckable = false
                    binding.bottomNavigationDescription.menu.findItem(R.id.info_item).isChecked = true
                    findNavController().navigate(TaskDescriptionDirections.actionTaskDescriptionToMatrix(args.taskId, viewModel.mainTask.value.taskTag))
                    true
                }
                else -> false
            }
        }

        dateText.setOnClickListener {
            val bottomSheetDateDialog = DateDialog(viewModel, dataStore)
            if (childFragmentManager.findFragmentByTag(DateDialog.TAG) == null) {
                bottomSheetDateDialog.show(childFragmentManager, DateDialog.TAG)
            }
        }


        switch.setOnCheckedChangeListener { _, isChecked ->
            when(isChecked) {
                true -> mainTaskViewModel.setImportance(args.taskId, 1)
                else -> mainTaskViewModel.setImportance(args.taskId, 0)
            }
        }


        createChannel(
            getString(R.string.subtask_notification_channel),
            getString(R.string.subtask_notification_channel_name)
        )

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(binding.topBarDescription)

        return binding.root
    }

    override fun onResume() {
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.seconMainBackground, resources)
        lifecycleScope.launch {
            val tagAdapter = ArrayAdapter(requireContext(), R.layout.list_item_main_tags, mainTaskViewModel.tagsTextList.value!!)
            (binding.textFieldSetTag.editText as? AutoCompleteTextView)?.setAdapter(tagAdapter)
            (binding.textFieldSetTag.editText as? AutoCompleteTextView)?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.popup_list, null))

            viewModel.colorChanger.collectLatest {
                binding.appBarLayoutDescription.setDescBackground(it.bookMarkColor)
            }
        }

        super.onResume()
    }

    private fun infoView() {
        binding.descriptionRecycleView.visibility = View.GONE
        binding.addSubtask.visibility = View.GONE
        binding.emptyTaskDescriptionBlob.visibility = View.GONE
        binding.emptyTaskDescriptionText.visibility = View.GONE
    }

    private fun listView() {

        binding.addSubtask.visibility = View.VISIBLE
        val recyclerView = binding.descriptionRecycleView
        recyclerView.visibility = View.VISIBLE

        adapter = TaskDescriptionAdapter(MatrixDescriptionListener {
            viewModel.setMatrixSetter(it)
        })

            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(activity)
            layoutManager.isSmoothScrollbarEnabled = true
            recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            viewModel.subtaskList.collectLatest {
                adapter.submitData(it)
            }
        }
        lifecycleScope.launch {
            viewModel.additionalList.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyTaskDescriptionBlob.visibility = View.VISIBLE
                    binding.emptyTaskDescriptionText.visibility = View.VISIBLE
                } else {
                    binding.emptyTaskDescriptionBlob.visibility = View.GONE
                    binding.emptyTaskDescriptionText.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.topbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
            }
        }
        return true
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
                description = getString(R.string.subtask_notification_channel_name)
            }
            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}

