package com.example.tasksapp.timer

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.databinding.TimerFragmentBinding
import com.example.tasksapp.receiver.PauseTimerReceiver
import com.example.tasksapp.service.OPEN_TIMER
import com.example.tasksapp.service.TimerService
import com.example.tasksapp.utilities.setElapsedTime
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



@ExperimentalCoroutinesApi
class Timer : Fragment() {

    private lateinit var binding: TimerFragmentBinding
    private lateinit var factory: TimerFactory
    private val viewModel: TimerViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = requireActivity().application
        factory = TimerFactory(application)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimerFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

       val s = object : LifecycleObserver {
           @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun pauseEvents() {
                lifecycleScope.launch {
                    if (viewModel.mainCountDownTimer != null) {
                        viewModel.mainCountDownTimer!!.cancel()
                    }

                    if (viewModel.loadedStartTimer.value  && viewModel.mainCountDownTimer != null) {
                        viewModel.service.action = "New_Timer_Action"
                        val service = viewModel.service
                        service.putExtra(TimerViewModel.TIMER_TRIGGER_TIME, viewModel.timerElapsedTime.value)
                        ContextCompat.startForegroundService(requireContext().applicationContext, viewModel.service)
                        viewModel.setServicePermit(true)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycle.addObserver(s)

        val playButton = binding.playButton
        val pauseButton = binding.pauseButton
        val stopButton = binding.stopButton

        val timeSlider = binding.mainSlider

        timeSlider.addOnChangeListener { _, value, _ ->
            viewModel.setSliderValue(value.toInt())
            binding.mainTimer.text = setElapsedTime(value.toLong() * 60_000)
        }

        binding.playButton.setOnClickListener {
            startScenario()
            viewModel.startMainTimer()
            lifecycleScope.launch {
                viewModel.dataStore.edit {
                    it[OPEN_TIMER] = MAIN_OPEN_TIMER
                }
            }
        }

        binding.pauseButton.setOnClickListener {
            pauseScenario()
            viewModel.pauseTimer()
            lifecycleScope.launch {
                viewModel.dataStore.edit {
                    it[OPEN_TIMER] = MAIN_OPEN_TIMER
                }
            }
        }

        binding.stopButton.setOnClickListener {
            stopScenario()
            viewModel.stopTimer()
            lifecycleScope.launch {
                viewModel.dataStore.edit {
                    it[OPEN_TIMER] = MAIN_OPEN_TIMER
                }
            }
        }

        lifecycleScope.launch {
            viewModel.mainTimerElapsedTime.collectLatest {
                binding.mainTimer.text = setElapsedTime(it)

            }
        }

        lifecycleScope.launch {
            viewModel.mainTimerPercentage.collectLatest {
                binding.mainProgressCircular.progress = it.toInt()
            }
        }

        val restTimeSlider = binding.restSlider
        restTimeSlider.addOnChangeListener { _, value, _ ->
            viewModel.setRestSlider(value.toInt())
            binding.restTimer.text = setElapsedTime(value.toLong() * 60_000)
        }

        val repeatSlider = binding.repeatSlider
        repeatSlider.addOnChangeListener { _, value, _ ->
            viewModel.setRepeatSlider(value.toInt())
            binding.repeatText.text = "${value.toInt()}"
        }

        lifecycleScope.launch {
            viewModel.restTimerElapsedTime.collectLatest {
                binding.restTimer.text = setElapsedTime(it)
            }
        }

        lifecycleScope.launch {
            viewModel.restTimerPercentage.collectLatest {
                binding.restProgressCircular.progress = it.toInt()
            }
        }

        lifecycleScope.launch {
            viewModel.timePercentage.collectLatest {
                binding.repeatProgressCircular.progress = it.toInt()
                binding.repeatText.text = "${it.toInt()}%"
            }
        }

        binding.timerToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launch {

            launch {
                viewModel.triggerTime.collectLatest {
                    viewModel.setLoadedTriggerTime(it)
                }
            }

            launch {
                viewModel.triggerPercentage.collectLatest {
                    viewModel.setLoadedTriggerPercentage(it)
                }
            }

            launch {
                viewModel.sum.collectLatest {
                    viewModel.setLoadedSum(it)
                }
            }

            launch {
                viewModel.mainTriggerTime.collectLatest {
                    viewModel.setLoadedMainTriggerTime(it)
                }
            }

            launch {
                viewModel.mainTriggerTimePause.collectLatest {
                    viewModel.setLoadedMainTriggerTimePause(it)
                }
            }

            launch {
                viewModel.mainTriggerPercentage.collectLatest {
                    viewModel.setLoadedMainTriggerPercentage(it)
                }
            }

            launch {
                viewModel.restTriggerTime.collectLatest {
                    viewModel.setLoadedRestTriggerTime(it)
                }
            }

            launch {
                viewModel.restTriggerTimePause.collectLatest {
                    viewModel.setLoadedRestTriggerTimePause(it)
                }
            }

            launch {
                viewModel.restTriggerPercentage.collectLatest {
                    viewModel.setLoadedRestTriggerPercentage(it)
                }
            }

            launch {
                viewModel.servicePermission.collectLatest {
                    viewModel.setLoadedServicePermission(it)
                }
            }

            launch {
                viewModel.startTimer.collectLatest {
                    viewModel.setLoadedStartTimer(it)
                }
            }

            launch {
                viewModel.openTimer.collectLatest { permit ->
                    when(permit) {
                        TimerService.SERVICE_OPEN_TIMER -> {
                            animateView()
                            pauseButton.setActivity(true)
                            playButton.setActivity(false)
                            stopButton.setActivity(true)
                        }
                        PauseTimerReceiver.PAUSE_OPEN_TIMER -> {
                            pauseButton.setActivity(false)
                            playButton.setActivity(true)
                            stopButton.setActivity(true)
                            animateView()
                        }
                        TimerService.SERVICE_COMPLETED -> {
                            viewModel.stopTimer()
                        }
                        else -> {
                            val startVisibility = viewModel.getPlayButtonVisibility()
                            val pauseVisibility = viewModel.getPauseButtonVisibility()
                            val stopVisibility = viewModel.getStopButtonVisibility()
                            val slidersVisibility = viewModel.getSlidersVisibility()
                            playButton.setActivity(startVisibility)
                            pauseButton.setActivity(pauseVisibility)
                            stopButton.setActivity(stopVisibility)


                            when(slidersVisibility) {
                                true -> {
                                    animatedView()
                                }
                                else -> {
                                    animateView()
                                }
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun startScenario() {
        lifecycleScope.launch {
            viewModel.savePlayButtonVisibility(false)
            viewModel.savePauseButtonVisibility(true)
            viewModel.saveStopButtonVisibility(true)
            viewModel.saveSlidersVisibility(false)
            binding.playButton.setActivity(false)
            binding.pauseButton.setActivity(true)
            binding.stopButton.setActivity(true)
            animateView()
        }
    }
    private fun pauseScenario() {
        lifecycleScope.launch {
            viewModel.savePauseButtonVisibility(false)
            viewModel.savePlayButtonVisibility(true)
            viewModel.saveStopButtonVisibility(true)
            viewModel.saveSlidersVisibility(false)
            binding.pauseButton.setActivity(false)
            binding.playButton.setActivity(true)
            binding.stopButton.setActivity(true)
            animateView()
        }
    }
    private fun stopScenario() {
        lifecycleScope.launch {
            viewModel.saveStopButtonVisibility(false)
            viewModel.savePlayButtonVisibility(true)
            viewModel.savePauseButtonVisibility(false)
            viewModel.saveSlidersVisibility(true)
            binding.stopButton.setActivity(false)
            binding.playButton.setActivity(true)
            binding.pauseButton.setActivity(false)
            animatedView()
        }
    }

    private fun FloatingActionButton.setActivity(value: Boolean) {
        lifecycleScope.launch {
        when(value) {
            true -> {
                isClickable = true
                alpha = 1.0f
            }
            false -> {
                isClickable = false
                alpha = 0.5f
            }
        }
        }
    }

    private fun animateView() {
        binding.mainSlider.visibility = View.GONE
        binding.restSlider.visibility = View.GONE
        binding.repeatSlider.visibility = View.GONE
        binding.MainSliderText.visibility = View.GONE
        binding.restSliderText.visibility = View.GONE
        binding.repeatSliderText.visibility = View.GONE
    }

    private fun animatedView() {
        binding.mainSlider.visibility = View.VISIBLE
        binding.restSlider.visibility = View.VISIBLE
        binding.repeatSlider.visibility = View.VISIBLE
        binding.MainSliderText.visibility = View.VISIBLE
        binding.restSliderText.visibility = View.VISIBLE
        binding.repeatSliderText.visibility = View.VISIBLE
    }

    override fun onResume() {
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)

            val notificationManager =
                ContextCompat.getSystemService(
                    requireContext(),
                    NotificationManager::class.java
                ) as NotificationManager

        viewModel.setTimerPercentage(viewModel.loadedTriggerPercentage.value)
        viewModel.setMainTimerValue(viewModel.loadedMainTriggerTimePause.value)
        viewModel.setMainTimerPercentage(viewModel.loadedMainTriggerPercentage.value)
        viewModel.setRestTimerValue(viewModel.loadedRestTriggerTimePause.value)
        viewModel.setRestTimerPercentage(viewModel.loadedRestTriggerPercentage.value)

        viewModel.createTestMainTimer()

        lifecycleScope.launch {
            if (viewModel.loadedServicePermission.value) {
                requireContext().stopService(viewModel.service)
                viewModel.dataStore.edit {
                    it[TimerViewModel.SERVICE_PERMISSION] = false
                }
            }
            delay(1000)
            notificationManager.cancelAll()
        }
        super.onResume()
    }
}