package com.shiro.formhrddover.database.entity.user.relation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainMenuItem(
    val idauthpages: String,
    val idauthuser: String,
    val idparentsmenuadmin: String,
    val idrefmenuadmin: String,
    val menu: String,
    val androidpath: String,
    val urut: Int,
    val status: Int
) : Parcelable
