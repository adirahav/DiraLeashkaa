package com.adirahav.diraleashkaa.data.network

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adirahav.diraleashkaa.data.network.dao.*
import com.adirahav.diraleashkaa.data.network.entities.*

@Database(entities = [FixedParametersEntity::class, UserEntity::class, PropertyEntity::class, BestYieldEntity::class, StringEntity::class, CalculatorEntity::class], version = 86)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fixedParametersDao(): FixedParametersDao
    abstract fun userDao(): UsersDao
    abstract fun propertyDao(): PropertiesDao
    abstract fun calculatorDao(): CalculatorsDao
    abstract fun bestYieldDao(): BestYieldDao
    abstract fun stringsDao(): StringsDao
}