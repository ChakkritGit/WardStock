package com.thanes.wardstock.data.repositories

import android.content.Context
import retrofit2.Response
import com.thanes.wardstock.data.models.ApiResponse
import com.thanes.wardstock.data.models.OrderModel
import com.thanes.wardstock.data.models.RefillDrugModel
import com.thanes.wardstock.data.models.RefillModel
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.data.models.UserModel
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.remote.api.services.AddDrugRequest
import com.thanes.wardstock.remote.api.services.LoginRequest
import com.thanes.wardstock.remote.configs.RetrofitInstance
import com.thanes.wardstock.remote.configs.RetrofitInstance.createApiWithAuth
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object ApiRepository {
  suspend fun login(userName: String, userPassword: String): Response<ApiResponse<UserData>> {
    val request = LoginRequest(userName, userPassword)
    return RetrofitInstance.api.login(request)
  }

  suspend fun orderWithPresId(context: Context, prescriptionId: String): Response<ApiResponse<OrderModel>> {
    return createApiWithAuth(context).orderWithPresId(prescriptionId)
  }

  suspend fun orderWithInitial(context: Context): Response<ApiResponse<OrderModel>> {
    return createApiWithAuth(context).orderWithOutPresId()
  }

  suspend fun refill(context: Context): Response<ApiResponse<List<RefillModel>>> {
    return createApiWithAuth(context).refill()
  }

  suspend fun addDrug(context: Context, prescriptionId: String, inventoryQty: Int): Response<ApiResponse<RefillDrugModel>> {
    val request = AddDrugRequest(inventoryQty)
    return createApiWithAuth(context).addDrug(prescriptionId, request)
  }

  suspend fun userWithInitial(context: Context): Response<ApiResponse<List<UserModel>>> {
    return createApiWithAuth(context).getUser()
  }

  suspend fun uploadUserWithImage(
    context: Context,
    imagePart: MultipartBody.Part,
    username: String,
    password: String,
    display: String,
    role: UserRole
  ): Response<ApiResponse<UserModel>> {
    val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
    val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())
    val displayPart = display.toRequestBody("text/plain".toMediaTypeOrNull())
    val rolePart = role.name.toRequestBody("text/plain".toMediaTypeOrNull())

    return createApiWithAuth(context).uploadUser(
      image = imagePart,
      username = usernamePart,
      password = passwordPart,
      display = displayPart,
      role = rolePart
    )
  }
}