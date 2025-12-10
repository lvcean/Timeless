package com.example.myapplication.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

/**
 * 音效管理器
 * 使用系统音效和震动反馈提升用户体验
 */
class SoundEffectManager(private val context: Context) {
    
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 播放成功音效（打卡成功、保存成功等）
     */
    fun playSuccess() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            vibrateLight()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 播放点击音效
     */
    fun playClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
            vibrateVeryLight()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 播放删除音效
     */
    fun playDelete() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            vibrateMedium()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 播放错误音效
     */
    fun playError() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 300)
            vibrateStrong()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 播放完成音效（连续打卡、达成目标等）
     */
    fun playAchievement() {
        try {
            // 播放两次短音
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
            Thread.sleep(150)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
            vibratePattern()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 轻微震动（点击反馈）
     */
    private fun vibrateVeryLight() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 轻度震动（成功反馈）
     */
    private fun vibrateLight() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 中度震动（删除反馈）
     */
    private fun vibrateMedium() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(50, 128)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 强烈震动（错误反馈）
     */
    private fun vibrateStrong() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 震动模式（成就反馈）
     */
    private fun vibratePattern() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 50, 50, 50)
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 50, 50, 50)
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 释放资源
     */
    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        @Volatile
        private var instance: SoundEffectManager? = null
        
        fun getInstance(context: Context): SoundEffectManager {
            return instance ?: synchronized(this) {
                instance ?: SoundEffectManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
