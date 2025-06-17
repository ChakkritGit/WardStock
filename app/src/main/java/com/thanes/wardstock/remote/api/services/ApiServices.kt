package com.thanes.wardstock.remote.api.services

import com.thanes.wardstock.data.models.ApiResponse
import com.thanes.wardstock.data.models.OrderModel
import com.thanes.wardstock.data.models.UserData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class LoginRequest(val username: String, val password: String)

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserData>>

  @GET("dispense/{prescriptionId}")
  suspend fun orderWithPresId(@Path("prescriptionId") prescriptionId: String): Response<ApiResponse<OrderModel>>

  @GET("dispense/prescription/order")
  suspend fun orderWithOutPresId(): Response<ApiResponse<OrderModel>>
}