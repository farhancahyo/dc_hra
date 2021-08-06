package com.shiro.formhrddover.ui.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.shiro.formhrddover.databinding.ActivityLoginBinding
import com.shiro.formhrddover.helper.ViewModelFactory
import com.shiro.formhrddover.ui.mainmenu.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    companion object{
        private var numShift = 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this.application)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        if (isOnline(this)) {
            viewModel.loadData(this)
        } else {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Alert Messages")
            builder.setMessage("Anda sedang offline dan menggunakan data dari local")
            builder.setPositiveButton("Okay") { dialog, which ->
            }
            builder.show()
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.edtEmail.text.toString()
            val userpass = encodeMd5(binding.edtPass.text.toString())
            if (username.isNotEmpty() && userpass.isNotEmpty()) {
                GlobalScope.launch {
                    val listlogin =
                        GlobalScope.async { viewModel.getDDIAuthUserLogin(username, userpass) }
                            .await()
                    if (listlogin.isNotEmpty()) {
                        val spDataLogin = getSharedPreferences("DATALOGIN", MODE_PRIVATE)
                        with(spDataLogin.edit()) {
                            putString("USERNAME", listlogin[0].name)
                            putInt("IDNETSUITE", listlogin[0].netsuite_id_employee)
                            putString("EMAIL", listlogin[0].username)
                            putString("IDAUTHUSER", listlogin[0].id_auth_user)
                            putInt("NUMSHIFT", getShiftNumber())
                            commit()
                        }
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        this@LoginActivity.runOnUiThread {
                            Toast.makeText(
                                this@LoginActivity,
                                "Username dan Password Salah",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.edtPass.setText("")
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Isi form terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encodeMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getShiftNumber() : Int{
        return if ("06:59" < getLocalTimeNow() && getLocalTimeNow() <= "14:59") {
            1
        } else if ("14:59" < getLocalTimeNow() && getLocalTimeNow() <= "22:59") {
            2
        } else {
            3
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocalTimeNow(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return current.format(formatter)
    }
}