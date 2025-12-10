package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.entity.UserEntity
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserInfo
import android.util.Log
import com.example.myapplication.data.SupabaseModule
import com.example.myapplication.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import io.github.jan.supabase.gotrue.*
import io.github.jan.supabase.gotrue.providers.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import io.github.jan.supabase.gotrue.providers.builtin.Email

/**
 * 认证仓库 - Mock实现版本
 */
class AuthRepository(private val context: Context) {
    private val tokenManager = TokenManager(context)
    private val userDao = AppDatabase.getDatabase(context).userDao()
    
    // 开关：控制是否使用Mock数据. 
    // TODO: 后端服务准备好后，将此值改为 false
    private val USE_MOCK = false

    /**
     * 发送验证码 (Email OTP)
     * @param phone 这里实际上传入的是邮箱地址 (UI层输入的是邮箱)
     */
    suspend fun sendVerificationCode(phone: String): Result<String> {
        val email = phone // 这里实际上传入的是邮箱
        return try {
            Log.d("AuthRepository", "Sending verification code to $email via OTP...")
            // 使用 OTP 提供者发送验证码 (Magic Link / Code)
            // 关键修正：从 Email 改为 OTP，并开启 createUser = true 以支持新用户注册
            com.example.myapplication.data.SupabaseModule.client.auth.signInWith(
                io.github.jan.supabase.gotrue.providers.builtin.OTP
            ) {
                this.email = email  
                this.createUser = true // 允许新用户注册
            }
            Log.d("AuthRepository", "OTP sent successfully.")
            Result.success("验证码已发送至 $email")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to send OTP", e)
            Result.failure(Exception("发送失败: ${e.message}"))
        }
    }

    /**
     * 验证码登录 (Email OTP Verification)
     * @param phone 邮箱地址
     * @param code 用户收到的6位验证码
     */
    suspend fun login(email: String, code: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Verifying Code (Direct HTTP): $email, Code: $code")
                
                val url = URL("https://mwmmknlbeokrldsybsje.supabase.co/auth/v1/verify")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                // Using the specific API Key from RetrofitClient (Assuming it's safe to use the one we saw earlier for this demo fix)
                connection.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im13bW1rbmxiZW9rcmxkc3lic2plIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxODE1MTEsImV4cCI6MjA4MDc1NzUxMX0.HxoClrQpexO1nvXTNJ5OvYhHyWCKl6S7x1sEMHZjMCg")
                connection.doOutput = true

                val jsonBody = """
                    {
                        "type": "email",
                        "email": "$email",
                        "token": "$code"
                    }
                """.trimIndent()

                connection.outputStream.use { os ->
                    val input = jsonBody.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                Log.d("AuthRepository", "Verify API Response Code: $responseCode")

                if (responseCode in 200..299) {
// Add import at top: import org.json.JSONObject

                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("AuthRepository", "Verify Response: $responseBody")

                    // Use JSONObject for robust parsing
                    val jsonObject = org.json.JSONObject(responseBody)
                    val accessToken = jsonObject.optString("access_token", null) 
                                      ?: jsonObject.optJSONObject("session")?.optString("access_token")
                    
                    val userObj = jsonObject.optJSONObject("user")
                    val userId = userObj?.optString("id") ?: jsonObject.optString("id", null) 

                    if (!accessToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                        // 登录成功 - 保存状态
                        tokenManager.saveToken(accessToken)
                        tokenManager.saveUserId(userId)
                        RetrofitClient.setToken(accessToken)
                        RetrofitClient.userId = userId
                        
                        // 保存到本地数据库
                        val user = UserEntity(
                            id = userId,
                            phone = email,
                            nickname = "User_${email.substringBefore("@")}",
                            avatar = "",
                            lastLoginAt = System.currentTimeMillis()
                        )
                        userDao.insertUser(user)

                         Result.success(
                            LoginResponse(
                                success = true,
                                token = accessToken,
                                user = UserInfo(
                                    id = userId,
                                    phone = email,
                                    nickname = user.nickname,
                                    avatar = ""
                                ),
                                message = "登录成功"
                            )
                        )
                    } else {
                        Result.failure(Exception("解析登录响应失败: Token or UID missing"))
                    }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    Log.e("AuthRepository", "Verify Failed: $errorBody")
                    Result.failure(Exception("验证失败: $errorBody"))
                }

            } catch (e: Exception) {
                Log.e("AuthRepository", "Login Error", e)
                Result.failure(e)
            }
        }
    }

    // Helper methods removed (replaced by JSONObject)

    // Mock implementation removed
    private suspend fun mockSendVerificationCode(phone: String): Result<String> { return Result.success("") }
    private suspend fun mockLogin(phone: String, code: String): Result<LoginResponse> { return Result.success(LoginResponse(true, "", null, "")) }
    
    /**
     * 游客登录 (Guest Login)
     * 创建一个本地的 Guest 用户，不进行网络验证
     */
    suspend fun loginAsGuest(): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // 定义 Guest 用户信息
                val guestId = "guest_user"
                val guestToken = "guest_token_placeholder"
                val guestEmail = "guest@local"
                
                // 保存状态
                tokenManager.saveToken(guestToken)
                tokenManager.saveUserId(guestId)
                RetrofitClient.setToken(guestToken)
                RetrofitClient.userId = guestId
                
                // 保存到本地数据库
                val user = UserEntity(
                    id = guestId,
                    phone = guestEmail,
                    nickname = "游客",
                    avatar = "",
                    lastLoginAt = System.currentTimeMillis()
                )
                userDao.insertUser(user)

                Result.success(
                    LoginResponse(
                        success = true,
                        token = guestToken,
                        user = UserInfo(
                            id = guestId,
                            phone = guestEmail,
                            nickname = user.nickname,
                            avatar = ""
                        ),
                        message = "游客登录成功"
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 登出
     */
    suspend fun logout() {
        tokenManager.clearAuth()
        RetrofitClient.setToken(null)
        userDao.deleteAllUsers()
    }
    
    /**
     * 获取当前用户
     */
    fun getCurrentUser(): Flow<UserEntity?> {
        return userDao.getCurrentUser()
    }
    
    /**
     * 检查登录状态
     */
    fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.isLoggedIn()
    }
    
    /**
     * 初始化Token (应用启动时调用)
     */
    suspend fun initializeToken() {
        tokenManager.getToken().first()?.let { token ->
            RetrofitClient.setToken(token)
        }
        tokenManager.getUserId().first()?.let { uid ->
            RetrofitClient.userId = uid
        }
    }
}
