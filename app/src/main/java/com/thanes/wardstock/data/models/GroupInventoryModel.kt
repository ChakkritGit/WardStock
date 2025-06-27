package com.thanes.wardstock.data.models

data class GroupInventoryModel(
  val groupid: String,
  val drugid: String,
  val drugname: String,
  val drugimage: String,
  val drugpriority: Int,
  val drugunit: String,
  val groupmin: Int,
  val groupmax: Int,
  val inventoryList: List<InventoryItem>
)

data class InventoryItem(
  val inventoryId: String,
  val inventoryPosition: Int,
  val inventoryQty: Int
)
