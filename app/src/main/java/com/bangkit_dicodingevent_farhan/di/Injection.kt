package com.bangkit_dicodingevent_farhan.di

import android.content.Context
import com.bangkit_dicodingevent_farhan.data.local.database.EventDatabase
import com.bangkit_dicodingevent_farhan.data.remote.retrofit.ApiClient
import com.bangkit_dicodingevent_farhan.data.repository.EventRepository
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModelFactory

object Injection {

    private fun provideRepository(context: Context): EventRepository {
        val database = EventDatabase.getDatabase(context)
        val apiService = ApiClient.apiService
        return EventRepository(database.eventDao(), apiService)
    }

    fun provideViewModelFactory(context: Context): EventViewModelFactory {
        val repository = provideRepository(context)
        return EventViewModelFactory(repository)
    }
}