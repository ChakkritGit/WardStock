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
  val updatedAt: String,
  val biometrics: List<UserFingerprint>?
)

data class UserFingerprint(
  val id: String,
  val userId: String,
  val type: String,
  val description: String,
  val createdAt: String
)

enum class UserRole {
  SUPER, SERVICE, PHARMACIST, PHARMACIST_ASSISTANCE, HEAD_NURSE, NURSE
}