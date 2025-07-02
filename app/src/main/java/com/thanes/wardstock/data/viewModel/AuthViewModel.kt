package com.thanes.wardstock.data.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.data.store.DataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthState(
  val isLoading: Boolean = true,
  val token: String? = null,
  val userData: UserData? = null,
  val isAuthenticated: Boolean = false,
  val error: String? = null
)

object TokenHolder {
  var token: String? = null
}

class AuthViewModel : ViewModel() {
  private val _authState = MutableStateFlow(AuthState())
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  fun initializeAuth(context: Context) {
    viewModelScope.launch {
      try {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        val token = DataManager.getToken(context).first()
        TokenHolder.token = token
        val userData = if (token.isNotEmpty()) {
          DataManager.getUserData(context).first()
        } else {
          null
        }

        _authState.value = _authState.value.copy(
          isLoading = false,
          token = token,
          userData = userData,
          isAuthenticated = token.isNotEmpty() && userData != null
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          isLoading = false,
          error = e.message
        )
      }
    }
  }

  fun login(context: Context, token: String, userData: UserData) {
    viewModelScope.launch {
      try {
        DataManager.saveToken(context, token)
        DataManager.saveUserData(context, userData)

        _authState.value = _authState.value.copy(
          token = token,
          userData = userData,
          isAuthenticated = true,
          error = null
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          error = e.message
        )
      }
    }
  }

  fun logout(context: Context) {
    viewModelScope.launch {
      try {
        DataManager.clearAll(context)

        _authState.value = AuthState(
          isLoading = false,
          token = null,
          userData = null,
          isAuthenticated = false
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          error = e.message
        )
      }
    }
  }

  fun updateUserData(context: Context, userData: UserData) {
    viewModelScope.launch {
      try {
        DataManager.saveUserData(context, userData)

        _authState.value = _authState.value.copy(
          userData = userData
        )
      } catch (e: Exception) {
        _authState.value = _authState.value.copy(
          error = e.message
        )
      }
    }
  }

  fun clearError() {
    _authState.value = _authState.value.copy(error = null)
  }
}