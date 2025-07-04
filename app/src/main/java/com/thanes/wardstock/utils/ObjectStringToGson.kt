package com.thanes.wardstock.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

inline fun <reified T> objectStringToGson(message: String): T? {
  val gson = Gson()

  return try {
    gson.fromJson(message, T::class.java)
  } catch (e: JsonSyntaxException) {
    Log.e("RabbitMQ", "Invalid JSON format: ${e.message}")
    null
  }
}