package com.shiro.formhrddover.database.repository

import android.app.Application
import com.shiro.formhrddover.database.HRRoomDatabase
import com.shiro.formhrddover.database.dao.UserDao
import com.shiro.formhrddover.database.entity.user.DDIAuthPagesEntity
import com.shiro.formhrddover.database.entity.user.DDIAuthUserEntity
import com.shiro.formhrddover.database.entity.user.DDIRefMenuAdminEntity
import com.shiro.formhrddover.database.entity.user.relation.MainMenuItem

class UserRepository (application: Application) {

    private val mUserDao: UserDao

    init {
        val db = HRRoomDatabase.getDatabase(application)
        mUserDao = db.userDao()
    }

    //DDIAuthUser
    suspend fun clearDDIAuthUser() = mUserDao.clearDDIAuthUser()
    suspend fun insertDDIAuthUser(dataResponse: List<DDIAuthUserEntity>) =
            mUserDao.insertDDIAuthUser(dataResponse)

    suspend fun getDDIAuthUserLogin(username: String, userpass: String): List<DDIAuthUserEntity> =
            mUserDao.getDDIAuthUserLogin(username, userpass)

    //DDIAuthPages
    suspend fun clearDDIAuthPages() = mUserDao.clearDDIAuthPages()
    suspend fun insertDDIAuthPages(dataResponse: List<DDIAuthPagesEntity>) =
            mUserDao.insertDDIAuthPages(dataResponse)
    suspend fun getMainMenuParents(idAuthUser : String) : List<MainMenuItem> {
        return mUserDao.getMainMenuParents(idAuthUser)
    }
    suspend fun getMainMenuChild(idAuthUser : String, idParent : String) : List<MainMenuItem> {
        return mUserDao.getMainMenuChild(idAuthUser, idParent)
    }

    //DDIRefMenuAdmin
    suspend fun clearDDIRefMenuAdmin() = mUserDao.clearDDIRefMenuAdmin()
    suspend fun insertDDIRefMenuAdmin(dataResponse: List<DDIRefMenuAdminEntity>) =
            mUserDao.insertDDIRefMenuAdmin(dataResponse)
}