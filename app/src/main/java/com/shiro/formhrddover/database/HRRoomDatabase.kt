package com.shiro.formhrddover.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shiro.formhrddover.database.dao.HireCheckListDao
import com.shiro.formhrddover.database.dao.OrientationDao
import com.shiro.formhrddover.database.dao.UserDao
import com.shiro.formhrddover.database.entity.hirechecklist.*
import com.shiro.formhrddover.database.entity.orientation.MEmployeeEntity
import com.shiro.formhrddover.database.entity.orientation.MUraianEntity
import com.shiro.formhrddover.database.entity.orientation.TDetailOrientationEntity
import com.shiro.formhrddover.database.entity.orientation.TOrientationEntity
import com.shiro.formhrddover.database.entity.user.DDIAuthPagesEntity
import com.shiro.formhrddover.database.entity.user.DDIAuthUserEntity
import com.shiro.formhrddover.database.entity.user.DDIRefMenuAdminEntity

@Database(
        entities = [DDIRefMenuAdminEntity::class, DDIAuthUserEntity::class, DDIAuthPagesEntity::class, MCategoryEntity::class, MMappingCategoryEntity::class, MItemEntity::class, MDepartementEntity::class, TDetailNewHireCheckListEntity::class, TNewHireCheckListEntity::class, MUraianEntity::class, TDetailOrientationEntity::class, TOrientationEntity::class, MEmployeeEntity::class],
        version = 1,
        exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class HRRoomDatabase : RoomDatabase() {
    abstract fun hireChecklistDao(): HireCheckListDao
    abstract fun orientationDao(): OrientationDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: HRRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): HRRoomDatabase {
            if (INSTANCE == null) {
                synchronized(HRRoomDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            HRRoomDatabase::class.java, "dc_hra"
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE as HRRoomDatabase
        }
    }
}