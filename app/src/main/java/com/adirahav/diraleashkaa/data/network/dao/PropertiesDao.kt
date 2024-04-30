package com.adirahav.diraleashkaa.data.network.dao

import androidx.room.*
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity

@Dao
interface PropertiesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(property: PropertyEntity) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg property: PropertyEntity)

    @Delete
    fun delete(property: PropertyEntity)

    @Query("UPDATE propertyEntity set archive = 1 where roomID is :roomID")
    fun archive(roomID: Long)

    @Query("UPDATE propertyEntity set archive = 0 where roomID is :roomID")
    fun restore(roomID: Long)

    @Query("SELECT * FROM propertyEntity WHERE archive is not 1 and type = 'property'")
    fun getAll(): List<PropertyEntity>//LiveData<List<PropertyEntity>>

    @Query("SELECT * FROM propertyEntity WHERE archive is 1 and type = 'property'")
    fun getAllArchive(): List<PropertyEntity>

    //@Query("SELECT city, COUNT(*) FROM propertyEntity GROUP BY city ORDER BY COUNT(*) DESC")
    @Query("SELECT city, COUNT(*) FROM propertyEntity WHERE archive is not 1 and roomID is not 0 and type = 'property' GROUP BY city")
    fun getMyCities(): List<PropertyEntity>//LiveData<List<PropertyEntity>>

    @Query("SELECT * FROM propertyEntity WHERE city is :city and roomID is not 0 and archive is not 1 and type = 'property'")
    fun getCityProperties(city: String?): List<PropertyEntity>//LiveData<List<PropertyEntity>>

    @Query("DELETE FROM propertyEntity")
    fun deleteAll()

    @Query("SELECT * FROM propertyEntity WHERE roomID = :roomID LIMIT 1 ")
    fun findById(roomID: Long): PropertyEntity//LiveData<PropertyEntity>

    @Query("SELECT * FROM propertyEntity WHERE _id = :propertyId LIMIT 1 ")
    fun findById(propertyId: String): PropertyEntity//LiveData<PropertyEntity>

    /*@Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE user_name LIKE :uname LIMIT 1 ")
    fun findByName(uname: String): UserEntity

    @Insert
    fun insertAll(users: User)
*/

}
