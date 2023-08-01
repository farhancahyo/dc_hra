package com.shiro.formhrddover.ui.mainmenu

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.shiro.formhrddover.R
import com.shiro.formhrddover.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Setting HR Departement"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

        val spDataAPI = getSharedPreferences("DATAAPIHRD", MODE_PRIVATE)
        val api = spDataAPI.getString("APIGLOBAL", "http://115.85.65.42:8000")
//        val darkmode = spDataAPI.getBoolean("DARKMODE", false)

        binding.edtFormApiGlobal.setText(api)

//        if (darkmode) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            binding.swDarkMode.isChecked = true
//        }

//        binding.swDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                binding.swDarkMode.isChecked = true
//                val editor: SharedPreferences.Editor = spDataAPI.edit()
//                editor.putBoolean("DARKMODE", true)
//                editor.apply()
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                binding.swDarkMode.isChecked = false
//                val editor: SharedPreferences.Editor = spDataAPI.edit()
//                editor.putBoolean("DARKMODE", false)
//                editor.apply()
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_detail_plant_patrol, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.check -> {
                val spDataAPI = getSharedPreferences("DATAAPIHRD", MODE_PRIVATE)
                with(spDataAPI.edit()) {
                    putString("APIGLOBAL", binding.edtFormApiGlobal.text.toString())
                    commit()
                }
                Toast.makeText(this, "Data telah tersimpan", Toast.LENGTH_SHORT).show()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}