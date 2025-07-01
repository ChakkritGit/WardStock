package com.thanes.wardstock.data.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.thanes.wardstock.data.models.DrugExitsModel
import com.thanes.wardstock.data.models.DrugModel
import com.thanes.wardstock.data.repositories.ApiRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class DrugViewModel(application: Application) : AndroidViewModel(application) {
  var drugState by mutableStateOf<List<DrugModel>>(emptyList())
    private set

  var drugExitsState by mutableStateOf<List<DrugExitsModel>>(emptyList())
    private set

  var isLoading by mutableStateOf(false)
    private set

  var errorMessage by mutableStateOf("")
    internal set

  var selectedDrug by mutableStateOf<DrugModel?>(null)
    private set

  fun setDrug(drug: DrugModel) {
    selectedDrug = drug
  }

  fun clear() {
    selectedDrug = null
  }

  fun fetchDrug() {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.getDrug(application)
        if (response.isSuccessful) {
          drugState = response.body()?.data ?: emptyList()
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
          is UnknownHostException -> {
            "No internet connection"
          }

          is SocketTimeoutException -> {
            "Request timeout, please try again"
          }

          is ConnectException -> {
            "Unable to connect to server"
          }

          is SSLException -> {
            "Secure connection failed"
          }

          is JsonSyntaxException -> {
            "Invalid response format"
          }

          is IOException -> {
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

  fun fetchDrugExits() {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.getDrugExits(application)
        if (response.isSuccessful) {
          drugExitsState = response.body()?.data ?: emptyList()
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
          is UnknownHostException -> {
            "No internet connection"
          }

          is SocketTimeoutException -> {
            "Request timeout, please try again"
          }

          is ConnectException -> {
            "Unable to connect to server"
          }

          is SSLException -> {
            "Secure connection failed"
          }

          is JsonSyntaxException -> {
            "Invalid response format"
          }

          is IOException -> {
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