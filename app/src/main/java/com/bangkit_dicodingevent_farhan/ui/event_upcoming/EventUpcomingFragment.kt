package com.bangkit_dicodingevent_farhan.ui.event_upcoming

import android.os.Bundle
import android.util.Log
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
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bangkit_dicodingevent_farhan.databinding.FragmentEventUpcomingBinding
import com.bangkit_dicodingevent_farhan.di.Injection
import com.bangkit_dicodingevent_farhan.utils.NetworkUtils
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModel

class EventUpcomingFragment : Fragment() {

    private var _binding: FragmentEventUpcomingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventViewModel
    private lateinit var adapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnReloadUpcoming.setOnClickListener {
            fetchUpcomingEvents()
        }

        val factory = Injection.provideViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupReloadButton()
        fetchUpcomingEvents()
    }

    private fun setupRecyclerView() {
        adapter = EventListAdapter(::navigateToDetail)
        binding.recyclerViewUpcoming.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EventUpcomingFragment.adapter
        }
    }

    private fun setupObservers() {
        with(viewModel) {
            upcomingEvents.observe(viewLifecycleOwner) { events ->
                events?.let {
                    if (it.isEmpty()) {
                        Toast.makeText(context, getString(R.string.no_upcoming_events_found), Toast.LENGTH_SHORT)
                            .show()
                    }
                    adapter.submitList(it)
                }
            }

            isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBarUpcoming.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            error.observe(viewLifecycleOwner) { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    binding.btnReloadUpcoming.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupReloadButton() {
        binding.btnReloadUpcoming.setOnClickListener {
            fetchUpcomingEvents()
        }
    }

    private fun fetchUpcomingEvents() {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            binding.btnReloadUpcoming.visibility = View.GONE
            viewModel.setLoading(true)
            viewModel.fetchUpcomingEvents() // Fetch data list of events
        } else {
            Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            binding.btnReloadUpcoming.visibility = View.VISIBLE
            viewModel.setLoading(false)
        }
    }

    private fun navigateToDetail(event: EventEntity) {
        try {
            val bundle = Bundle().apply {
                putInt("eventId", event.id)
            }
            findNavController().navigate(R.id.action_upcoming_to_eventDetailFragment, bundle)
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