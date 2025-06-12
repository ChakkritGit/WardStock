package com.thanes.wardstock.data.models

data class OrderModel(
  val id: String,
  val hn: String,
  val patientName: String,
  val wardCode: String?,
  val wardDesc: String?,
  val priorityCode: String?,
  val priorityDesc: String?,
  val status: String,
  val comment: String?,
  val createdAt: String,
  val updatedAt: String,
  val order: List<OrderItem>
)

data class OrderItem(
  val id: String,
  val prescriptionId: String,
  val drugId: String,
  val drugName: String,
  val qty: Int,
  val unit: String,
  val drugLot: String,
  val drugExpire: String,
  val drugPriority: Int,
  val position: Int,
  val machineId: String,
  val status: String,
  val comment: String?,
  val createdAt: String,
  val updatedAt: String
)
