package com.thanes.wardstock.utils

import android.util.Log
import org.json.JSONObject

fun parseErrorMessage(responseCode: Int, errorJson: String?): String {
  return try {
    JSONObject(errorJson ?: "").getString("message")
  } catch (_: Exception) {
    when (responseCode) {
      400 -> "Invalid request data"
      401 -> "Authentication required"
      403 -> "Access denied"
      404 -> "Prescription not found"
      500 -> "Server error, please try again later"
      else -> "HTTP Error $responseCode"
    }
  }
}

fun parseExceptionMessage(e: Exception): String {
  return when (e) {
    is java.net.UnknownHostException -> "No internet connection"
    is java.net.SocketTimeoutException -> "Request timeout, please try again"
    is java.net.ConnectException -> "Unable to connect to server"
    is javax.net.ssl.SSLException -> "Secure connection failed"
    is com.google.gson.JsonSyntaxException -> "Invalid response format"
    is java.io.IOException -> "Network error occurred"
    else -> {
      Log.e("OrderAPI", "Unexpected error: ${e.javaClass.simpleName}", e)
      "Unexpected error occurred"
    }
  }
}
