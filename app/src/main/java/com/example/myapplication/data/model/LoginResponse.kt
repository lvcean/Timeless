package com.example.myapplication.data.model

/**
 * 用户信息
 */
data class UserInfo(
    val id: String,
    val phone: String,
    val nickname: String? = null,
    val avatar: String? = null
)

/**
 * 登录响应
 */
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val user: UserInfo? = null,
    val message: String? = null
)

/**
 * 发送验证码响应
 */
data class SendCodeResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * 通用API响应
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)
