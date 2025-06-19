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
  val drugPriority: Int?
)