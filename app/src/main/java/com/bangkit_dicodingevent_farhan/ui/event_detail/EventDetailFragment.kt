package com.bangkit_dicodingevent_farhan.ui.event_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
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
import com.bangkit_dicodingevent_farhan.utils.HtmlCleaningFormatter.cleanAndFormatHtml
import com.bangkit_dicodingevent_farhan.utils.NetworkUtils
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModel
import com.bangkit_dicodingevent_farhan.viewmodel.EventViewModelFactory
import com.bumptech.glide.Glide


@Suppress("DEPRECATION")
class EventDetailFragment : Fragment() {

    private lateinit var viewModel: EventViewModel
    private lateinit var factory: EventViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    @SuppressLint("SetTextI18s")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupObservers(view)
        setupActionBar()
    }

    private fun setupViewModel() {
        // Initialize Repository dengan ApiClient dan EventDatabase yang baru
        val database = EventDatabase.getDatabase(requireContext())
        val apiService = ApiClient.apiService
        val repository = EventRepository(database.eventDao(), apiService)

        // Initialize ViewModel dengan Factory
        factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        // Set loading state to true before fetching event detail
        viewModel.setLoading(true)

        // Fetch event detail jika network tersedia
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            val eventId = arguments?.getInt("eventId") ?: 0
            viewModel.fetchEventDetail(eventId)
        } else {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            // Set loading state to false
            viewModel.setLoading(false)
        }
    }

    private fun setupObservers(view: View) {
        // Observe event detail
        viewModel.eventDetail.observe(viewLifecycleOwner) { event ->
            // Bind event data
            bindEventData(view, event)

            // Set loading state to false
            viewModel.setLoading(false)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            view.findViewById<ProgressBar>(R.id.progressBarDetail).visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }


        // Observe error messages
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            view.findViewById<View>(R.id.progressBar)?.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.eventDescription)
        }
    }

    @SuppressLint("SetTextI18s", "SetTextI18n")
    private fun bindEventData(view: View, event: EventEntity) {
        with(view) {
            findViewById<TextView>(R.id.eventName).text = event.name
            findViewById<TextView>(R.id.eventOwner).text = "By: ${event.ownerName}"
            findViewById<TextView>(R.id.eventTime).text = "Waktu: ${event.beginTime}"
            findViewById<TextView>(R.id.eventQuota).text =
                "Sisa Kuota: ${event.quota - event.registrants}"

            // Gunakan extension function untuk membersihkan HTML
            findViewById<TextView>(R.id.eventDescription).apply {
                text = event.description.cleanAndFormatHtml()
                setLineSpacing(0f, 1.2f)
            }

            // Load image menggunakan Glide
            Glide.with(context)
                .load(event.mediaCover)
                .placeholder(R.drawable.background_bangkit)
                .error(R.drawable.ic_error)
                .into(findViewById(R.id.eventImage))

            // Setup event link button
            findViewById<Button>(R.id.openEventLink).setOnClickListener {
                openEventUrl(event.link)
            }
        }
    }

    private fun openEventUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
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
}