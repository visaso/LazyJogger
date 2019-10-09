package com.example.lazyjogger.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val uid: Long,
    val firstName: String,
    val lastName: String,
    val distance: Double,
    val date: String,
    val steps: Int,
    val geoPoints: List<GeoPoint>

) {

    override fun toString(): String = "$firstName $lastName"
}





