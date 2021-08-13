package com.shiro.formhrddover.database.dao

import androidx.room.*
import com.shiro.formhrddover.database.entity.hirechecklist.*
import com.shiro.formhrddover.database.entity.hirechecklist.relation.MItemUraianCheckList

@Dao
interface HireCheckListDao {

    // Master Category
    @Query("DELETE FROM m_category")
    suspend fun clearMCategory()

    @Query("SELECT * FROM m_category ORDER BY internalid ASC")
    suspend fun getMCategory(): List<MCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMCategory(equipment: List<MCategoryEntity>)

    // Master Item
    @Query("DELETE FROM m_item")
    suspend fun clearMItem()

    @Query("SELECT * FROM m_item ORDER BY internalid ASC")
    suspend fun getMItem(): List<MItemEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMItem(equipment: List<MItemEntity>)

    // Master Departement
    @Query("DELETE FROM m_departement")
    suspend fun clearMDepartement()

    @Query("SELECT * FROM m_departement ORDER BY internalid ASC")
    suspend fun getMDepartement(): List<MDepartementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMDepartement(equipment: List<MDepartementEntity>)

    // Master Mapping Category
    @Query("DELETE FROM m_mapping_category")
    suspend fun clearMMappingCategory()

    @Query("SELECT * FROM m_mapping_category ORDER BY internalid ASC")
    suspend fun getMMappingCategory(): List<MMappingCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMMappingCategory(equipment: List<MMappingCategoryEntity>)

    // Transaksi New Hire Check List
    @Query("SELECT * FROM t_new_hire_checklist_entity WHERE transactionno LIKE '%ZTRX1%' ORDER BY createddate DESC")
    suspend fun getTNewHireChecklistOffline(): List<TNewHireCheckListEntity>

    @Query("SELECT * FROM t_new_hire_checklist_entity ORDER BY createddate DESC")
    suspend fun getTNewHireChecklist(): List<TNewHireCheckListEntity>

    @Query("SELECT * FROM t_new_hire_checklist_entity WHERE employeename LIKE '%' || :name || '%' AND employeeno LIKE '%' || :no || '%' AND jointdate BETWEEN :sdate AND :edate ORDER BY createddate DESC")
    suspend fun getTNewHireChecklist(sdate : Long, edate : Long, name : String, no : String): List<TNewHireCheckListEntity>

    @Query("SELECT * FROM t_new_hire_checklist_entity WHERE transactionno = :idtrx ORDER BY createddate DESC")
    suspend fun getTNewHireChecklist(idtrx : String): TNewHireCheckListEntity

    @Query("SELECT * FROM t_new_hire_checklist_entity WHERE status = :status")
    suspend fun getTNewHireChecklist(status: Int): List<TNewHireCheckListEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTNewHireChecklist(data: TNewHireCheckListEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTNewHireChecklist(data: List<TNewHireCheckListEntity>)

    @Update
    suspend fun updateTNewHireChecklist(perform: TNewHireCheckListEntity)

    @Query("DELETE FROM t_new_hire_checklist_entity WHERE status = 2")
    suspend fun clearTNewHireChecklist()

    @Delete
    suspend fun deleteTNewHireChecklist(data: TNewHireCheckListEntity)

    // Transaksi Detail New Hire Check List
    @Query("SELECT * FROM t_detail_new_hire_checklist_entity WHERE transactionno = :transanctionno AND categoryid = :categoryid")
    suspend fun getTDetailNewHireCheckList(
            transanctionno: String,
            categoryid: Int
    ): List<TDetailNewHireCheckListEntity>

    @Query("SELECT * FROM t_detail_new_hire_checklist_entity WHERE transactionno = :transanctionno")
    suspend fun getTDetailNewHireCheckList(transanctionno: String): List<TDetailNewHireCheckListEntity>

    @Query("SELECT * FROM t_detail_new_hire_checklist_entity WHERE status = :status")
    suspend fun getTDetailNewHireCheckList(status: Int): List<TDetailNewHireCheckListEntity>

    @Query("SELECT * FROM t_detail_new_hire_checklist_entity ORDER BY internalid ASC")
    suspend fun getTDetailNewHireCheckList(): List<TDetailNewHireCheckListEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTDetailNewHireCheckList(detail: TDetailNewHireCheckListEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTDetailNewHireCheckList(detail: List<TDetailNewHireCheckListEntity>)

    @Update
    suspend fun updateTDetailNewHireCheckList(detail: TDetailNewHireCheckListEntity)

    @Query("DELETE FROM t_detail_new_hire_checklist_entity WHERE status = 2")
    suspend fun clearTDetailNewHireCheckList()

    // Relation
    @Query("SELECT a.internalid AS mappingid, c.itemname as itemname, c.status as status FROM m_mapping_category a, m_category b, m_item c WHERE a.categoryid = b.internalid AND a.itemid = c.internalid AND a.categoryid = :categoryid")
    suspend fun getItemUraianChecklist(categoryid: Int): List<MItemUraianCheckList>

    @Delete
    suspend fun deleteTDetailNewHireChecklist(data: TDetailNewHireCheckListEntity)
}