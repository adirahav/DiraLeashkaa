package com.adirahav.diraleashkaa.data.network.dao

import androidx.room.*
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity

@Dao
interface PhrasesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(content: PhraseEntity) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(string: PhraseEntity)

    @Delete
    fun delete(string: PhraseEntity)

    @Query("SELECT * FROM phraseEntity")
    fun getAll(): List<PhraseEntity>

    @Query("SELECT * FROM phraseEntity LIMIT 1")
    fun getFirst(): PhraseEntity

    @Query("DELETE FROM phraseEntity")
    fun deleteAll()

    @Query("SELECT * FROM phraseEntity WHERE roomSID = :roomSID LIMIT 1 ")
    fun findById(roomSID: Long): PhraseEntity

}
