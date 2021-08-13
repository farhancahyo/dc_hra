package com.shiro.formhrddover.database.entity.hirechecklist

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "t_detail_new_hire_checklist_entity")
@Parcelize
data class TDetailNewHireCheckListEntity(
        @PrimaryKey(autoGenerate = true)
        @NonNull
        @ColumnInfo(name = "internalid")
        var internalid: Int,

        @ColumnInfo(name = "transactionno")
        var transactionno: String,

        @ColumnInfo(name = "categoryid")
        var categoryid: Int,

        @ColumnInfo(name = "mappingidcategory")
        var mappingidcategory: Int,

        @ColumnInfo(name = "valuecheck")
        var valuecheck: Int,

        @ColumnInfo(name = "netsuiteidoperator")
        var netsuiteidoperator: Int,

        @ColumnInfo(name = "operatorname")
        var operatorname: String,

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
