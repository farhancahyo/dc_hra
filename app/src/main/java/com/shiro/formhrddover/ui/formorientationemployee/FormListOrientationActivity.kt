package com.shiro.formhrddover.ui.formorientationemployee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.formhrddover.databinding.ActivityFormListOrientationBinding

class FormListOrientationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormListOrientationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormListOrientationBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}