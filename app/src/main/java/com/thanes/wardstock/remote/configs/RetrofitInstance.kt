package com.thanes.wardstock.remote.configs

import android.util.Log
import com.thanes.wardstock.data.viewModel.TokenHolder
import com.thanes.wardstock.remote.api.services.ApiService
import com.thanes.wardstock.utils.BaseURL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
  val api: ApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BaseURL)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }

  fun createApiWithAuth(): ApiService {
    val client = OkHttpClient.Builder()
      .addInterceptor(AuthInterceptor())
      .build()

    return Retrofit.Builder()
      .baseUrl(BaseURL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(ApiService::class.java)
  }
}

class AuthInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val token = TokenHolder.token.orEmpty()

    val request = if (token.isNotEmpty()) {
      chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    } else {
      chain.request()
    }

    return chain.proceed(request)
  }
}
