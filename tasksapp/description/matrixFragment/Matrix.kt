package com.example.tasksapp.description.matrixFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.database.MatrixResultDao
import com.example.tasksapp.database.SubtaskDataDao
import com.example.tasksapp.database.TaskDatabase
import com.example.tasksapp.database.TodaySessionDao
import com.example.tasksapp.databinding.MatrixFragmentBinding
import com.example.tasksapp.description.matrixFragment.matrixBottomDialog.MatrixBottomDialog
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class Matrix : Fragment() {

    private val args: MatrixArgs by navArgs()
    private lateinit var factory: MatrixFactory
    @ExperimentalCoroutinesApi
    private val viewModel: MatrixViewModel by viewModels { factory }
    private lateinit var binding: MatrixFragmentBinding
    private lateinit var matrixResultDao: MatrixResultDao
    private lateinit var todaySessionDao: TodaySessionDao
    private lateinit var subtaskDataDao: SubtaskDataDao
    private var sessionTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = requireActivity().application
        val database = TaskDatabase.getInstance(app)
        matrixResultDao = database.matrixResultDao
        todaySessionDao = database.todaySessionDao
        subtaskDataDao = database.subtaskDataDao
        factory = MatrixFactory(subtaskDataDao, args.id, todaySessionDao, app)
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = MatrixFragmentBinding.inflate(inflater, container, false)

        val MATRIX_SESSION = preferencesKey<Long>("MATRIX_SESSION${args.id}")

        val todaySession = viewModel.dataStore.data.catch { e ->
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }.map { it[MATRIX_SESSION] ?: 0L }

        binding.appBarMatrix.subtitle = args.taskName

        lifecycleScope.launch {
            viewModel.subtaskListMatrix.collectLatest { viewModel.setMatrixLists(it) }
        }

        binding.appBarMatrix.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launch {
            viewModel.impUrgList.collectLatest {
                binding.impUrgCount.text = it.size.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.impNotUrgList.collectLatest {
                binding.impNotUrgCount.text = it.size.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.notImpUrgList.collectLatest {
                binding.notImpUrgCount.text = it.size.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.notImpNotUrgList.collectLatest {
                binding.notImpNotUrgCount.text = it.size.toString()
            }
        }

        lifecycleScope.launch {
            todaySession.collectLatest {
                sessionTime = it
                viewModel.setSessionValue(it)
                if(it == 0L) {
                    binding.saveAndClearButton.visibility = View.GONE
                } else {
                    binding.saveAndClearButton.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.sessionValue.collectLatest {
                binding.todaySessionValue.text = setElapsedTime(it)
            }
        }

        binding.impUrgCard.setOnClickListener {
            showDialog(1)
        }

        binding.impNotUrgCard.setOnClickListener {
            showDialog(2)
        }

        binding.notImpUrgCard.setOnClickListener {
            showDialog(3)
        }

        binding.notImpNotUrgCard.setOnClickListener {
            showDialog(4)
        }

        binding.chartButton.setOnClickListener {
            findNavController().navigate(MatrixDirections.actionMatrixToChartStatisticsFragment(args.id))
        }

        binding.saveAndClearButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.saveAndClearTodaySession(sessionTime)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.seconMainBackground, resources)
    }

    @ExperimentalCoroutinesApi
    private fun showDialog(idMatrix: Int) {
        val dialog = MatrixBottomDialog(
            args.id, args.taskName,
            idMatrix, viewModel,
            requireActivity().application,
            matrixResultDao, subtaskDataDao
        )
        if (childFragmentManager.findFragmentByTag(MatrixBottomDialog.TAG) == null) {
            dialog.show(childFragmentManager, MatrixBottomDialog.TAG)
        }
    }
}