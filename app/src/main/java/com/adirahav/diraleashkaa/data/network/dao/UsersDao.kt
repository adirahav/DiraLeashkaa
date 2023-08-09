package com.adirahav.diraleashkaa.data.network.dao

import androidx.room.*
import com.adirahav.diraleashkaa.data.network.entities.UserEntity

@Dao
interface UsersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(user: UserEntity)

    @Delete
    fun delete(user: UserEntity)

    @Query("SELECT * FROM userEntity")
    fun getAll(): List<UserEntity>//LiveData<List<UserEntity>>

    @Query("DELETE FROM userEntity")
    fun deleteAll()

    @Query("SELECT * FROM userEntity WHERE roomUID = :roomUID LIMIT 1 ")
    fun findById(roomUID: Long): UserEntity//LiveData<UserEntity>

    /*@Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE user_name LIKE :uname LIMIT 1 ")
    fun findByName(uname: String): UserEntity

    @Insert
    fun insertAll(users: User)
*/

}
