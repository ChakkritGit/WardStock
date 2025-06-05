package com.thanes.wardstock.data.store

import android.content.Context
import com.google.gson.Gson
import com.thanes.wardstock.data.models.UserData
import androidx.core.content.edit

object DataManager {
  private const val PREF_NAME = "user_prefs"
  private const val TOKEN_KEY = "auth_token"
  private const val USER_DATA = "user_data"

  fun saveToken(context: Context, token: String) {
    val prefs = context.getSharedPreferences(
      PREF_NAME,
      Context.MODE_PRIVATE
    )
    prefs.edit { putString(TOKEN_KEY, token) }
  }

  fun saveUserData(context: Context, userData: UserData) {
    val prefs = context.getSharedPreferences(
      PREF_NAME,
      Context.MODE_PRIVATE
    )
    val jsonString = Gson().toJson(userData)
    prefs.edit {
      putString(USER_DATA, jsonString)
    }
  }

  fun getToken(context: Context): String {
    val prefs = context.getSharedPreferences(
      PREF_NAME,
      Context.MODE_PRIVATE
    )
    return prefs.getString(TOKEN_KEY, "") ?: ""
  }

  fun getUserData(context: Context): UserData? {
    val prefs = context.getSharedPreferences(
      PREF_NAME,
      Context.MODE_PRIVATE
    )
    val jsonString = prefs.getString(USER_DATA, null)

    return try {
      jsonString?.let {
        Gson().fromJson(it, UserData::class.java)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

//    suspend fun clearToken(context: Context) {
//        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        prefs.edit().remove(TOKEN_KEY).apply()
//    }

  fun clearAll(context: Context) {
    val prefs = context.getSharedPreferences(
      PREF_NAME,
      Context.MODE_PRIVATE
    )
    prefs.edit { clear() }
  }
}