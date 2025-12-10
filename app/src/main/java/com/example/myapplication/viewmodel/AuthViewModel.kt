package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 登录状态密封类
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AuthRepository(application)
    
    // UI状态
    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()
    
    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 倒计时状态
    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()
    private var countdownJob: Job? = null
    
    // 登录状态检查
    val isLoggedIn = repository.isLoggedIn()
    
    // 当前用户
    val currentUser = repository.getCurrentUser()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            repository.initializeToken()
        }
    }
    
    fun onPhoneChange(newPhone: String) {
        // 允许邮箱输入，并进行宽松的邮箱格式检查
        _phone.value = newPhone
        _errorMessage.value = null // Clear error on input change
    }
    
    fun onCodeChange(newCode: String) {
        // 允许 6 到 8 位验证码 (Supabase 默认为 6，用户可能配置了 8)
        if (newCode.all { it.isDigit() } && newCode.length <= 8) {
            _code.value = newCode
            _errorMessage.value = null // Clear error on input change
        }
    }
    
    fun sendVerificationCode() {
        val email = _phone.value
        // 宽松的邮箱格式检查，只要包含 "@" 且长度大于3即可
        val isValidEmail = email.contains("@") && email.length > 3
        if (!isValidEmail) {
            _errorMessage.value = "请输入有效的邮箱地址"
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            val result = repository.sendVerificationCode(email)
            
            result.fold(
                onSuccess = { message ->
                    _loginState.value = LoginState.Idle
                    _errorMessage.value = message // Display success message or instruction
                    startCountdown()
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Idle
                    _errorMessage.value = error.message
                }
            )
        }
    }
    
    fun login() {
        val email = _phone.value
        val code = _code.value
        
        // 验证码长度检查：支持 6 位或 8 位
        if (code.length < 6) {
            _errorMessage.value = "请输入完整的验证码"
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            val result = repository.login(email, code)
            
            result.fold(
                onSuccess = { response ->
                    _loginState.value = LoginState.Success(response)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "登录失败")
                    _errorMessage.value = error.message
                }
            )
        }
    }
    
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _countdown.value = 60
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value -= 1
            }
        }
    }
    
    fun loginAsGuest() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            delay(500) // Simulate loading
            
            val result = repository.loginAsGuest()
            
            result.fold(
                onSuccess = { response ->
                    _loginState.value = LoginState.Success(response)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "游客登录失败")
                    _errorMessage.value = error.message
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _phone.value = ""
            _code.value = ""
            _loginState.value = LoginState.Idle
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState.Idle
        _errorMessage.value = null
    }
}
