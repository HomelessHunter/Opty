package com.example.tasksapp.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.databinding.SettingsFragmentBinding
import com.example.tasksapp.utilities.Prefs
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


val TIME_FORMAT = preferencesKey<Boolean>("time_format")
class SettingsFragment : Fragment(){

    private lateinit var binding: SettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)

        val pkgName = requireActivity().application.packageName
        binding.appVersion.text = requireActivity().application.packageManager.getPackageInfo(pkgName, 0).versionName

        val dataStore = Prefs.getInstance(requireContext())

        binding.timeFormatSwitch.setOnCheckedChangeListener { _, b ->
            lifecycleScope.launch {
                dataStore.edit { preferences ->
                    preferences[TIME_FORMAT] = b
                }
            }
        }

        val isSwitched = dataStore.data
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
            isSwitched.collectLatest {
                binding.timeFormatSwitch.isChecked = it
            }
        }
        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.privacyPolicy.setOnClickListener {
            openWebPage("https://sites.google.com/view/optydev/privacy-policy?authuser=1")
        }
        binding.termsOfUse.setOnClickListener {
            openWebPage("https://sites.google.com/view/optydev/eula?authuser=1")
        }
        binding.troubleshooting.setOnClickListener {
            val dialog = TroubleShootingDialog()
            if (childFragmentManager.findFragmentByTag(TroubleShootingDialog.TAG) == null) {
                dialog.show(childFragmentManager, TroubleShootingDialog.TAG)
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

    private fun openWebPage(url: String) {
        val webPage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        if (activity?.packageManager?.let { intent.resolveActivity(it) } != null) {
            startActivity(intent)
        }
    }

}