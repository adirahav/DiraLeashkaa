package com.adirahav.diraleashkaa.data.network.dao

import androidx.room.*
import com.adirahav.diraleashkaa.data.network.entities.CalculatorEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity

@Dao
interface CalculatorsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(calculator: CalculatorEntity) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMaxPrice(vararg calculator: PropertyEntity)

    @Delete
    fun delete(calculator: CalculatorEntity)

    @Query("SELECT * FROM calculatorEntity")
    fun getAll(): List<CalculatorEntity>//LiveData<List<CalculatorEntity>>

    @Query("DELETE FROM calculatorEntity")
    fun deleteAll()

    @Query("SELECT * FROM propertyEntity where type = 'max_price' LIMIT 1")
    fun getMaxPrice(): PropertyEntity//LiveData<List<CalculatorEntity>>

}
