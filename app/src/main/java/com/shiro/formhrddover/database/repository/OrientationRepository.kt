package com.shiro.formhrddover.database.repository

import android.app.Application
import com.shiro.formhrddover.database.HRRoomDatabase
import com.shiro.formhrddover.database.dao.OrientationDao

class OrientationRepository (application: Application) {

    private val mOrientationDao: OrientationDao

    init {
        val db = HRRoomDatabase.getDatabase(application)
        mOrientationDao = db.orientationDao()
    }
}