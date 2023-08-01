package com.shiro.formhrddover.database.entity.orientation

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "m_employee")
@Parcelize
data class MEmployeeEntity (
        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "internalid")
        val internalid: Int,

        @ColumnInfo(name = "employeename")
        val employeename: String,

        @ColumnInfo(name = "status")
        var status: Int,

        @ColumnInfo(name = "createddate")
        var createddate: Date,

        @ColumnInfo(name = "createdby")
        var createdby: Int,

        @ColumnInfo(name = "createdname")
        var createdname: String
        ) : Parcelable {
        override fun toString(): String {
                return employeename
        }
        }