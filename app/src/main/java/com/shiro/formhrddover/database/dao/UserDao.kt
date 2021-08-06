package com.shiro.formhrddover.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shiro.formhrddover.database.entity.user.DDIAuthPagesEntity
import com.shiro.formhrddover.database.entity.user.DDIAuthUserEntity
import com.shiro.formhrddover.database.entity.user.DDIRefMenuAdminEntity
import com.shiro.formhrddover.database.entity.user.relation.MainMenuItem

@Dao
interface UserDao {
    // DDIAuthUser
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDDIAuthUser(equipment: List<DDIAuthUserEntity>)

    @Query("DELETE FROM ddi_auth_user")
    suspend fun clearDDIAuthUser()

    @Query("SELECT * FROM ddi_auth_user WHERE username = :username AND userpass = :userpass")
    suspend fun getDDIAuthUserLogin(username: String, userpass: String): List<DDIAuthUserEntity>

    // DDIAuthPages
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDDIAuthPages(equipment: List<DDIAuthPagesEntity>)

    @Query("DELETE FROM ddi_auth_pages")
    suspend fun clearDDIAuthPages()

    @Query("SELECT a.id_auth_pages AS idauthpages, a.id_auth_user AS idauthuser, b.id_parents_menu_admin AS idparentsmenuadmin, b.id_ref_menu_admin AS idrefmenuadmin, b.menu AS menu, b.androidpath AS androidpath, b.urut AS urut, b.status AS status FROM ddi_auth_pages a, ddi_ref_menu_admin b WHERE a.id_ref_menu_admin = b.id_ref_menu_admin AND a.netsuite_id_employee = :idAuthUser AND b.id_parents_menu_admin = 1")
    suspend fun getMainMenuParents(idAuthUser : String): List<MainMenuItem>

    @Query("SELECT a.id_auth_pages AS idauthpages, a.id_auth_user AS idauthuser, b.id_parents_menu_admin AS idparentsmenuadmin, b.id_ref_menu_admin AS idrefmenuadmin, b.menu AS menu, b.androidpath AS androidpath, b.urut AS urut, b.status AS status FROM ddi_auth_pages a, ddi_ref_menu_admin b WHERE a.id_ref_menu_admin = b.id_ref_menu_admin AND a.netsuite_id_employee = :idAuthUser AND b.id_parents_menu_admin = :idParentsMenu")
    suspend fun getMainMenuChild(idAuthUser : String, idParentsMenu : String): List<MainMenuItem>

    // DDIRefmenuadmin
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDDIRefMenuAdmin(equipment: List<DDIRefMenuAdminEntity>)

    @Query("DELETE FROM ddi_ref_menu_admin")
    suspend fun clearDDIRefMenuAdmin()
}