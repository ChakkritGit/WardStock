package com.thanes.wardstock.data.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.models.DrugExitsModel
import com.thanes.wardstock.data.models.DrugModel
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

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
        val response = ApiRepository.getDrug()
        if (response.isSuccessful) {
          drugState = response.body()?.data ?: emptyList()
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

  fun fetchDrugExits() {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.getDrugExits()
        if (response.isSuccessful) {
          drugExitsState = response.body()?.data ?: emptyList()
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