package com.example.tasksapp.maintask.addMainTask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.databinding.AddTaskDialogBinding
import com.example.tasksapp.maintask.MainTaskViewModel
import com.example.tasksapp.utilities.isTextCorrect
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AddMainTaskDialog(private val viewModel: MainTaskViewModel) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ADD_MAIN_TASK_DIALOG"
    }

    private lateinit var binding: AddTaskDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AddTaskDialogBinding.inflate(inflater, container, false)

        val editText = binding.editText
        binding.textFieldAdd.hint = getString(R.string.project)
        editText.requestFocus()


        binding.acceptButton.setOnClickListener {
            val textField = binding.textFieldAdd.editText!!.text
            if (!isTextCorrect(textField)) {
                binding.textFieldAdd.error = "Error"
            } else {
                binding.textFieldAdd.error = null
                viewModel.onGetTaskText(textField.toString())
                viewModel.insertTask()
                binding.textFieldAdd.editText!!.text = null

            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }
}