package com.shiro.formhrddover.database.repository

import android.app.Application
import com.shiro.formhrddover.database.HRRoomDatabase
import com.shiro.formhrddover.database.dao.HireCheckListDao
import com.shiro.formhrddover.database.entity.hirechecklist.*

class HireChecklistRepository (application: Application) {

    private val mHireCheckListDao: HireCheckListDao

    init {
        val db = HRRoomDatabase.getDatabase(application)
        mHireCheckListDao = db.hireChecklistDao()
    }

    //Category
    suspend fun clearMCategory() = mHireCheckListDao.clearMCategory()
    suspend fun insertMCategory(dataResponse: ArrayList<MCategoryEntity>) = mHireCheckListDao.insertMCategory(dataResponse)
    suspend fun getMCategory() = mHireCheckListDao.getMCategory()

    //Item
    suspend fun clearMItem() = mHireCheckListDao.clearMItem()
    suspend fun insertMItem(dataResponse: ArrayList<MItemEntity>) = mHireCheckListDao.insertMItem(dataResponse)
    suspend fun getMItem() = mHireCheckListDao.getMItem()

    //Departement
    suspend fun clearMDepartement() = mHireCheckListDao.clearMDepartement()
    suspend fun insertMDepartement(dataResponse: ArrayList<MDepartementEntity>) = mHireCheckListDao.insertMDepartement(dataResponse)
    suspend fun getMDepartement() = mHireCheckListDao.getMDepartement()

    //Category
    suspend fun clearMMappingCategory() = mHireCheckListDao.clearMMappingCategory()
    suspend fun insertMMappingCategory(dataResponse: ArrayList<MMappingCategoryEntity>) = mHireCheckListDao.insertMMappingCategory(dataResponse)
    suspend fun getMMappingCategory() = mHireCheckListDao.getMMappingCategory()

    // New Hire Checklist
    suspend fun clearTNewHireCheckList() = mHireCheckListDao.clearTNewHireChecklist()
    suspend fun deleteTNewHireCheckList(data : TNewHireCheckListEntity) = mHireCheckListDao.deleteTNewHireChecklist(data)
    suspend fun insertTNewHireCheckList(dataResponse: ArrayList<TNewHireCheckListEntity>) = mHireCheckListDao.insertTNewHireChecklist(dataResponse)
    suspend fun insertTNewHireCheckList(dataResponse: TNewHireCheckListEntity) = mHireCheckListDao.insertTNewHireChecklist(dataResponse)
    suspend fun updateTNewHireCheckList(dataResponse: TNewHireCheckListEntity) = mHireCheckListDao.updateTNewHireChecklist(dataResponse)
    suspend fun getTNewHireCheckList(sdate: Long, edate: Long, name : String, no : String) = mHireCheckListDao.getTNewHireChecklist(sdate, edate, name, no)
    suspend fun getTNewHireCheckList(idTrx : String) = mHireCheckListDao.getTNewHireChecklist(idTrx)
    suspend fun getTNewHireCheckList(status: Int) = mHireCheckListDao.getTNewHireChecklist(status)
    suspend fun getTNewHireCheckList() = mHireCheckListDao.getTNewHireChecklist()
    suspend fun getTNewHireCheckListOffline() = mHireCheckListDao.getTNewHireChecklistOffline()


    // Detail New Hire Checklist
    suspend fun deleteTDetailNewHireCheckList(data : TDetailNewHireCheckListEntity) = mHireCheckListDao.deleteTDetailNewHireChecklist(data)
    suspend fun clearTDetailNewHireCheckList()= mHireCheckListDao.clearTDetailNewHireCheckList()
    suspend fun insertTDetailNewHireCheckList(dataResponse: ArrayList<TDetailNewHireCheckListEntity>) = mHireCheckListDao.insertTDetailNewHireCheckList(dataResponse)
    suspend fun insertTDetailNewHireCheckList(dataResponse: TDetailNewHireCheckListEntity) = mHireCheckListDao.insertTDetailNewHireCheckList(dataResponse)
    suspend fun updateTDetailNewHireCheckList(dataResponse: TDetailNewHireCheckListEntity) = mHireCheckListDao.updateTDetailNewHireCheckList(dataResponse)
    suspend fun getTDetailNewHireCheckList(idTrx : String) = mHireCheckListDao.getTDetailNewHireCheckList(idTrx)
    suspend fun getTDetailNewHireCheckList(idTrx : String, categoryid : Int) = mHireCheckListDao.getTDetailNewHireCheckList(idTrx, categoryid)
    suspend fun getTDetailNewHireCheckList(status : Int) = mHireCheckListDao.getTDetailNewHireCheckList(status)

    // Relation
    suspend fun getItemUraianChecklist(categoryid: Int) = mHireCheckListDao.getItemUraianChecklist(categoryid)
}
