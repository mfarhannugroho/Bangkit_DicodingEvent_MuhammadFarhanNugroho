package com.bangkit_dicodingevent_farhan.data.remote.response

import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity

data class EventResponse(
    val error: Boolean,
    val listEvents: List<EventEntity>
)