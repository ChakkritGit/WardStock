package com.thanes.wardstock.data.models

import com.thanes.wardstock.screens.manage.user.BiometricInfo

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
  val biometrics: List<BiometricInfo>?
)

enum class UserRole {
  SUPER, SERVICE, PHARMACIST, PHARMACIST_ASSISTANCE, HEAD_NURSE, NURSE
}