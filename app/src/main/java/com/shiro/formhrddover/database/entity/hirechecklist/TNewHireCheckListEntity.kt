package com.shiro.formhrddover.database.entity.hirechecklist

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "t_new_hire_checklist_entity")
@Parcelize
data class TNewHireCheckListEntity(
        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "transactionno")
        var transactionno: String,

        @ColumnInfo(name = "employeename")
        var employeename: String,

        @ColumnInfo(name = "departementid")
        var departementid: Int,

        @ColumnInfo(name = "titlename")
        var titlename: String,

        @ColumnInfo(name = "jointdate")
        var jointdate: Date,

        @ColumnInfo(name = "employeeno")
        var employeeno: Int,

        @ColumnInfo(name = "status")
        var status: Int,

        @ColumnInfo(name = "createddate")
        var createddate: Date,

        @ColumnInfo(name = "createdby")
        var createdby: Int,

        @ColumnInfo(name = "createdname")
        var createdname: String,

        @ColumnInfo(name = "lastmodifieddate")
        var lastmodifieddate: Date,

        @ColumnInfo(name = "lastmodifiedby")
        var lastmodifiedby: Int,

        @ColumnInfo(name = "lastmodifiedname")
        var lastmodifiedname: String
) : Parcelable