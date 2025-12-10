# Supabase Magic Link 集成指南

## 📋 概述

本文档详细说明如何将当前的Mock登录系统升级为使用Supabase Magic Link的真实云端认证系统。

---

## ✅ 已完成的准备工作

### 1. Supabase项目配置
- ✅ 项目已创建: `event-tracker`
- ✅ 数据表已创建: `Users1`
- ✅ 环境信息:
  ```
  Project URL: https://mwmmknlbeokrldsybsje.supabase.co
  API Key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```

### 2. Android依赖
- ✅ 已添加Supabase SDK到 `libs.versions.toml`
- ✅ 已添加依赖到 `app/build.gradle.kts`
- ✅ 已创建 `SupabaseClient.kt`

---

## 🚀 实施步骤

### 第1步: 修改LoginScreen UI (30分钟)

#### 文件: `LoginScreen.kt`

**需要修改的部分**:

1. **标题和描述**
```kotlin
// 第85-94行,修改为:
Text(
    text = "欢迎回来",
    style = MaterialTheme.typography.headlineLarge,
    fontWeight = FontWeight.Bold
)

Spacer(modifier = Modifier.height(8.dp))

Text(
    text = "使用邮箱登录您的账户",
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
)
```

2. **移除手机号输入,改为邮箱输入**
```kotlin
// 第110-180行,替换为:
Column(modifier = Modifier.fillMaxWidth()) {
    Text(
        text = "邮箱地址",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    PremiumTextField(
        value = email,
        onValueChange = { viewModel.onEmailChange(it) },
        placeholder = "your@email.com",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
```

3. **移除验证码输入和倒计时**
   - 删除第182-260行的验证码相关代码

4. **修改登录按钮**
```kotlin
// 第262-290行,修改为:
Button(
    onClick = { viewModel.sendMagicLink() },
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
    enabled = email.isNotEmpty() && !isLoading,
    shape = RoundedCornerShape(16.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
    } else {
        Text(
            text = "发送登录链接",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

Spacer(modifier = Modifier.height(16.dp))

Text(
    text = "我们将向您的邮箱发送一个登录链接",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth()
)
```

---

### 第2步: 修改AuthViewModel (30分钟)

#### 文件: `AuthViewModel.kt`

**完全重写为**:

```kotlin
package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    
    // UI状态
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    // 登录状态
    val isLoggedIn = repository.isLoggedIn()
    
    // 当前用户
    val currentUser = repository.getCurrentUser()
    
    init {
        // 初始化Token
        viewModelScope.launch {
            repository.initializeToken()
        }
    }
    
    /**
     * 邮箱输入变化
     */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
    }
    
    /**
     * 发送Magic Link
     */
    fun sendMagicLink() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            
            val result = repository.sendMagicLink(_email.value)
            
            result.fold(
                onSuccess = { message ->
                    _successMessage.value = message
                    _email.value = "" // 清空邮箱
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "发送失败"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    /**
     * 处理Magic Link回调
     */
    fun handleMagicLink(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = repository.handleMagicLinkCallback(token)
            
            result.fold(
                onSuccess = {
                    // 登录成功,导航会自动处理
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "登录失败"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
}
```

---

### 第3步: 修改AuthRepository (45分钟)

#### 文件: `AuthRepository.kt`

**完全重写为**:

```kotlin
package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.api.SupabaseClient
import com.example.myapplication.data.entity.UserEntity
import com.example.myapplication.util.TokenManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AuthRepository(private val context: Context) {
    private val tokenManager = TokenManager(context)
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val supabase = SupabaseClient.client
    
    /**
     * 发送Magic Link到邮箱
     */
    suspend fun sendMagicLink(email: String): Result<String> {
        return try {
            // 验证邮箱格式
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("邮箱格式错误"))
            }
            
            // 发送Magic Link
            supabase.auth.signInWith(Email) {
                this.email = email
                createUser = true // 如果用户不存在则创建
            }
            
            Result.success("登录链接已发送到您的邮箱,请查收!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 处理Magic Link回调
     */
    suspend fun handleMagicLinkCallback(token: String): Result<Unit> {
        return try {
            // Supabase会自动处理token
            val session = supabase.auth.currentSessionOrNull()
            
            if (session != null) {
                // 保存Token
                tokenManager.saveToken(session.accessToken)
                
                // 获取用户信息
                val user = supabase.auth.currentUserOrNull()
                
                if (user != null) {
                    // 保存用户ID
                    tokenManager.saveUserId(user.id)
                    
                    // 保存到本地数据库
                    val userEntity = UserEntity(
                        id = user.id,
                        phone = user.email ?: "", // 使用email作为phone字段
                        nickname = user.email?.substringBefore('@') ?: "用户",
                        avatar = "",
                        lastLoginAt = System.currentTimeMillis()
                    )
                    userDao.insertUser(userEntity)
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("登录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 登出
     */
    suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            // 忽略错误
        }
        
        tokenManager.clearAuth()
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
     * 初始化Token
     */
    suspend fun initializeToken() {
        val token = tokenManager.getToken().first()
        // Supabase会自动管理session
    }
}
```

---

### 第4步: 配置Deep Link (30分钟)

#### 4.1 修改AndroidManifest.xml

在`MainActivity`的`<activity>`标签内添加:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    
    <!-- 替换为您的包名 -->
    <data
        android:scheme="com.example.myapplication"
        android:host="login-callback" />
</intent-filter>
```

#### 4.2 修改MainActivity处理Deep Link

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 处理Deep Link
        handleDeepLink(intent)
        
        setContent {
            // ... 现有代码
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "com.example.myapplication") {
            // 提取token并处理
            val token = data.getQueryParameter("access_token")
            if (token != null) {
                // 通知ViewModel处理
                // authViewModel.handleMagicLink(token)
            }
        }
    }
}
```

---

### 第5步: 配置Supabase邮件设置 (15分钟)

#### 在Supabase控制台:

1. 进入 **Authentication** → **URL Configuration**

2. 设置 **Site URL**:
   ```
   com.example.myapplication://login-callback
   ```

3. 添加 **Redirect URLs**:
   ```
   com.example.myapplication://login-callback
   ```

4. 保存设置

---

## 🧪 测试流程

### 1. 发送Magic Link
1. 输入邮箱: `your@email.com`
2. 点击"发送登录链接"
3. 检查邮箱

### 2. 点击邮件中的链接
1. 在手机上打开邮件
2. 点击登录链接
3. 应该自动打开App并登录

### 3. 验证登录状态
1. 关闭App
2. 重新打开
3. 应该保持登录状态

---

## 📊 数据同步(可选)

如果需要将事件数据同步到Supabase:

### 1. 在Supabase创建events表

```sql
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id),
    name TEXT NOT NULL,
    icon TEXT,
    color TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 启用RLS
ALTER TABLE events ENABLE ROW LEVEL SECURITY;

-- 创建策略:用户只能访问自己的数据
CREATE POLICY "Users can only access their own events"
ON events
FOR ALL
USING (auth.uid() = user_id);
```

### 2. 修改EventRepository

添加Supabase同步逻辑...

---

## ⚠️ 注意事项

1. **邮件发送限制**
   - Supabase免费版有邮件发送限制
   - 建议配置自定义SMTP

2. **Deep Link测试**
   - 需要在真机上测试
   - 模拟器可能无法正确处理

3. **Token管理**
   - Supabase会自动刷新token
   - 需要处理token过期情况

---

## 🎯 总结

完成以上步骤后,您将拥有:
- ✅ 真实的邮箱登录
- ✅ Magic Link无密码认证
- ✅ 云端用户管理
- ✅ Token自动刷新
- ✅ 多设备同步(可选)

预计总时间: **2-3小时**

---

## 📝 备注

- 当前Mock系统已经完全可用
- Supabase集成是可选的升级
- 可以随时回退到Mock系统
- 建议在新分支上进行集成

---

**创建日期**: 2025-12-08
**文档版本**: 1.0
