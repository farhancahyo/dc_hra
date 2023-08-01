package com.shiro.formhrddover.database.dao

import androidx.room.*
import com.shiro.formhrddover.database.entity.hirechecklist.MItemEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TDetailNewHireCheckListEntity
import com.shiro.formhrddover.database.entity.hirechecklist.TNewHireCheckListEntity
import com.shiro.formhrddover.database.entity.hirechecklist.relation.MItemUraianCheckList
import com.shiro.formhrddover.database.entity.orientation.MEmployeeEntity
import com.shiro.formhrddover.database.entity.orientation.MUraianEntity
import com.shiro.formhrddover.database.entity.orientation.TDetailOrientationEntity
import com.shiro.formhrddover.database.entity.orientation.TOrientationEntity

@Dao
interface OrientationDao {
    // Master Uraian
    @Query("DELETE FROM m_uraian")
    suspend fun clearMUraian()

    @Query("SELECT * FROM m_uraian WHERE status = 1 ORDER BY internalid ASC")
    suspend fun getMUraian(): List<MUraianEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMUraian(equipment: List<MUraianEntity>)

    // Master Employee
    @Query("DELETE FROM m_employee")
    suspend fun clearMEmployee()

    @Query("SELECT * FROM m_employee WHERE employeename LIKE '%' || :keyword || '%' ORDER BY internalid ASC")
    suspend fun getMEmployee(keyword: String): List<MEmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMEmployee(equipment: List<MEmployeeEntity>)

    // Trx Orientation
    @Query("SELECT * FROM t_orientation WHERE transactionno LIKE '%YTRX1%' ORDER BY createddate DESC")
    suspend fun getTOrientationOffline(): List<TOrientationEntity>

    @Query("SELECT * FROM t_orientation ORDER BY createddate DESC")
    suspend fun getTOrientation(): List<TOrientationEntity>

    @Query("SELECT * FROM t_orientation WHERE employeename LIKE '%' || :name || '%' AND lastmodifiedname LIKE '%' || :officer || '%' AND starteddate BETWEEN :sdate AND :edate COLLATE NOCASE ORDER BY createddate DESC")
    suspend fun getTOrientation(sdate : Long, edate : Long, name : String, officer : String): List<TOrientationEntity>

    @Query("SELECT * FROM t_orientation WHERE transactionno = :idtrx ORDER BY createddate DESC")
    suspend fun getTOrientation(idtrx : String): TOrientationEntity

    @Query("SELECT * FROM t_orientation WHERE status = :status")
    suspend fun getTOrientation(status: Int): List<TOrientationEntity>

    @Query("SELECT * FROM t_orientation WHERE employeeno = :employeeno")
    suspend fun getTOrientationEmployee(employeeno: Int): List<TOrientationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTOrientation(data: TOrientationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTOrientation(data: List<TOrientationEntity>)

    @Update
    suspend fun updateTOrientation(perform: TOrientationEntity)

    @Query("DELETE FROM t_orientation WHERE status = 2")
    suspend fun clearTOrientation()

    @Delete
    suspend fun deleteTOrientation(data: TOrientationEntity)

    // Trx Detail Orientation
    @Query("SELECT * FROM t_detail_orientation WHERE transactionno = :transanctionno AND uraianid = :uraianid")
    suspend fun getTDetailOrientation(transanctionno: String, uraianid: Int): TDetailOrientationEntity

    @Query("SELECT * FROM t_detail_orientation WHERE transactionno = :transanctionno")
    suspend fun getTDetailOrientation(transanctionno: String): List<TDetailOrientationEntity>

    @Query("SELECT * FROM t_detail_orientation WHERE status = :status")
    suspend fun getTDetailOrientation(status: Int): List<TDetailOrientationEntity>

    @Query("SELECT * FROM t_detail_orientation ORDER BY internalid ASC")
    suspend fun getTDetailOrientation(): List<TDetailOrientationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTDetailOrientation(detail: TDetailOrientationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTDetailOrientation(detail: List<TDetailOrientationEntity>)

    @Update
    suspend fun updateTDetailOrientation(detail: TDetailOrientationEntity)

    @Query("DELETE FROM t_detail_orientation WHERE status = 2")
    suspend fun clearTDetailOrientation()

    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 't_detail_orientation'")
    suspend fun resetTDetailOrientation()

    @Delete
    suspend fun deleteTDetailOrientation(data: TDetailOrientationEntity)
}