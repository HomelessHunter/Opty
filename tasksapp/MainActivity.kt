package com.example.tasksapp

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)

        if(!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }
        setContentView(R.layout.activity_main)
    }


   fun setStatusBarColor(color: Int, resources: Resources) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ResourcesCompat.getColor(resources, color, null)
   }

    fun setNavigationBarColor(color: Int, resources: Resources) {
        window.navigationBarColor = ResourcesCompat.getColor(resources, color, null)
    }
}
