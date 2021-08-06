package com.shiro.formhrddover.ui.formorientationemployee.header

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.formhrddover.databinding.ActivityFormHeaderOrientationBinding

class FormHeaderOrientationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormHeaderOrientationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormHeaderOrientationBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}