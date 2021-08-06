package com.shiro.formhrddover.ui.formhirechecklist.header

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.formhrddover.databinding.ActivityFormHeaderHireChecklistBinding

class FormHeaderHireChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormHeaderHireChecklistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormHeaderHireChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}