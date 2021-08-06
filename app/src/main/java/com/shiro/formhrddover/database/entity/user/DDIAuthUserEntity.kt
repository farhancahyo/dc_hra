package com.shiro.formhrddover.database.entity.user

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "ddi_auth_user")
@Parcelize
data class DDIAuthUserEntity(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id_auth_user")
    val id_auth_user: String,
    @ColumnInfo(name = "id_auth_user_grup")
    val id_auth_user_grup: String,
    @ColumnInfo(name = "netsuite_id_employee")
    val netsuite_id_employee: Int,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "userpass")
    val userpass: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "pass")
    val pass: String
) : Parcelable {
    override fun toString(): String {
        return "$netsuite_id_employee-$name"
    }
}
