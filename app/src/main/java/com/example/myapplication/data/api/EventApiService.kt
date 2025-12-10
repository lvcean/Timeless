package com.example.myapplication.data.api

import com.example.myapplication.data.model.NetworkEvent
import com.example.myapplication.model.Event
import retrofit2.Response
import retrofit2.http.*

/**
 * 事件API服务接口
 */
interface EventApiService {

    @GET("events?select=*&order=created_at.desc")
    suspend fun getEvents(): Response<List<NetworkEvent>>

    @POST("events")
    suspend fun createEvent(@Body event: NetworkEvent): Response<List<NetworkEvent>>

    // 修正：调用时应该传 "eq.uuid" 而不是 "id=eq.uuid" 导致重复 id 参数
    // Retrofit: @Query("id") 会自动拼成 ?id=...
    // 所以调用方只需要传 "eq.123"
    @DELETE("events")
    suspend fun deleteEvent(@Query("id") idNavigator: String): Response<Unit>

    // --- 打卡记录相关 ---

    @POST("event_records")
    suspend fun createRecord(@Body record: com.example.myapplication.data.model.NetworkRecord): Response<List<com.example.myapplication.data.model.NetworkRecord>>

    // 根据 event_id 和 timestamp 删除记录 (Demo简化做法，通常应该用 record_id)
    @DELETE("event_records")
    suspend fun deleteRecord(
        @Query("event_id") eventId: String,
        @Query("timestamp") timestamp: String
    ): Response<Unit>
}

// Helper mainly for UI logic if needed, but Retrofit will return List<NetworkEvent> directly
// Removal of CreateEventResponse since Supabase returns raw list
// Removal of DeleteEventResponse
