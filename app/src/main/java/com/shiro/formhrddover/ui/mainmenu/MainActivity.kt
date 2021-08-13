package com.shiro.formhrddover.ui.mainmenu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.shiro.formhrddover.R
import com.shiro.formhrddover.databinding.ActivityMainBinding
import com.shiro.formhrddover.helper.ViewModelFactory
import com.shiro.formhrddover.ui.formhirechecklist.FormHireChecklistViewModel
import com.shiro.formhrddover.ui.formhirechecklist.FormListHireChecklistActivity
import com.shiro.formhrddover.ui.login.LoginActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var viewModel: MainViewModel
    private lateinit var t: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = findViewById(R.id.activity_main)
        navView = findViewById(R.id.nav_view_main)

//        dl = (DrawerLayout)findViewById(R.id.activity_main);
        t = ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close)
        t.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(t)
        t.syncState()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val spDataLogin = getSharedPreferences("DATALOGIN", MODE_PRIVATE)
        val netsuite = spDataLogin.getInt("IDNETSUITE", 0)
        val username = spDataLogin.getString("USERNAME", "")
        val email = spDataLogin.getString("EMAIL", "")
        Toast.makeText(this, "$netsuite = $username", Toast.LENGTH_LONG).show()

        // Menselect header navigation drawer
        val headerView: View = navView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tv_name_login)
        val tvEmail = headerView.findViewById<TextView>(R.id.tv_email_login)
        tvName.text = username
        tvEmail.text = email

        GlobalScope.launch {
            val menu = navView.menu
            val menuParents = GlobalScope.async { netsuite?.let { viewModel.getMainMenuParents(it.toString()) } }.await()
            if (menuParents != null) {
                for(parent in menuParents){
                    runOnUiThread {
                        menu.add(parent.menu).setOnMenuItemClickListener {
                            try {
                                Class.forName(parent.androidpath)
                                val intent = Intent()
                                intent.setClassName(
                                        this@MainActivity,
                                        parent.androidpath
                                )
                                startActivity(intent)
                            } catch (e: ClassNotFoundException) {
                                Toast.makeText(this@MainActivity, "Class file under construction!", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }
//                        val menugrup = menu.addSubMenu(parent.menu)
//                        GlobalScope.launch {
//                            val menuChild = GlobalScope.async { netsuite?.let { viewModel.getMainMenuChild(
//                                it.toString(),
//                                parent.idrefmenuadmin
//                            ) } }.await()
//                            runOnUiThread{
//                                Log.d("HAZAR2", menuChild.toString())
//                                if (menuChild != null) {
//                                    for(child in menuChild){
////                                        if(child.idrefmenuadmin == "20210414-MNU002"){
////                                            continue
////                                        }
//                                        menugrup.add(child.menu).setOnMenuItemClickListener {
//                                            val intent = Intent()
//                                            intent.setClassName(
//                                                this@MainActivity,
//                                                child.androidpath
//                                            )
//                                            startActivity(intent)
//                                            true
//                                        }
//                                    }
//                                }
//                            }
//                        }
                    }
                }
            }
        }

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> {
                    val spLogin = getSharedPreferences("DATALOGIN", MODE_PRIVATE)
                    with(spLogin.edit()) {
                        putString("USERNAME", "")
                        putInt("IDNETSUITE", 0)
                        putString("EMAIL", "")
                        putString("IDAUTHUSER", "")
                        putInt("NUMSHIFT", 0)
                        commit()
                    }
                    finish()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                R.id.hirechecklist -> {
                    startActivity(Intent(this, FormListHireChecklistActivity::class.java))
                }
//                R.id.shift -> {
//                    startActivity(Intent(this, FormPlantShiftListActivity::class.java))
////                    Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show()
//                }
//                R.id.setting -> {
//                    startActivity(Intent(this, SettingsActivity::class.java))
//                }
//                R.id.sample -> {
//                    startActivity(Intent(this, ViewSampleAnalyst::class.java))
//                }
            }
            true
        }

        val permissionAll = 1
        val permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (!hasPermissions(this, *permissions)) {
            ActivityCompat.requestPermissions(this, permissions, permissionAll)
        }
    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // action bar touch
        if (t.onOptionsItemSelected(item)) {
            return true
        }
        return when (item.itemId) {
//            R.id.setting -> {
//                val intent = Intent()
//                intent.setClassName(
//                    this@MainActivity,
//                    "com.shiro.dover.formshedover.ui.setting.FormInspectionSettingActivity"
//                )
//                startActivity(intent)
//
//                startActivity(Intent(this, FormInspectionSettingActivity::class.java))
//                true
//            }
            else -> true
        }
    }
}