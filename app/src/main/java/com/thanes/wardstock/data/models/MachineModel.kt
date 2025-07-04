package com.thanes.wardstock.data.models

data class MachineModel(
  val id: String,
  val machineName: String,
  val location: String,
  val capacity: Int,
  val status: Boolean,
  val comment: String?,
  val createdAt: String,
  val updatedAt: String
)