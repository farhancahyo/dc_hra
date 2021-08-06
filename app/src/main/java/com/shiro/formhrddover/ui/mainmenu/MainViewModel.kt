package com.shiro.formhrddover.ui.mainmenu

import android.app.Application
import androidx.lifecycle.ViewModel
import com.shiro.formhrddover.database.entity.user.relation.MainMenuItem
import com.shiro.formhrddover.database.repository.UserRepository

class MainViewModel(application: Application) : ViewModel() {
    private val mUserRepository = UserRepository(application)

    suspend fun getMainMenuParents(idAuthUser : String) : List<MainMenuItem> {
        return mUserRepository.getMainMenuParents(idAuthUser)
    }

    suspend fun getMainMenuChild(idAuthUser : String, idParent : String) : List<MainMenuItem> {
        return mUserRepository.getMainMenuChild(idAuthUser, idParent)
    }
}