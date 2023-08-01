package com.shiro.formhrddover.database.entity.hirechecklist

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "m_departement")
@Parcelize
data class MDepartementEntity(
        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "internalid")
        val internalid: Int,

        @ColumnInfo(name = "netsuiteiddepartement")
        val netsuiteiddepartement: Int,

        @ColumnInfo(name = "departementname")
        val departementname: String,

        @ColumnInfo(name = "status")
        var status: Int,

        @ColumnInfo(name = "createddate")
        var createddate: Date

//        @ColumnInfo(name = "createdby")
//        var createdby: Int,
//
//        @ColumnInfo(name = "createdname")
//        var createdname: String,
//
//        @ColumnInfo(name = "lastmodifieddate")
//        var lastmodifieddate: Date,
//
//        @ColumnInfo(name = "lastmodifiedby")
//        var lastmodifiedby: Int,
//
//        @ColumnInfo(name = "lastmodifiedname")
//        var lastmodifiedname: String
) : Parcelable {
        override fun toString(): String {
                return "$internalid-$departementname"
        }
}