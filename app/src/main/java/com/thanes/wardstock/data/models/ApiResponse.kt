package com.thanes.wardstock.data.models

data class ApiResponse<T>(
  val message: String,
  val success: Boolean,
  val data: T
)