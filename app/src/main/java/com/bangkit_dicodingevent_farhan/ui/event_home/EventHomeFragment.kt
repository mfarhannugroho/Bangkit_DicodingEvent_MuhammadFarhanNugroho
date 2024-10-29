package com.bangkit_dicodingevent_farhan.ui.event_home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.adapter.EventListAdapter
import com.bangkit_dicodingevent_farhan.data.local.database.EventDatabase
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bangkit_dicodingevent_farhan.data.remote.retrofit.ApiClient
import com.bangkit_dicodingevent_farhan.data.repository.EventRepository
import com.bangkit_dicodingevent_farhan.utils.NetworkUtils
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModel
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModelFactory

class EventHomeFragment : Fragment() {
    private lateinit var viewModel: EventViewModel
    private lateinit var upcomingAdapter: EventListAdapter
    private lateinit var finishedAdapter: EventListAdapter
    private var _progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _progressBar = view.findViewById(R.id.progressBarHome)

        setupViewModel()
        setupAdapters(view)
        setupObservers()
        fetchEvents()
    }

    private fun setupViewModel() {
        val database = EventDatabase.getDatabase(requireContext())
        val repository = EventRepository(database.eventDao(), ApiClient.apiService)
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupAdapters(view: View) {
        // Setup Upcoming Events
        upcomingAdapter = EventListAdapter(::navigateToDetail)
        view.findViewById<RecyclerView>(R.id.recyclerViewUpcomingHome).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingAdapter
        }

        finishedAdapter = EventListAdapter(::navigateToDetail)
        view.findViewById<RecyclerView>(R.id.recyclerViewFinishedHome).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = finishedAdapter
        }
    }

    private fun setupObservers() {
        with(viewModel) {
            isLoading.observe(viewLifecycleOwner) { isLoading ->
                _progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            upcomingEvents.observe(viewLifecycleOwner) { events ->
                events?.let { upcomingAdapter.submitList(it) }
            }

            finishedEvents.observe(viewLifecycleOwner) { events ->
                events?.let { finishedAdapter.submitList(it) }
            }

            error.observe(viewLifecycleOwner) { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchEvents() {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            viewModel.apply {
                fetchUpcomingEvents()
                fetchFinishedEvents()
            }
        } else {
            Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDetail(event: EventEntity) {
        try {
            val bundle = Bundle().apply {
                putInt("eventId", event.id)
            }
            findNavController().navigate(R.id.action_home_to_eventDetailFragment, bundle)
        } catch (e: Exception) {
            Log.e("Navigation", "Failed to navigate to detail", e)
            Toast.makeText(context, getString(R.string.failed_to_open_event_detail), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _progressBar = null
    }
}