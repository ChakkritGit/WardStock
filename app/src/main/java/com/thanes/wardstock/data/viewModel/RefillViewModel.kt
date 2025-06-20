package com.thanes.wardstock.data.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.models.RefillModel
import com.thanes.wardstock.data.repositories.ApiRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class RefillViewModel(application: Application) : AndroidViewModel(application) {
  var refillState by mutableStateOf<List<RefillModel>>(emptyList())
    private set

  var isLoading by mutableStateOf(false)
    private set

  var errorMessage by mutableStateOf("")
    internal set

  var selectedDrug by mutableStateOf<RefillModel?>(null)
    private set

  fun selectDrug(drug: RefillModel) {
    selectedDrug = drug
  }

  fun clear() {
    refillState = emptyList()
    isLoading = false
    errorMessage = ""
    selectedDrug = null
  }

  fun fetchRefill() {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.refill(application)
        if (response.isSuccessful) {
          refillState = response.body()?.data ?: emptyList()
        } else {
          val errorJson = response.errorBody()?.string()
          val message = try {
            JSONObject(errorJson ?: "").getString("message")
          } catch (_: Exception) {
            when (response.code()) {
              400 -> "Invalid request data"
              401 -> "Authentication required"
              403 -> "Access denied"
              404 -> "Prescription not found"
              500 -> "Server error, please try again later"
              else -> "HTTP Error ${response.code()}: ${response.message()}"
            }
          }
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = when (e) {
          is java.net.UnknownHostException -> {
            "No internet connection"
          }

          is java.net.SocketTimeoutException -> {
            "Request timeout, please try again"
          }

          is java.net.ConnectException -> {
            "Unable to connect to server"
          }

          is javax.net.ssl.SSLException -> {
            "Secure connection failed"
          }

          is com.google.gson.JsonSyntaxException -> {
            "Invalid response format"
          }

          is java.io.IOException -> {
            "Network error occurred"
          }

          else -> {
            Log.e("OrderAPI", "Unexpected error: ${e.javaClass.simpleName}", e)
            "Unexpected error occurred"
          }
        }
      } finally {
        isLoading = false
      }
    }
  }
}
