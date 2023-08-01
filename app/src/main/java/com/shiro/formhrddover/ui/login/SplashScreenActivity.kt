package com.shiro.formhrddover.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.shiro.formhrddover.databinding.ActivitySplashScreenBinding
import com.shiro.formhrddover.ui.mainmenu.MainActivity

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        val actionbar = supportActionBar
        actionbar?.hide()

        val spDataAPI = getSharedPreferences("DATAAPIINSPECTION", MODE_PRIVATE)
        val darkmode = spDataAPI.getBoolean("DARKMODE", false)

        if (darkmode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        loadDataSplash()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDataSplash() {
        Handler().postDelayed({
            // You can declare your desire activity here to open after finishing splash screen. Like MainActivity

//            val spDataLogin = getSharedPreferences("DATALOGIN", MODE_PRIVATE)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
//            val shift = spDataLogin.getInt("NUMSHIFT", 0)

//            if(getShiftNumber() == shift){
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            } else {
//                val spLogin = getSharedPreferences("DATALOGIN", MODE_PRIVATE)
//                with(spLogin.edit()) {
//                    putString("USERNAME", "")
//                    putInt("IDNETSUITE", 0)
//                    putString("EMAIL", "")
//                    putString("IDAUTHUSER", "")
//                    putInt("NUMSHIFT", 0)
//                    commit()
//                }
//                startActivity(Intent(this, LoginActivity::class.java))
//                finish()
//            }
        }, 3000)
    }
}