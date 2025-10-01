package com.example.wear.service

import com.example.wear.util.NetworkConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {
    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var retrofit: Retrofit? = null

    fun api(): ApiService {
        val current = retrofit
        if (current != null) return current.create(ApiService::class.java)

        synchronized(this) {
            val again = retrofit
            if (again != null) return again.create(ApiService::class.java)

            val built = Retrofit.Builder()
                .baseUrl(NetworkConfig.baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit = built
            return built.create(ApiService::class.java)
        }
    }

    fun setBaseUrl(url: String) {
        NetworkConfig.baseUrl = url
        // invalidate retrofit so it will rebuild with new base url
        retrofit = null
    }
}
