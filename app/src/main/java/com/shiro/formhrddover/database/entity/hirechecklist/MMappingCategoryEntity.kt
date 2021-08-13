package com.shiro.formhrddover.database.entity.hirechecklist

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "m_mapping_category")
@Parcelize
data class MMappingCategoryEntity(
        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "internalid")
        val internalid: Int,

        @ColumnInfo(name = "categoryid")
        val categoryid: Int,

        @ColumnInfo(name = "itemid")
        val itemid: Int,

        @ColumnInfo(name = "status")
        var status: Int,
) : Parcelable