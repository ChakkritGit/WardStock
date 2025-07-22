package com.thanes.wardstock.data.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.models.UserFingerprint
import com.thanes.wardstock.data.models.UserModel
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
  var userState by mutableStateOf<List<UserModel>>(emptyList())
    private set

  var isLoading by mutableStateOf(false)
    private set

  var errorMessage by mutableStateOf("")
    internal set

  var selectedUser by mutableStateOf<UserModel?>(null)
    private set

  var fingerprintList by mutableStateOf<List<UserFingerprint>?>(emptyList())
    private set

  var fingerprintObject by mutableStateOf<UserFingerprint?>(null)
    private set

  fun setUserFingerprintList(fingerprint: List<UserFingerprint>?) {
    fingerprintList = fingerprint
  }

  fun setFingerObject(fingerprint: UserFingerprint) {
    fingerprintObject = fingerprint
  }

  fun setUser(user: UserModel) {
    selectedUser = user
  }

  fun clearFingerObject() {
    fingerprintObject = null
  }

  fun clearFingerprintList() {
    fingerprintList = emptyList()
  }

  fun clear() {
    selectedUser = null
  }

  fun fetchUser() {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.userWithInitial()
        if (response.isSuccessful) {
          userState = response.body()?.data ?: emptyList()
        } else {
          val errorJson = response.errorBody()?.string()
          val message = parseErrorMessage(response.code(), errorJson)
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
      } finally {
        isLoading = false
      }
    }
  }

  fun fetchUserFingerprint(userId: String) {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.getUserFingerprint(userId)
        if (response.isSuccessful) {
          fingerprintList = response.body()?.data ?: emptyList()
        } else {
          val errorJson = response.errorBody()?.string()
          val message = parseErrorMessage(response.code(), errorJson)
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
      } finally {
        isLoading = false
      }
    }
  }
}
