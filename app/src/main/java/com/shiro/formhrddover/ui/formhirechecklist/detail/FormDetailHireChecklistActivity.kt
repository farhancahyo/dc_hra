package com.shiro.formhrddover.ui.formhirechecklist.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.formhrddover.databinding.ActivityFormDetailHireChecklistBinding

class FormDetailHireChecklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormDetailHireChecklistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormDetailHireChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}