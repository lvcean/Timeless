package com.example.myapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.util.NotificationHelper

/**
 * 提醒后台任务
 */
class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString("eventId") ?: return Result.failure()
        val eventName = inputData.getString("eventName") ?: "事件"
        
        // 发送通知
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showNotification(eventId, eventName)
        
        // 如果需要每日重复，可以在这里调度下一次（或者在这之前就使用 PeriodicWorkRequest，但 WorkManager 的 Periodic 最小间隔 15 分钟且不精确）
        // 对于每日固定时间提醒，更好的方式是根据当前时间计算下一个目标时间，设定 OneTimeWorkRequest
        // 这里简化处理，假设由外部调度器负责重复调度，或者仅仅是单次触发
        
        return Result.success()
    }
}
