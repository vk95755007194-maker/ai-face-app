package com.example.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Profile
import com.example.data.ProfileRepository
import com.example.domain.VerificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VerificationState {
    object Idle : VerificationState()
    object Processing : VerificationState()
    data class MatchFound(val profile: Profile) : VerificationState()
    data class NoMatch(val reason: String) : VerificationState()
    object ConsentRequired : VerificationState()
}

class FaceVerifyViewModel(
    private val repository: ProfileRepository,
    private val verificationManager: VerificationManager = VerificationManager()
) : ViewModel() {

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState.asStateFlow()

    val userProfile = repository.profile

    fun resetState() {
        _verificationState.value = VerificationState.Idle
    }

    fun verifyFace(bitmap: Bitmap) {
        viewModelScope.launch {
            _verificationState.value = VerificationState.Processing
            
            val currentProfile = repository.getProfileSync()
            if (currentProfile == null || !currentProfile.consentGiven) {
                _verificationState.value = VerificationState.ConsentRequired
                return@launch
            }

            val analysis = verificationManager.analyzeFace(bitmap)
            
            if (analysis.hasFace && analysis.faceQualityGood) {
                // Simulated match logic: if face is good, match to the existing profile
                _verificationState.value = VerificationState.MatchFound(currentProfile)
            } else {
                val reason = if (!analysis.hasFace) "No clear face detected." else "Face quality is too low."
                _verificationState.value = VerificationState.NoMatch(reason)
            }
        }
    }

    fun saveProfile(profile: Profile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            repository.deleteProfile()
        }
    }
}
