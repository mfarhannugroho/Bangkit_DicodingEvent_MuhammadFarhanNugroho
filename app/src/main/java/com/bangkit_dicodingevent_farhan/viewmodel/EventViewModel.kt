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

    private val _favoriteEvents = MutableLiveData<List<EventEntity>>()
    val favoriteEvents: LiveData<List<EventEntity>> get() = _favoriteEvents

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingUpcoming = MutableLiveData<Boolean>()
    val isLoadingUpcoming: LiveData<Boolean> = _isLoadingUpcoming

    private val _isLoadingFinished = MutableLiveData<Boolean>()
    val isLoadingFinished: LiveData<Boolean> = _isLoadingFinished

    private suspend fun updateFavoriteEvents() {
        _favoriteEvents.postValue(repository.getFavoriteEvents())
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun addEventToFavorites(event: EventEntity) {
        viewModelScope.launch {
            repository.insert(event)
            updateFavoriteEvents()
        }
    }

    fun removeEventFromFavorites(event: EventEntity) {
        viewModelScope.launch {
            repository.delete(event)
            _favoriteEvents.value = repository.getFavoriteEvents()
        }
    }

    suspend fun isEventFavorite(eventId: Int): Boolean {
        return repository.isEventFavorite(eventId)
    }

    fun fetchFavoriteEvents() {
        _isLoading.value = true
        viewModelScope.launch {
            val favorites = repository.getFavoriteEvents()
            _favoriteEvents.value = favorites
            _isLoading.value = false

            if (favorites.isEmpty()) {
                _favoriteEvents.postValue(emptyList())
            }
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
        _isLoadingUpcoming.value = true
        viewModelScope.launch {
            try {
                val response = repository.getUpcomingEvents()
                if (response.isSuccessful) {
                    response.body()?.listEvents?.let {
                        _upcomingEvents.postValue(it)
                    }
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoadingUpcoming.postValue(false)
            }
        }
    }

    fun fetchFinishedEvents() {
        _isLoadingFinished.value = true
        viewModelScope.launch {
            try {
                val response = repository.getFinishedEvents()
                if (response.isSuccessful) {
                    response.body()?.listEvents?.let {
                        _finishedEvents.postValue(it)
                    }
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoadingFinished.postValue(false)
            }
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