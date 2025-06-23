package com.thanes.wardstock.data.models

data class UserModel(
  val id: String,
  val username: String,
  val display: String,
  val picture: String,
  val role: UserRole?,
  val status: Boolean,
  val comment: String?,
  val createdAt: String,
  val updatedAt: String
)

enum class UserRole {
  SUPER, SERVICE, PHARMACIST, PHARMACIST_ASSISTANCE, HEAD_NURSE, NURSE
}