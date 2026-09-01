package com.example.data

import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val profileDao: ProfileDao) {
    val profile: Flow<Profile?> = profileDao.getProfile()
    
    suspend fun getProfileSync(): Profile? = profileDao.getProfileSync()

    suspend fun saveProfile(profile: Profile) = profileDao.insertProfile(profile)

    suspend fun deleteProfile() = profileDao.deleteProfile()
}
