package com.bangkit_dicodingevent_farhan.data.repository

import com.bangkit_dicodingevent_farhan.data.local.database.EventDao
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bangkit_dicodingevent_farhan.data.remote.response.EventDetailResponse
import com.bangkit_dicodingevent_farhan.data.remote.response.EventResponse
import com.bangkit_dicodingevent_farhan.data.remote.retrofit.ApiService
import retrofit2.Response

class EventRepository(
    private val eventDao: EventDao,
    private val apiService: ApiService
) {
    suspend fun insert(event: EventEntity) {
        eventDao.insert(event)
    }

    // Remote API operations
    suspend fun getUpcomingEvents(): Response<EventResponse> {
        return apiService.getEvents(1)
    }

    suspend fun getFinishedEvents(): Response<EventResponse> {
        return apiService.getEvents(0)
    }

    suspend fun getEventDetail(id: Int): Response<EventDetailResponse> {
        return apiService.getEventDetail(id)
    }

    suspend fun searchFinishedEvents(query: String): Response<EventResponse> {
        return apiService.searchEvents(query, 0)
    }
}