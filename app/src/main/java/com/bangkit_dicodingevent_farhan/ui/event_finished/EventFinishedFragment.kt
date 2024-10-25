package com.bangkit_dicodingevent_farhan.ui.event_finished

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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

class EventFinishedFragment : Fragment() {

    private lateinit var viewModel: EventViewModel
    private lateinit var adapter: EventListAdapter
    private var _progressBar: ProgressBar? = null
    private var _searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_finished, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _progressBar = view.findViewById(R.id.progressBarFinished)
        _searchView = view.findViewById(R.id.searchViewFinished)

        setupViewModel()
        setupRecyclerView(view)
        setupSearchView()
        setupObservers()
        fetchEvents()
    }

    private fun setupViewModel() {
        val database = EventDatabase.getDatabase(requireContext())
        val repository = EventRepository(database.eventDao(), ApiClient.apiService)
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupRecyclerView(view: View) {
        adapter = EventListAdapter(emptyList(), ::navigateToDetail)
        view.findViewById<RecyclerView>(R.id.recyclerViewFinished).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EventFinishedFragment.adapter
        }
    }

    private fun setupSearchView() {
        _searchView?.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { searchEvents(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        viewModel.fetchFinishedEvents()
                    }
                    return true
                }
            })
        }
    }

    private fun setupObservers() {
        with(viewModel) {
            finishedEvents.observe(viewLifecycleOwner) { events ->
                events?.let {
                    if (it.isEmpty()) {
                        Toast.makeText(context, "No finished events found", Toast.LENGTH_SHORT)
                            .show()
                    }
                    adapter.updateData(it)
                }
            }

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                _progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
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
            viewModel.fetchFinishedEvents()
        } else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchEvents(query: String) {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            viewModel.searchFinishedEvents(query)
        } else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDetail(event: EventEntity) {
        try {
            val bundle = Bundle().apply {
                putInt("eventId", event.id)
            }
            findNavController().navigate(R.id.action_finished_to_eventDetailFragment, bundle)
        } catch (e: Exception) {
            Log.e("Navigation", "Failed to navigate to detail", e)
            Toast.makeText(context, "Failed to open event detail", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _progressBar = null
        _searchView = null
    }
}