package com.shiro.formhrddover.ui.formhirechecklist

import android.app.Application
import androidx.lifecycle.ViewModel
import com.shiro.formhrddover.database.repository.HireChecklistRepository

class FormHireChecklistViewModel(application: Application) : ViewModel() {
    private val mHireChecklistRepository = HireChecklistRepository(application)
}