package com.adirahav.diraleashkaa.data.network.dao

import androidx.room.*
import com.adirahav.diraleashkaa.data.network.entities.BestYieldEntity

@Dao
interface BestYieldDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(property: BestYieldEntity) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg property: BestYieldEntity): Int

    @Delete
    fun delete(property: BestYieldEntity): Int

    @Query("SELECT * FROM bestYieldEntity")
    fun getAll(): List<BestYieldEntity>//LiveData<List<PropertyEntity>>

    @Query("DELETE FROM bestYieldEntity")
    fun deleteAll()
}
