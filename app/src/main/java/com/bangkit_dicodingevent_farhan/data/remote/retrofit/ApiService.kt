package com.bangkit_dicodingevent_farhan.data.remote.retrofit

import com.bangkit_dicodingevent_farhan.data.remote.response.EventDetailResponse
import com.bangkit_dicodingevent_farhan.data.remote.response.EventResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("events")
    suspend fun getEvents(@Query("active") active: Int): Response<EventResponse>

    @GET("events/{id}")
    suspend fun getEventDetail(@Path("id") id: Int): Response<EventDetailResponse>

    @GET("events")
    suspend fun searchEvents(
        @Query("q") query: String,
        @Query("active") active: Int
    ): Response<EventResponse>
}