package com.example.lazyjogger.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getAll(): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Query("DELETE FROM user WHERE uid = :userId")
    fun deleteByUserId(userId: Long): Int
}
/*
@Dao
interface ContactInfoDao {

    @Query("SELECT * FROM ContactInfo")
    fun getAll(): LiveData<List<ContactInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: ContactInfo): Long
}
*/