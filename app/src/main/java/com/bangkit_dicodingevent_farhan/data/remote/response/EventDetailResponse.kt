package com.bangkit_dicodingevent_farhan.data.remote.response

import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity

data class EventDetailResponse(
    val error: Boolean,
    val event: EventEntity
)