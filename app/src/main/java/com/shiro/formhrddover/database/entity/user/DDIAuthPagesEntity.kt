package com.shiro.formhrddover.database.entity.user

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "ddi_auth_pages")
@Parcelize
data class DDIAuthPagesEntity(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id_auth_pages")
    val id_auth_pages: String,
    @ColumnInfo(name = "id_auth_user_grup")
    val id_auth_user_grup: String,
    @ColumnInfo(name = "id_auth_user")
    val id_auth_user: String,
    @ColumnInfo(name = "id_ref_menu_admin")
    val id_ref_menu_admin: String,
    @ColumnInfo(name = "netsuite_id_employee")
    val netsuiteidemployee: String
) : Parcelable
