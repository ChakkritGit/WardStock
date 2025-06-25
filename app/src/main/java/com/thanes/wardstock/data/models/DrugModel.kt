package com.thanes.wardstock.data.models

data class DrugModel(
  val id: String,
  val drugCode: String,
  val drugName: String,
  val unit: String,
  val drugLot: String,
  val drugExpire: String,
  val drugPriority: Int,
  val weight: Int,
  val status: Boolean,
  val picture: String,
  val comment: String,
  val createdAt: String,
  val updatedAt: String
)