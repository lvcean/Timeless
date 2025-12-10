package com.example.myapplication.data.model

/**
 * 发送验证码请求
 */
data class SendCodeRequest(
    val phone: String
)

/**
 * 登录请求
 */
data class LoginRequest(
    val phone: String,
    val code: String
)
