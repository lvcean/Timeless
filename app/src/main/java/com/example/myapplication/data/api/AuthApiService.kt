package com.example.myapplication.data.api

import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.SendCodeRequest
import com.example.myapplication.data.model.SendCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 认证API服务接口
 */
interface AuthApiService {
    /**
     * 发送验证码
     */
    @POST("api/auth/send-code")
    suspend fun sendVerificationCode(
        @Body request: SendCodeRequest
    ): Response<SendCodeResponse>
    
    /**
     * 验证码登录
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
