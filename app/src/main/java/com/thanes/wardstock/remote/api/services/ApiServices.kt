package com.thanes.wardstock.remote.api.services

import com.thanes.wardstock.data.models.ApiResponse
import com.thanes.wardstock.data.models.OrderModel
import com.thanes.wardstock.data.models.RefillDrugModel
import com.thanes.wardstock.data.models.RefillModel
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.data.models.UserModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class LoginRequest(val username: String, val password: String)
data class AddDrugRequest(val inventoryQty: Int)

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserData>>

  @GET("dispense/{prescriptionId}")
  suspend fun orderWithPresId(@Path("prescriptionId") prescriptionId: String): Response<ApiResponse<OrderModel>>

  @GET("dispense/prescription/order")
  suspend fun orderWithOutPresId(): Response<ApiResponse<OrderModel>>

  @GET("group-inventory/stock")
  suspend fun refill(): Response<ApiResponse<List<RefillModel>>>

  @PATCH("group-inventory/stock/{inventoryId}")
  suspend fun addDrug(@Path("inventoryId") inventoryId: String, @Body request: AddDrugRequest): Response<ApiResponse<RefillDrugModel>>

  @GET("users")
  suspend fun getUser(): Response<ApiResponse<List<UserModel>>>

  @Multipart
  @POST("users")
  suspend fun uploadUser(
    @Part image: MultipartBody.Part,
    @Part("username") username: RequestBody,
    @Part("password") password: RequestBody,
    @Part("display") display: RequestBody,
    @Part("role") role: RequestBody
  ): Response<ApiResponse<UserModel>>
}