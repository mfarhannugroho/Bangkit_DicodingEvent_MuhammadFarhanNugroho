package com.bangkit_dicodingevent_farhan.ui.event_favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.adapter.EventListAdapter
import com.bangkit_dicodingevent_farhan.data.local.database.EventDatabase
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bangkit_dicodingevent_farhan.data.remote.retrofit.ApiClient.apiService
import com.bangkit_dicodingevent_farhan.data.repository.EventRepository
import com.bangkit_dicodingevent_farhan.databinding.FragmentFavoriteBinding
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModel
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModelFactory

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventViewModel
    private lateinit var adapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupObservers()

        viewModel.fetchFavoriteEvents()
    }

    private fun setupViewModel() {
        val database = EventDatabase.getDatabase(requireContext())
        val repository = EventRepository(database.eventDao(), apiService)
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = EventListAdapter(::navigateToDetail)
        binding.recyclerViewFavorites.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FavoriteFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarFav.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.favoriteEvents.observe(viewLifecycleOwner) { events ->
            binding.progressBarFav.visibility = View.GONE
            if (events.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.no_favorite_events_found), Toast.LENGTH_SHORT).show()
            } else {
                adapter.submitList(events)
            }
        }
    }

    private fun navigateToDetail(event: EventEntity) {
        val bundle = Bundle().apply {
            putInt("eventId", event.id)
        }
        findNavController().navigate(R.id.action_favorite_to_eventDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}