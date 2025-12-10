package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class NetworkEvent(
    val id: String? = null,
    @SerializedName("name") val name: String,
    val icon: String,
    @SerializedName("background_color") val backgroundColor: String,
    val category: String,
    // val attributes: Any? = null, 
    // @SerializedName("start_time") val startTime: String? = null,
    // @SerializedName("end_time") val endTime: String? = null,
    // val location: String? = null,
    @SerializedName("creator_id") val creatorId: String? = null
)

/**
 * 网络传输用的打卡记录模型
 */
data class NetworkRecord(
    val id: Long? = null,
    @SerializedName("event_id") val eventId: String,
    val timestamp: Long, // 建议后端存 bigint 或 timestamptz
    val note: String,
    @SerializedName("attributes_data") val attributesData: String?
)
