package com.shiro.formhrddover.database.repository

import android.app.Application
import com.shiro.formhrddover.database.HRRoomDatabase
import com.shiro.formhrddover.database.dao.OrientationDao
import com.shiro.formhrddover.database.entity.orientation.MEmployeeEntity
import com.shiro.formhrddover.database.entity.orientation.MUraianEntity
import com.shiro.formhrddover.database.entity.orientation.TDetailOrientationEntity
import com.shiro.formhrddover.database.entity.orientation.TOrientationEntity

class OrientationRepository (application: Application) {

    private val mOrientationDao: OrientationDao

    init {
        val db = HRRoomDatabase.getDatabase(application)
        mOrientationDao = db.orientationDao()
    }

    //Master Uraian
    suspend fun clearMUraian() = mOrientationDao.clearMUraian()
    suspend fun insertMUraian(dataResponse: ArrayList<MUraianEntity>) = mOrientationDao.insertMUraian(dataResponse)
    suspend fun getMUraian() = mOrientationDao.getMUraian()

    //Master Employee
    suspend fun clearMEmployee() = mOrientationDao.clearMEmployee()
    suspend fun insertMEmployee(dataResponse: ArrayList<MEmployeeEntity>) = mOrientationDao.insertMEmployee(dataResponse)
    suspend fun getMEmployee(keyword : String) = mOrientationDao.getMEmployee(keyword)

    // Trx Orientation
    suspend fun clearTOrientation() = mOrientationDao.clearTOrientation()
    suspend fun deleteTOrientation(data : TOrientationEntity) = mOrientationDao.deleteTOrientation(data)
    suspend fun insertTOrientation(dataResponse: ArrayList<TOrientationEntity>) = mOrientationDao.insertTOrientation(dataResponse)
    suspend fun insertTOrientation(dataResponse: TOrientationEntity) = mOrientationDao.insertTOrientation(dataResponse)
    suspend fun updateTOrientation(dataResponse: TOrientationEntity) = mOrientationDao.updateTOrientation(dataResponse)
    suspend fun getTOrientation(sdate: Long, edate: Long, name : String, no : String) = mOrientationDao.getTOrientation(sdate, edate, name, no)
    suspend fun getTOrientation(idTrx : String) = mOrientationDao.getTOrientation(idTrx)
    suspend fun getTOrientation(status: Int) = mOrientationDao.getTOrientation(status)
    suspend fun getTOrientationEmployee(employeeno: Int) = mOrientationDao.getTOrientationEmployee(employeeno)
    suspend fun getTOrientation() = mOrientationDao.getTOrientation()
    suspend fun getTOrientationOffline() = mOrientationDao.getTOrientationOffline()

    // Trx Detail Orientation
    suspend fun deleteTDetailOrientation(data : TDetailOrientationEntity) = mOrientationDao.deleteTDetailOrientation(data)
    suspend fun clearTDetailOrientation()= mOrientationDao.clearTDetailOrientation()
    suspend fun resetTDetailOrientation()= mOrientationDao.resetTDetailOrientation()
    suspend fun insertTDetailOrientation(dataResponse: ArrayList<TDetailOrientationEntity>) = mOrientationDao.insertTDetailOrientation(dataResponse)
    suspend fun insertTDetailOrientation(dataResponse: TDetailOrientationEntity) = mOrientationDao.insertTDetailOrientation(dataResponse)
    suspend fun updateTDetailOrientation(dataResponse: TDetailOrientationEntity) = mOrientationDao.updateTDetailOrientation(dataResponse)
    suspend fun getTDetailOrientation(idTrx : String) = mOrientationDao.getTDetailOrientation(idTrx)
    suspend fun getTDetailOrientation(idTrx : String, uraianid : Int) = mOrientationDao.getTDetailOrientation(idTrx, uraianid)
    suspend fun getTDetailOrientation(status : Int) = mOrientationDao.getTDetailOrientation(status)
}