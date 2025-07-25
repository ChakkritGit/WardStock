package com.thanes.wardstock.remote.api.services

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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class LoginRequest(val username: String, val password: String)
data class LoginVeinRequest(val uid: String)
data class AddDrugRequest(val inventoryQty: Int)
data class MachineRequest(
  val machineName: String,
  val location: String,
  val capacity: Int,
  val status: Boolean,
  val comment: String
)
data class FingerprintRequest(
  val description: String
)
data class AddFingerprintRequest(
  val userId: String,
  val featureData: String,
  val description: String?
)
data class InventoryRequest(
  val position: Int,
  val min: Int,
  val max: Int,
  val machineId: String,
  val status: Boolean,
  val comment: String
)

data class GroupInventoryRequest(
  val drugId: String,
  val groupMin: Int,
  val groupMax: Int,
  val inventories: List<InventoryItem>
)

interface ApiService {
  @POST("auth/login")
  suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserData>>

  @POST("auth/login/vein")
  suspend fun loginWithVein(@Body request: LoginVeinRequest): Response<ApiResponse<UserData>>

  @POST("auth/verify-drug")
  suspend fun verifyUser(@Body request: LoginRequest): Response<ApiResponse<String?>>

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
  suspend fun createUser(
    @Part image: MultipartBody.Part,
    @Part("username") username: RequestBody,
    @Part("password") password: RequestBody,
    @Part("display") display: RequestBody,
    @Part("role") role: RequestBody,
    @Part("biometrics") biometrics: RequestBody?
  ): Response<ApiResponse<UserModel>>

  @Multipart
  @PATCH("users/{userId}")
  suspend fun updatedUser(
    @Path("userId") userId: String,
    @Part image: MultipartBody.Part?,
    @Part("username") username: RequestBody,
    @Part("display") display: RequestBody,
    @Part("role") role: RequestBody
  ): Response<ApiResponse<UserModel>>

  @DELETE("users/{userId}")
  suspend fun removeUser(@Path("userId") userId: String): Response<ApiResponse<String>>

  @GET("drugs")
  suspend fun getDrug(): Response<ApiResponse<List<DrugModel>>>

  @GET("drugs/exist")
  suspend fun getDrugExits(): Response<ApiResponse<List<DrugExitsModel>>>

  @Multipart
  @POST("drugs")
  suspend fun createDrug(
    @Part image: MultipartBody.Part,
    @Part("drugCode") drugCode: RequestBody,
    @Part("drugName") drugName: RequestBody,
    @Part("unit") unit: RequestBody,
    @Part("weight") weight: RequestBody,
    @Part("drugLot") drugLot: RequestBody,
    @Part("drugExpire") drugExpire: RequestBody,
    @Part("drugPriority") drugPriority: RequestBody,
    @Part("drugStatus") drugStatus: RequestBody,
    @Part("comment") comment: RequestBody?
  ): Response<ApiResponse<DrugModel>>

  @Multipart
  @PATCH("drugs/{drugId}")
  suspend fun updateDrug(
    @Path("drugId") drugId: String,
    @Part image: MultipartBody.Part?,
    @Part("drugCode") drugCode: RequestBody,
    @Part("drugName") drugName: RequestBody,
    @Part("unit") unit: RequestBody,
    @Part("weight") weight: RequestBody,
    @Part("drugLot") drugLot: RequestBody,
    @Part("drugExpire") drugExpire: RequestBody,
    @Part("drugPriority") drugPriority: RequestBody,
    @Part("status") drugStatus: RequestBody,
    @Part("comment") comment: RequestBody?
  ): Response<ApiResponse<DrugModel>>

  @DELETE("drugs/{drugId}")
  suspend fun removeDrug(@Path("drugId") drugId: String): Response<ApiResponse<String>>

  @GET("machine")
  suspend fun getMachine(): Response<ApiResponse<List<MachineModel>>>

  @POST("machine")
  suspend fun createMachine(@Body request: MachineRequest): Response<ApiResponse<MachineModel>>

  @PATCH("machine/{machineId}")
  suspend fun updateMachine(@Path("machineId") machineId: String, @Body request: MachineRequest): Response<ApiResponse<MachineModel>>

  @DELETE("machine/{machineId}")
  suspend fun removeMachine(@Path("machineId") machineId: String): Response<ApiResponse<String>>

  @GET("inventory")
  suspend fun getInventory(): Response<ApiResponse<List<InventoryModel>>>

  @GET("inventory/exist")
  suspend fun getInventoryExits(): Response<ApiResponse<List<InventoryExitsModel>>>

  @GET("group-inventory")
  suspend fun getGroupInventory(): Response<ApiResponse<List<GroupInventoryModel>>>

  @POST("inventory")
  suspend fun createInventory(@Body request: InventoryRequest): Response<ApiResponse<InventoryModel>>

  @PATCH("inventory/{inventoryId}")
  suspend fun updateInventory(@Path("inventoryId") inventoryId: String, @Body request: InventoryRequest): Response<ApiResponse<InventoryModel>>

  @DELETE("inventory/{inventoryId}")
  suspend fun removeInventory(@Path("inventoryId") inventoryId: String): Response<ApiResponse<String>>

  @DELETE("group-inventory/{groupId}")
  suspend fun removeGroup(@Path("groupId") groupId: String): Response<ApiResponse<String>>

  @POST("group-inventory")
  suspend fun createGroup(@Body request: GroupInventoryRequest): Response<ApiResponse<String>>

  @PATCH("group-inventory/{groupId}")
  suspend fun updateGroup(@Path("groupId") groupId: String, @Body request: GroupInventoryRequest): Response<ApiResponse<String>>

  @GET("dispense/order/status/pending/{orderId}/{prescriptionId}")
  suspend fun updateOrderToPending(
    @Path("orderId") orderId: String,
    @Path("prescriptionId") prescriptionId: String
  ): Response<ApiResponse<String>>

  @GET("dispense/order/status/receive/{orderId}/{prescriptionId}")
  suspend fun updateOrderToReceive(
    @Path("orderId") orderId: String,
    @Path("prescriptionId") prescriptionId: String
  ): Response<ApiResponse<String>>

  @GET("dispense/order/status/complete/{orderId}/{prescriptionId}")
  suspend fun updateOrderToComplete(
    @Path("orderId") orderId: String,
    @Path("prescriptionId") prescriptionId: String
  ): Response<ApiResponse<String>>

  @GET("dispense/order/status/error/{orderId}/{prescriptionId}")
  suspend fun updateOrderToError(
    @Path("orderId") orderId: String,
    @Path("prescriptionId") prescriptionId: String
  ): Response<ApiResponse<String>>

  @GET("reports/below-min-max")
  suspend fun getReportAlertMinMax(): Response<ApiResponse<List<InventoryMinMax>>>

  @GET("config/user")
  suspend fun getUserConfig(): Response<ApiResponse<List<BiometricLoadModel>>>

  @GET("config/fingerprint/{userId}")
  suspend fun getUserFingerprint(@Path("userId") userId: String): Response<ApiResponse<List<UserFingerprint>>>

  @PATCH("config/fingerprint/{bioId}")
  suspend fun updateFingerprint(
    @Path("bioId") bioId: String,
    @Body request: FingerprintRequest
  ): Response<ApiResponse<String>>

  @DELETE("config/fingerprint/{bioId}")
  suspend fun deleteFingerprint(@Path("bioId") bioId: String): Response<ApiResponse<String>>

  @POST("config/fingerprint")
  suspend fun addFingerprint(@Body request: AddFingerprintRequest): Response<ApiResponse<String>>
}