package com.example.lazyjogger.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.google.gson.Gson
import org.osmdroid.util.GeoPoint
import com.google.gson.reflect.TypeToken



@Database(entities = [(User::class)] , version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class UserDB: RoomDatabase() {
    abstract fun userDao(): UserDao

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

/**
 * TypeConverters to store Lists inside the database
 */

class TypeConverter {
    @androidx.room.TypeConverter
    fun fromGeoPointList(geoPoints: List<GeoPoint>): String {
        val gson = Gson()
        val type = object : TypeToken<List<GeoPoint>>() {
        }.type
        return gson.toJson(geoPoints, type)
    }

    @androidx.room.TypeConverter
    fun toGeoPointList(geoPoints: String): List<GeoPoint> {
        val gson = Gson()
        val collectionType = object : TypeToken<List<GeoPoint>>() {
        }.type
        return gson.fromJson(geoPoints, collectionType)
    }

    @androidx.room.TypeConverter
    fun fromHRList(hrList: List<Int>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {
        }.type
        return gson.toJson(hrList, type)
    }

    @androidx.room.TypeConverter
    fun toHRList(hrList: String): List<Int> {
        val gson = Gson()
        val collectionType = object : TypeToken<List<Int>>() {
        }.type
        return gson.fromJson(hrList, collectionType)
    }
}

