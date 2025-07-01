package com.thanes.wardstock.data.models

data class InventoryModel(
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

data class InventoryExitsModel(
  val inventoryId: String
)