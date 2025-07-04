package com.thanes.wardstock.data.models

data class RabbitOrderMessage(
  val id: String,
  val presId: String,
  val qty: Int,
  val position: Int,
  val priority: Int
)