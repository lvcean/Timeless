package com.example.myapplication.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端单例
 */
object RetrofitClient {
    // Supabase Configuration
    private const val BASE_URL = "https://mwmmknlbeokrldsybsje.supabase.co/rest/v1/"
    private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im13bW1rbmxiZW9rcmxkc3lic2plIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxODE1MTEsImV4cCI6MjA4MDc1NzUxMX0.HxoClrQpexO1nvXTNJ5OvYhHyWCKl6S7x1sEMHZjMCg"
    
    private var token: String? = null
    var userId: String? = null
    
    /**
     * 设置认证Token
     */
    fun setToken(newToken: String?) {
        token = newToken
    }
    
    /**
     * Token拦截器 - 自动添加 Supabase Headers
     */
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val builder = request.newBuilder()
            .addHeader("apikey", API_KEY)
            .addHeader("Prefer", "return=representation") // Important for created object return
        
        // 如果有登录Token，用登录Token；否则用Anon Key (用于公开访问)
        if (token != null) {
            builder.addHeader("Authorization", "Bearer $token")
        } else {
            builder.addHeader("Authorization", "Bearer $API_KEY")
        }
        
        chain.proceed(builder.build())
    }
    
    /**
     * 日志拦截器
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * OkHttp客户端
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Retrofit实例
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /**
     * 认证API服务
     */
    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)

    /**
     * 事件API服务
     */
    val eventApi: EventApiService = retrofit.create(EventApiService::class.java)
}
