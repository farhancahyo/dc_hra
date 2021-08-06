package com.shiro.formhrddover.ui.formorientationemployee

import android.app.Application
import androidx.lifecycle.ViewModel
import com.shiro.formhrddover.database.repository.OrientationRepository

class FormOrientationViewModel (application: Application) : ViewModel() {
    private val mOrientationRepository = OrientationRepository(application)

}