package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "profile")
@Serializable
data class Profile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val bio: String,
    val photoUri: String,
    val consentGiven: Boolean,
    val isVerified: Boolean = true
)
