package com.bangkit_dicodingevent_farhan.ui.event_finished

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.adapter.EventListAdapter
import com.bangkit_dicodingevent_farhan.data.local.database.EventDatabase
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bangkit_dicodingevent_farhan.data.remote.retrofit.ApiClient
import com.bangkit_dicodingevent_farhan.data.repository.EventRepository
import com.bangkit_dicodingevent_farhan.databinding.FragmentEventFinishedBinding
import com.bangkit_dicodingevent_farhan.utils.NetworkUtils
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModel
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModelFactory

class EventFinishedFragment : Fragment() {

    private var _binding: FragmentEventFinishedBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventViewModel
    private lateinit var adapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventFinishedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
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

    private fun setupRecyclerView() {
        adapter = EventListAdapter(::navigateToDetail)
        binding.recyclerViewFinished.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EventFinishedFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchViewFinished.apply {
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
                        Toast.makeText(
                            context,
                            getString(R.string.no_finished_events_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    adapter.submitList(it)
                }
            }

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBarFinished.visibility = if (isLoading) View.VISIBLE else View.GONE
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
            Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchEvents(query: String) {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            viewModel.searchFinishedEvents(query)
        } else {
            Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, getString(R.string.failed_to_open_event_detail), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}