package com.thanes.wardstock.data.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.models.RefillModel
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

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
        val response = ApiRepository.refill()
        if (response.isSuccessful) {
          refillState = response.body()?.data ?: emptyList()
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
