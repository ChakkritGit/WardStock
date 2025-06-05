package com.thanes.wardstock.data.repositories

import retrofit2.Response
import com.thanes.wardstock.data.models.ApiResponse
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.remote.api.services.LoginRequest
import com.thanes.wardstock.remote.configs.RetrofitInstance

object ApiRepository {
  suspend fun login(userName: String, userPassword: String): Response<ApiResponse<UserData>> {
    val request = LoginRequest(userName, userPassword)
    return RetrofitInstance.api.login(request)
  }
}