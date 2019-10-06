package com.example.lazyjogger.database

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val uid: Long,
    val firstName: String,
    val lastName: String) {

    override fun toString(): String = "$firstName $lastName"
}





