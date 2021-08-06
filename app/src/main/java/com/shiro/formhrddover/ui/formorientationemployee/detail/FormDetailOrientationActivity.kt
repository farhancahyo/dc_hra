package com.shiro.formhrddover.ui.formorientationemployee.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.formhrddover.databinding.ActivityFormDetailOrientationBinding

class FormDetailOrientationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormDetailOrientationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormDetailOrientationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}