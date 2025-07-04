package com.thanes.wardstock.data.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.models.InventoryExitsModel
import com.thanes.wardstock.data.models.InventoryModel
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application)  {
  var inventoryState by mutableStateOf<List<InventoryModel>>(emptyList())
    private set

  var inventoryExitsState by mutableStateOf<List<InventoryExitsModel>>(emptyList())
    private set

  var isLoading by mutableStateOf(false)
    private set

  var errorMessage by mutableStateOf("")
    internal set

  var selectedInventory by mutableStateOf<InventoryModel?>(null)
    private set

  fun setInventory(inventory: InventoryModel) {
    selectedInventory = inventory
  }

  fun clear() {
    selectedInventory = null
  }

  fun fetchInventory() {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.getInventory()
        if (response.isSuccessful) {
          inventoryState = response.body()?.data ?: emptyList()
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

  fun fetchInventoryExits() {
    errorMessage = ""
    isLoading = true

    viewModelScope.launch {
      try {
        val response = ApiRepository.getInventoryExits()
        if (response.isSuccessful) {
          inventoryExitsState = response.body()?.data ?: emptyList()
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