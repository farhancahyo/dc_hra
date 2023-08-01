package com.shiro.formhrddover.database.entity.orientation

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "t_orientation")
@Parcelize
data class TOrientationEntity(
        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "transactionno")
        var transactionno: String,

        @ColumnInfo(name = "employeename")
        var employeename: String,

        @ColumnInfo(name = "employeeno")
        var employeeno: Int,

        @ColumnInfo(name = "departementid")
        var departementid: Int,

        @ColumnInfo(name = "titlename")
        var titlename: String,

        @ColumnInfo(name = "starteddate")
        var starteddate: Date,

        @ColumnInfo(name = "localsignfilename")
        var localsignfilename: String,

        @ColumnInfo(name = "serversignfilename")
        var serversignfilename: String,

        @ColumnInfo(name = "status")
        var status: Int,

        @ColumnInfo(name = "iscancel")
        var iscancel: Int,

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
