package com.thanes.wardstock.data.models

data class MachineStatus(
  val temperature: String = "--.- °C",
  val humidity: String = "--.- %",
  val isTempOk: Boolean = false,
  val isHumidityOk: Boolean = false,
  val lastUpdated: Long = 0L
)