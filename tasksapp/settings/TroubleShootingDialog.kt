package com.example.tasksapp.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.databinding.TroubleshootingBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TroubleShootingDialog : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "TroubleShootingDialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = TroubleshootingBottomSheetBinding.inflate(inflater, container, false)

        binding.contactMeButton.setOnClickListener {
            sendEmail()
        }

        return binding.root
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("optyappdev@gmail.com"))
        }
        if (activity?.packageManager?.let { intent.resolveActivity(it) } != null) {
            startActivity(intent)
        }
    }
}