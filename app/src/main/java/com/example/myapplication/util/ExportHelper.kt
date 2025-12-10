package com.example.myapplication.util

import android.content.Context
import android.os.Environment
import com.example.myapplication.data.entity.EventRecordEntity
import com.example.myapplication.model.Event
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 数据导出工具类
 */
object ExportHelper {
    
    /**
     * 导出为 CSV 格式
     */
    suspend fun exportToCSV(
        context: Context,
        events: List<Event>,
        records: List<EventRecordEntity>
    ): Boolean {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            
            val fileName = "event_tracker_${System.currentTimeMillis()}.csv"
            val file = File(downloadsDir, fileName)
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val eventMap = events.associateBy { it.id }
            
            FileWriter(file).use { writer ->
                // BOM for Excel compatibility with UTF-8
                writer.write("\uFEFF")
                writer.append("记录ID,事件名称,事件分类,记录时间,备注\n")
                
                records.forEach { record ->
                    val event = eventMap[record.eventId]
                    val eventName = event?.name ?: "未知事件"
                    val category = event?.category?.displayName ?: "未知"
                    val timeStr = dateFormat.format(Date(record.timestamp))
                    val note = record.note.replace(",", "，") // 处理 CSV 中的逗号
                    
                    writer.append("${record.id},$eventName,$category,$timeStr,$note\n")
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 导出为 JSON 格式
     */
    suspend fun exportToJSON(
        context: Context,
        events: List<Event>,
        records: List<EventRecordEntity>
    ): Boolean {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            
            val fileName = "event_tracker_${System.currentTimeMillis()}.json"
            val file = File(downloadsDir, fileName)
            
            val data = mapOf(
                "exportTime" to System.currentTimeMillis(),
                "events" to events,
                "records" to records
            )
            
            val gson = Gson()
            FileWriter(file).use { writer ->
                gson.toJson(data, writer)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
