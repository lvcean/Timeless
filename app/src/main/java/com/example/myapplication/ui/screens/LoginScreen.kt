package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.PremiumTextField
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.LoginState

// Mint Green Theme Colors
val MintGreen = Color(0xFF52B788)
val MintLight = Color(0xFFE8F5E9)

/**
 * 登录界面 - Green Style & Guest Mode
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val loginState by authViewModel.loginState.collectAsState()
    val phone by authViewModel.phone.collectAsState()
    val code by authViewModel.code.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val countdown by authViewModel.countdown.collectAsState()
    
    // 监听登录成功
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
            authViewModel.resetState()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MintLight, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Updated Logo Area
            Icon(
                imageVector = Icons.Filled.DateRange, // Calendar Icon
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MintGreen
            )
            
            Text(
                text = "Timeless",
                style = MaterialTheme.typography.headlineLarge, // Fallback if displayMedium unavailable
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Cursive, // Cursive font for elegance
                color = MintGreen
            )
            
            Text(
                text = "Capture Every Moment",
                style = MaterialTheme.typography.bodyLarge,
                color = MintGreen.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    
                    // Styled Inputs using OutlinedTextField for consistent Green Style
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { authViewModel.onPhoneChange(it) },
                        label = { Text("邮箱地址") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MintGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            focusedLabelColor = MintGreen,
                            cursorColor = MintGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { authViewModel.onCodeChange(it) },
                            label = { Text("验证码") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintGreen,
                                focusedLabelColor = MintGreen,
                                cursorColor = MintGreen
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { authViewModel.sendVerificationCode() },
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = phone.contains("@") && countdown == 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintLight, 
                                contentColor = MintGreen
                            )
                        ) {
                            Text(if (countdown > 0) "${countdown}s" else "获验证码")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Login Button
                    Button(
                        onClick = { authViewModel.login() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = phone.isNotEmpty() && code.length >= 6 && loginState !is LoginState.Loading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGreen
                        )
                    ) {
                        if (loginState is LoginState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("登 录", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Guest Login Button
            TextButton(
                onClick = { authViewModel.loginAsGuest() },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("我是游客，直接体验 >", color = Color.Gray, fontSize = 16.sp)
            }
            
            // Error Message
            AnimatedVisibility(visible = errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
