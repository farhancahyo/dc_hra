package com.shiro.formhrddover.ui.formhirechecklist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.formhrddover.databinding.ActivityFormListHireChecklistBinding

class FormListHireChecklistActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFormListHireChecklistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormListHireChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}