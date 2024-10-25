package com.bangkit_dicodingevent_farhan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bangkit_dicodingevent_farhan.data.remote.response.EventDetailResponse
import com.bangkit_dicodingevent_farhan.data.remote.response.EventResponse
import com.bangkit_dicodingevent_farhan.data.repository.EventRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    private val _upcomingEvents = MutableLiveData<List<EventEntity>>()
    val upcomingEvents: LiveData<List<EventEntity>> = _upcomingEvents

    private val _finishedEvents = MutableLiveData<List<EventEntity>>()
    val finishedEvents: LiveData<List<EventEntity>> = _finishedEvents

    private val _eventDetail = MutableLiveData<EventEntity>()
    val eventDetail: LiveData<EventEntity> = _eventDetail

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    private fun insert(event: EventEntity) {
        viewModelScope.launch {
            repository.insert(event)
        }
    }

    private suspend fun handleEventResponse(
        apiCall: suspend () -> Response<EventResponse>,
        onSuccess: (EventResponse) -> Unit
    ) {
        _isLoading.postValue(true)
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    onSuccess(it)
                } ?: run {
                    _error.postValue("Response body is null")
                }
            } else {
                _error.postValue("Failed to fetch data: ${response.message()}")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "An unknown error occurred")
        } finally {
            _isLoading.postValue(false)
        }
    }

    private suspend fun handleEventDetailResponse(
        apiCall: suspend () -> Response<EventDetailResponse>,
        onSuccess: (EventDetailResponse) -> Unit
    ) {
        _isLoading.postValue(true)
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    onSuccess(it)
                } ?: run {
                    _error.postValue("Response body is null")
                }
            } else {
                _error.postValue("Failed to fetch data: ${response.message()}")
            }
        } catch (e: Exception) {
            _error.postValue(e.message ?: "An unknown error occurred")
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun fetchUpcomingEvents() {
        viewModelScope.launch {
            handleEventResponse(
                apiCall = { repository.getUpcomingEvents() },
                onSuccess = { events ->
                    if (!events.error) {
                        _upcomingEvents.postValue(events.listEvents)
                        events.listEvents.forEach { insert(it) }
                    } else {
                        _error.postValue("Failed to fetch upcoming events")
                    }
                }
            )
        }
    }

    fun fetchFinishedEvents() {
        viewModelScope.launch {
            handleEventResponse(
                apiCall = { repository.getFinishedEvents() },
                onSuccess = { events ->
                    if (!events.error) {
                        _finishedEvents.postValue(events.listEvents)
                        events.listEvents.forEach { insert(it) }
                    } else {
                        _error.postValue("Failed to fetch finished events")
                    }
                }
            )
        }
    }

    fun fetchEventDetail(id: Int) {
        viewModelScope.launch {
            handleEventDetailResponse(
                apiCall = { repository.getEventDetail(id) },
                onSuccess = { eventDetailResponse ->
                    if (!eventDetailResponse.error) {
                        _eventDetail.postValue(eventDetailResponse.event)
                    } else {
                        _error.postValue("Failed to fetch event details")
                    }
                }
            )
        }
    }

    fun searchFinishedEvents(query: String) {
        viewModelScope.launch {
            handleEventResponse(
                apiCall = { repository.searchFinishedEvents(query) },
                onSuccess = { events ->
                    if (!events.error) {
                        _finishedEvents.postValue(events.listEvents)
                    } else {
                        _error.postValue("Failed to search finished events")
                    }
                }
            )
        }
    }
}