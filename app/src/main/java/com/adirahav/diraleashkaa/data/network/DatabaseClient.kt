package com.adirahav.diraleashkaa.data.network

import android.content.Context
import androidx.room.Room
import com.adirahav.diraleashkaa.common.Configuration.DB_NAME

class DatabaseClient private constructor(private val context: Context) {
    // app database object
    val appDatabase: AppDatabase

    companion object {
        private var instance: DatabaseClient? = null
        @Synchronized
        fun getInstance(context: Context): DatabaseClient? {
            if (instance == null) {
                instance = DatabaseClient(context)
            }
            return instance
        }
    }

    init {
        // creating the app database with Room database builder
        appDatabase = Room
            .databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
}