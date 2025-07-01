package com.thanes.wardstock.data.repositories

import android.content.Context
import retrofit2.Response
import com.thanes.wardstock.data.models.ApiResponse
import com.thanes.wardstock.data.models.DrugExitsModel
import com.thanes.wardstock.data.models.DrugModel
import com.thanes.wardstock.data.models.GroupInventoryModel
import com.thanes.wardstock.data.models.InventoryModel
import com.thanes.wardstock.data.models.MachineModel
import com.thanes.wardstock.data.models.OrderModel
import com.thanes.wardstock.data.models.RefillDrugModel
import com.thanes.wardstock.data.models.RefillModel
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.data.models.UserModel
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.remote.api.services.AddDrugRequest
import com.thanes.wardstock.remote.api.services.InventoryRequest
import com.thanes.wardstock.remote.api.services.LoginRequest
import com.thanes.wardstock.remote.api.services.MachineRequest
import com.thanes.wardstock.remote.configs.RetrofitInstance
import com.thanes.wardstock.remote.configs.RetrofitInstance.createApiWithAuth
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object ApiRepository {
  suspend fun login(userName: String, userPassword: String): Response<ApiResponse<UserData>> {
    val request = LoginRequest(userName, userPassword)
    return RetrofitInstance.api.login(request)
  }

  suspend fun orderWithPresId(
    context: Context,
    prescriptionId: String
  ): Response<ApiResponse<OrderModel>> {
    return createApiWithAuth(context).orderWithPresId(prescriptionId)
  }

  suspend fun orderWithInitial(context: Context): Response<ApiResponse<OrderModel>> {
    return createApiWithAuth(context).orderWithOutPresId()
  }

  suspend fun refill(context: Context): Response<ApiResponse<List<RefillModel>>> {
    return createApiWithAuth(context).refill()
  }

  suspend fun addDrug(
    context: Context,
    prescriptionId: String,
    inventoryQty: Int
  ): Response<ApiResponse<RefillDrugModel>> {
    val request = AddDrugRequest(inventoryQty)
    return createApiWithAuth(context).addDrug(prescriptionId, request)
  }

  suspend fun userWithInitial(context: Context): Response<ApiResponse<List<UserModel>>> {
    return createApiWithAuth(context).getUser()
  }

  suspend fun createUserWithImage(
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

    return createApiWithAuth(context).createUser(
      image = imagePart,
      username = usernamePart,
      password = passwordPart,
      display = displayPart,
      role = rolePart
    )
  }

  suspend fun updateUserWithImage(
    context: Context,
    userId: String,
    imagePart: MultipartBody.Part?,
    username: String,
    display: String,
    role: UserRole
  ): Response<ApiResponse<UserModel>> {
    val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
    val displayPart = display.toRequestBody("text/plain".toMediaTypeOrNull())
    val rolePart = role.name.toRequestBody("text/plain".toMediaTypeOrNull())

    return createApiWithAuth(context).updatedUser(
      userId = userId,
      image = imagePart,
      username = usernamePart,
      display = displayPart,
      role = rolePart
    )
  }

  suspend fun removeUser(context: Context, userId: String): Response<ApiResponse<String>> {
    return createApiWithAuth(context).removeUser(userId = userId)
  }

  suspend fun getDrug(context: Context): Response<ApiResponse<List<DrugModel>>> {
    return createApiWithAuth(context).getDrug()
  }

  suspend fun getDrugExits(context: Context): Response<ApiResponse<List<DrugExitsModel>>> {
    return createApiWithAuth(context).getDrugExits()
  }

  suspend fun createDrugWithImage(
    context: Context,
    imagePart: MultipartBody.Part,
    drugCode: String,
    drugName: String,
    unit: String,
    weight: Int,
    drugLot: String,
    drugExpire: String,
    drugPriority: Int,
    drugStatus: Boolean,
    comment: String?
  ): Response<ApiResponse<DrugModel>> {

    val mediaType = "text/plain".toMediaTypeOrNull()

    val codePart = drugCode.toRequestBody(mediaType)
    val namePart = drugName.toRequestBody(mediaType)
    val unitPart = unit.toRequestBody(mediaType)
    val weightPart = weight.toString().toRequestBody(mediaType)
    val lotPart = drugLot.toRequestBody(mediaType)
    val expirePart = drugExpire.toRequestBody(mediaType)
    val priorityPart = drugPriority.toString().toRequestBody(mediaType)
    val statusPart = drugStatus.toString().toRequestBody(mediaType)
    val commentPart = comment?.toRequestBody(mediaType)

    return createApiWithAuth(context).createDrug(
      image = imagePart,
      drugCode = codePart,
      drugName = namePart,
      unit = unitPart,
      weight = weightPart,
      drugLot = lotPart,
      drugExpire = expirePart,
      drugPriority = priorityPart,
      drugStatus = statusPart,
      comment = commentPart
    )
  }

  suspend fun updateDrugWithImage(
    context: Context,
    drugId: String,
    imagePart: MultipartBody.Part?,
    drugCode: String,
    drugName: String,
    unit: String,
    weight: Int,
    drugLot: String,
    drugExpire: String,
    drugPriority: Int,
    drugStatus: Boolean,
    comment: String?
  ): Response<ApiResponse<DrugModel>> {
    val mediaType = "text/plain".toMediaTypeOrNull()

    val codePart = drugCode.toRequestBody(mediaType)
    val namePart = drugName.toRequestBody(mediaType)
    val unitPart = unit.toRequestBody(mediaType)
    val weightPart = weight.toString().toRequestBody(mediaType)
    val lotPart = drugLot.toRequestBody(mediaType)
    val expirePart = drugExpire.toRequestBody(mediaType)
    val priorityPart = drugPriority.toString().toRequestBody(mediaType)
    val statusPart = drugStatus.toString().toRequestBody(mediaType)
    val commentPart = comment?.toRequestBody(mediaType)

    return createApiWithAuth(context).updateDrug(
      drugId = drugId,
      image = imagePart,
      drugCode = codePart,
      drugName = namePart,
      unit = unitPart,
      weight = weightPart,
      drugLot = lotPart,
      drugExpire = expirePart,
      drugPriority = priorityPart,
      drugStatus = statusPart,
      comment = commentPart
    )
  }

  suspend fun removeDrug(context: Context, drugId: String): Response<ApiResponse<String>> {
    return createApiWithAuth(context).removeDrug(drugId = drugId)
  }

  suspend fun getMachine(context: Context): Response<ApiResponse<List<MachineModel>>> {
    return createApiWithAuth(context).getMachine()
  }

  suspend fun createMachine(
    context: Context,
    machineName: String,
    location: String,
    capacity: Int,
    status: Boolean,
    comment: String
  ): Response<ApiResponse<MachineModel>> {
    val request = MachineRequest(
      machineName = machineName,
      location = location,
      capacity = capacity,
      status = status,
      comment = comment
    )

    return createApiWithAuth(context).createMachine(request)
  }

  suspend fun updateMachine(
    context: Context,
    id: String,
    machineName: String,
    location: String,
    capacity: Int,
    status: Boolean,
    comment: String
  ): Response<ApiResponse<MachineModel>> {
    val request = MachineRequest(
      machineName = machineName,
      location = location,
      capacity = capacity,
      status = status,
      comment = comment
    )

    return createApiWithAuth(context).updateMachine(id, request)
  }

  suspend fun removeMachine(context: Context, machineId: String): Response<ApiResponse<String>> {
    return createApiWithAuth(context).removeMachine(machineId = machineId)
  }

  suspend fun getInventory(context: Context): Response<ApiResponse<List<InventoryModel>>> {
    return createApiWithAuth(context).getInventory()
  }

  suspend fun getGroupInventory(context: Context): Response<ApiResponse<List<GroupInventoryModel>>> {
    return createApiWithAuth(context).getGroupInventory()
  }

  suspend fun createInventory(
    context: Context,
    position: Int?,
    min: Int,
    max: Int,
    machineId: String,
    status: Boolean,
    comment: String
  ): Response<ApiResponse<InventoryModel>> {
    val request = InventoryRequest(
      position = position!!,
      min = min,
      max = max,
      machineId = machineId,
      status = status,
      comment = comment
    )

    return createApiWithAuth(context).createInventory(request)
  }

  suspend fun updateInventory(
    context: Context,
    id: String,
    position: Int,
    min: Int,
    max: Int,
    status: Boolean,
    machineId: String,
    comment: String
  ): Response<ApiResponse<InventoryModel>> {
    val request = InventoryRequest(
      position = position,
      min = min,
      max = max,
      machineId = machineId,
      status = status,
      comment = comment
    )

    return createApiWithAuth(context).updateInventory(id, request)
  }

  suspend fun removeInventory(context: Context, inventoryId: String): Response<ApiResponse<String>> {
    return createApiWithAuth(context).removeInventory(inventoryId = inventoryId)
  }
}