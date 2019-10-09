package com.example.lazyjogger.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [(User::class) /*,(ContactInfo::class)*/] , version = 1, exportSchema = false)
abstract class UserDB: RoomDatabase() {
    abstract fun userDao(): UserDao
    //abstract fun contactListDao(): ContactInfoDao

    companion object {
    private var sInstance: UserDB? = null
    @Synchronized
    fun get(context: Context): UserDB {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(
                context.applicationContext,
                UserDB::class.java, "user.db"
            ).build()
        }
        return sInstance!!
        }
    }
}