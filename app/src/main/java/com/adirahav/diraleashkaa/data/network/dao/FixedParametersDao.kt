package com.adirahav.diraleashkaa.data.network.dao

import androidx.room.*
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity

@Dao
interface FixedParametersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fixedParameter: FixedParametersEntity) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg fixedParameter: FixedParametersEntity)

    @Delete
    fun delete(fixedParameter: FixedParametersEntity)

    //@Query("SELECT * FROM fixedParametersEntity LIMIT 1 OFFSET 0")
    @Query("SELECT * FROM fixedParametersEntity /*ORDER BY register_time DESC */LIMIT 1")
    fun getAll(): List<FixedParametersEntity>//LiveData<List<FixedParametersEntity>>

    @Query("DELETE FROM fixedParametersEntity")
    fun deleteAll()
}
