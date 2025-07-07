package com.thanes.wardstock.data.models

data class RefillModel(
  val inventoryId: String,
  val inventoryPosition: Int,
  val inventoryQty: Int,
  val inventoryMin: Int,
  val inventoryMAX: Int,
  val inventoryStatus: Boolean,
  val drugId: String?,
  val drugName: String?,
  val drugUnit: String?,
  val drugImage: String?,
  val drugPriority: Int?,
  val drugExpire: String
)

data class RefillDrugModel(
  val id: String,
  val position: Int,
  val qty: Int,
  val min: Int,
  val max: Int,
  val status: Boolean,
  val machineId: String,
  val comment: String?,
  val createdAt: String,
  val updatedAt: String
)