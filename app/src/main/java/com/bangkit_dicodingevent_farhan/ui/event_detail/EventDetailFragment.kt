package com.bangkit_dicodingevent_farhan.ui.event_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.data.local.database.EventDatabase
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bangkit_dicodingevent_farhan.data.remote.retrofit.ApiClient
import com.bangkit_dicodingevent_farhan.data.repository.EventRepository
import com.bangkit_dicodingevent_farhan.databinding.FragmentEventDetailBinding
import com.bangkit_dicodingevent_farhan.utils.HtmlCleaningFormatter.cleanAndFormatHtml
import com.bangkit_dicodingevent_farhan.utils.NetworkUtils
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModel
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModelFactory
import com.bumptech.glide.Glide

@Suppress("DEPRECATION")
class EventDetailFragment : Fragment() {

    private lateinit var viewModel: EventViewModel
    private lateinit var factory: EventViewModelFactory
    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18s")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupObservers()
        setupActionBar()
    }

    private fun setupViewModel() {
        val database = EventDatabase.getDatabase(requireContext())
        val apiService = ApiClient.apiService
        val repository = EventRepository(database.eventDao(), apiService)

        factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        viewModel.setLoading(true)

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            val eventId = arguments?.getInt("eventId") ?: 0
            viewModel.fetchEventDetail(eventId)
        } else {
            Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            viewModel.setLoading(false)
        }
    }

    private fun setupObservers() {
        viewModel.eventDetail.observe(viewLifecycleOwner) { event ->
            bindEventData(event)
            viewModel.setLoading(false)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, getString(R.string.cannot_open_url), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.eventDescription)
        }
    }

    @SuppressLint("SetTextI18s", "StringFormatMatches")
    private fun bindEventData(event: EventEntity) {
        with(binding) {
            eventName.text = event.name
            eventOwner.text = getString(R.string.eventBy, event.ownerName)
            eventTime.text = getString(R.string.eventDate, event.beginTime)
            eventQuota.text = getString(R.string.quotaRemaining, event.quota - event.registrants)

            eventDescription.apply {
                text = event.description.cleanAndFormatHtml()
                setLineSpacing(0f, 1.2f)
            }

            Glide.with(requireContext())
                .load(event.mediaCover)
                .placeholder(R.drawable.background_bangkit)
                .error(R.drawable.ic_error)
                .into(eventImage)

            openEventLink.setOnClickListener {
                openEventUrl(event.link)
            }
        }
    }

    private fun openEventUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.cannot_open_url), Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}