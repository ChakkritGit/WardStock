package com.thanes.wardstock.data.repositories

import com.google.gson.Gson
import com.thanes.wardstock.data.models.ApiResponse
import com.thanes.wardstock.data.models.BiometricLoadModel
import com.thanes.wardstock.data.models.DrugExitsModel
import com.thanes.wardstock.data.models.DrugModel
import com.thanes.wardstock.data.models.GroupInventoryModel
import com.thanes.wardstock.data.models.InventoryExitsModel
import com.thanes.wardstock.data.models.InventoryItem
import com.thanes.wardstock.data.models.InventoryMinMax
import com.thanes.wardstock.data.models.InventoryModel
import com.thanes.wardstock.data.models.MachineModel
import com.thanes.wardstock.data.models.OrderModel
import com.thanes.wardstock.data.models.RefillDrugModel
import com.thanes.wardstock.data.models.RefillModel
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.data.models.UserFingerprint
import com.thanes.wardstock.data.models.UserModel
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.remote.api.services.AddDrugRequest
import com.thanes.wardstock.remote.api.services.AddFingerprintRequest
import com.thanes.wardstock.remote.api.services.FingerprintRequest
import com.thanes.wardstock.remote.api.services.GroupInventoryRequest
import com.thanes.wardstock.remote.api.services.InventoryRequest
import com.thanes.wardstock.remote.api.services.LoginRequest
import com.thanes.wardstock.remote.api.services.LoginVeinRequest
import com.thanes.wardstock.remote.api.services.MachineRequest
import com.thanes.wardstock.remote.configs.RetrofitInstance
import com.thanes.wardstock.remote.configs.RetrofitInstance.createApiWithAuth
import com.thanes.wardstock.screens.manage.user.BiometricData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

object ApiRepository {
  suspend fun login(userName: String, userPassword: String): Response<ApiResponse<UserData>> {
    val request = LoginRequest(userName, userPassword)
    return RetrofitInstance.api.login(request)
  }

  suspend fun loginWithVein(uid: String): Response<ApiResponse<UserData>> {
    val request = LoginVeinRequest(uid)
    return RetrofitInstance.api.loginWithVein(request)
  }

  suspend fun veryUser(userName: String, userPassword: String): Response<ApiResponse<String?>> {
    val request = LoginRequest(userName, userPassword)
    return RetrofitInstance.api.verifyUser(request)
  }

  suspend fun orderWithPresId(
    prescriptionId: String
  ): Response<ApiResponse<OrderModel>> {
    return createApiWithAuth().orderWithPresId(prescriptionId)
  }

  suspend fun orderWithInitial(): Response<ApiResponse<OrderModel>> {
    return createApiWithAuth().orderWithOutPresId()
  }

  suspend fun refill(): Response<ApiResponse<List<RefillModel>>> {
    return createApiWithAuth().refill()
  }

  suspend fun refillDrug(
    prescriptionId: String,
    inventoryQty: Int
  ): Response<ApiResponse<RefillDrugModel>> {
    val request = AddDrugRequest(inventoryQty)
    return createApiWithAuth().refillDrug(prescriptionId, request)
  }

  suspend fun userWithInitial(): Response<ApiResponse<List<UserModel>>> {
    return createApiWithAuth().getUser()
  }

  suspend fun createUserWithImage(
    imagePart: MultipartBody.Part,
    username: String,
    password: String,
    display: String,
    role: UserRole,
    biometrics: List<BiometricData>? = emptyList()
  ): Response<ApiResponse<UserModel>> {
    val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
    val passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull())
    val displayPart = display.toRequestBody("text/plain".toMediaTypeOrNull())
    val rolePart = role.name.toRequestBody("text/plain".toMediaTypeOrNull())

    var biometricsPart: RequestBody? = null

    if (biometrics != null) {
      val gson = Gson()
      val biometricsJson = gson.toJson(biometrics)

      biometricsPart = biometricsJson.toRequestBody("application/json".toMediaTypeOrNull())
    }

    return createApiWithAuth().createUser(
      image = imagePart,
      username = usernamePart,
      password = passwordPart,
      display = displayPart,
      role = rolePart,
      biometrics = biometricsPart
    )
  }

  suspend fun updateUserWithImage(
    userId: String,
    imagePart: MultipartBody.Part?,
    username: String,
    display: String,
    role: UserRole
  ): Response<ApiResponse<UserModel>> {
    val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
    val displayPart = display.toRequestBody("text/plain".toMediaTypeOrNull())
    val rolePart = role.name.toRequestBody("text/plain".toMediaTypeOrNull())

    return createApiWithAuth().updatedUser(
      userId = userId,
      image = imagePart,
      username = usernamePart,
      display = displayPart,
      role = rolePart
    )
  }

  suspend fun removeUser(userId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().removeUser(userId = userId)
  }

  suspend fun getDrug(): Response<ApiResponse<List<DrugModel>>> {
    return createApiWithAuth().getDrug()
  }

  suspend fun getDrugExits(): Response<ApiResponse<List<DrugExitsModel>>> {
    return createApiWithAuth().getDrugExits()
  }

  suspend fun createDrugWithImage(
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

    return createApiWithAuth().createDrug(
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

    return createApiWithAuth().updateDrug(
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

  suspend fun removeDrug(drugId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().removeDrug(drugId = drugId)
  }

  suspend fun getMachine(): Response<ApiResponse<List<MachineModel>>> {
    return createApiWithAuth().getMachine()
  }

  suspend fun createMachine(
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

    return createApiWithAuth().createMachine(request)
  }

  suspend fun updateMachine(
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

    return createApiWithAuth().updateMachine(id, request)
  }

  suspend fun removeMachine(machineId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().removeMachine(machineId = machineId)
  }

  suspend fun getInventory(): Response<ApiResponse<List<InventoryModel>>> {
    return createApiWithAuth().getInventory()
  }

  suspend fun getInventoryExits(): Response<ApiResponse<List<InventoryExitsModel>>> {
    return createApiWithAuth().getInventoryExits()
  }

  suspend fun getGroupInventory(): Response<ApiResponse<List<GroupInventoryModel>>> {
    return createApiWithAuth().getGroupInventory()
  }

  suspend fun createInventory(
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

    return createApiWithAuth().createInventory(request)
  }

  suspend fun updateInventory(
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

    return createApiWithAuth().updateInventory(id, request)
  }

  suspend fun removeInventory(inventoryId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().removeInventory(inventoryId = inventoryId)
  }

  suspend fun removeGroup(groupId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().removeGroup(groupId = groupId)
  }

  suspend fun createGroup(
    drugId: String?,
    groupMin: Int,
    groupMax: Int,
    inventories: List<InventoryItem>?
  ): Response<ApiResponse<String>> {
    val request = GroupInventoryRequest(
      drugId = drugId!!,
      groupMin = groupMin,
      groupMax = groupMax,
      inventories = inventories!!
    )

    return createApiWithAuth().createGroup(request)
  }

  suspend fun updateGroup(
    groupId: String,
    drugId: String?,
    groupMin: Int,
    groupMax: Int,
    inventories: List<InventoryItem>?
  ): Response<ApiResponse<String>> {
    val request = GroupInventoryRequest(
      drugId = drugId!!,
      groupMin = groupMin,
      groupMax = groupMax,
      inventories = inventories!!
    )

    return createApiWithAuth().updateGroup(groupId, request)
  }

  suspend fun updateOrderToPending(orderId: String, prescriptionId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().updateOrderToPending(orderId, prescriptionId)
  }

  suspend fun updateOrderToReceive(orderId: String, prescriptionId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().updateOrderToReceive(orderId, prescriptionId)
  }

  suspend fun updateOrderToComplete(orderId: String, prescriptionId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().updateOrderToComplete(orderId, prescriptionId)
  }

  suspend fun updateOrderToError(orderId: String, prescriptionId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().updateOrderToError(orderId, prescriptionId)
  }

  suspend fun getReportAlertMinMax(): Response<ApiResponse<List<InventoryMinMax>>> {
    return createApiWithAuth().getReportAlertMinMax()
  }

  suspend fun getAllBiometricsFromApi(): Response<ApiResponse<List<BiometricLoadModel>>> {
    return createApiWithAuth().getAllBiometricsFromApi()
  }

  suspend fun getUserFingerprint(userId: String): Response<ApiResponse<List<UserFingerprint>>> {
    return createApiWithAuth().getUserFingerprint(userId)
  }

  suspend fun updateFingerprint(
    id: String,
    description: String
  ): Response<ApiResponse<String>> {
    val request = FingerprintRequest(description = description)

    return createApiWithAuth().updateFingerprint(id, request)
  }

  suspend fun deleteFingerprint(bioId: String): Response<ApiResponse<String>> {
    return createApiWithAuth().deleteFingerprint(bioId)
  }

  suspend fun addFingerprint(
    userId: String,
    featureData: String,
    description: String?
  ): Response<ApiResponse<String>> {
    val request =
      AddFingerprintRequest(userId = userId, featureData = featureData, description = description)
    return createApiWithAuth().addFingerprint(request)
  }

  suspend fun clearPrescription(): Response<ApiResponse<String>> {
    return createApiWithAuth().clearPrescription()
  }
}
