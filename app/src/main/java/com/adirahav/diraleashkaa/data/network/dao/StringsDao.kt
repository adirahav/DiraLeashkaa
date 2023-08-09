package com.adirahav.diraleashkaa.data.network.dao

import androidx.room.*
import com.adirahav.diraleashkaa.data.network.entities.StringEntity
import java.util.ArrayList

@Dao
interface StringsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(content: StringEntity) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(string: StringEntity)

    @Delete
    fun delete(string: StringEntity)

    @Query("SELECT * FROM stringEntity")
    fun getAll(): List<StringEntity>

    @Query("DELETE FROM stringEntity")
    fun deleteAll()

    @Query("SELECT * FROM stringEntity WHERE roomSID = :roomSID LIMIT 1 ")
    fun findById(roomSID: Long): StringEntity

}
