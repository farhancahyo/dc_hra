package com.shiro.formhrddover.database.repository

import android.app.Application
import com.shiro.formhrddover.database.HRRoomDatabase
import com.shiro.formhrddover.database.dao.HireCheckListDao

class HireChecklistRepository (application: Application) {

    private val mHireCheckListDao: HireCheckListDao

    init {
        val db = HRRoomDatabase.getDatabase(application)
        mHireCheckListDao = db.hireChecklistDao()
    }

}
