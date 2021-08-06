package com.shiro.formhrddover.database.entity.user

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "ddi_ref_menu_admin")
@Parcelize
data class DDIRefMenuAdminEntity(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id_ref_menu_admin")
    val id_ref_menu_admin: String,
    @ColumnInfo(name = "id_parents_menu_admin")
    val id_parents_menu_admin: String,
    @ColumnInfo(name = "menu")
    val menu: String,
    @ColumnInfo(name = "file")
    val file: String,
    @ColumnInfo(name = "androidpath")
    val androidpath: String,
    @ColumnInfo(name = "urut")
    val urut: Int,
    @ColumnInfo(name = "status")
    val status: Int,
    @ColumnInfo(name = "icon")
    val icon: String
) : Parcelable
