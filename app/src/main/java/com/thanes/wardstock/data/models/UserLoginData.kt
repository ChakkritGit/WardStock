package com.thanes.wardstock.data.models

data class UserData(
  val id: String,
  val username: String,
  val display: String,
  val picture: String,
  val status: Boolean,
  val role: String,
  val token: String
)