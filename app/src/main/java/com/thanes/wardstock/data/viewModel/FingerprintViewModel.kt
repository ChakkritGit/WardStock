package com.thanes.wardstock.data.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.thanes.wardstock.data.models.RefillModel

class FingerprintViewModel(application: Application) : AndroidViewModel(application) {
  var fingerArray by mutableStateOf<List<RefillModel>>(emptyList())
    private set

  var selectedFinger by mutableStateOf<RefillModel?>(null)
    private set

  fun setFinger(finger: RefillModel) {
    selectedFinger = finger
  }

  fun setFingerArray(finger: List<RefillModel>) {
    fingerArray = finger
  }

  fun clear() {
    fingerArray = emptyList()
    selectedFinger = null
  }
}