package com.bangkit_dicodingevent_farhan.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bumptech.glide.Glide

class EventListAdapter(
    private var events: List<EventEntity>,
    private val onClick: (EventEntity) -> Unit
) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newEvents: List<EventEntity>) {
        Log.d("EventListAdapter", "Updating data with ${newEvents.size} items")
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        Log.d("EventListAdapter", "Creating new ViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_list, parent, false)
        return EventViewHolder(view).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    Log.d("EventListAdapter", "Item clicked at position: $position")
                    onClick(events[position])
                }
            }
        }
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        Log.d("EventListAdapter", "Binding data for event: ${event.name}")
        holder.bind(event)
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: CardView = view.findViewById(R.id.eventCard)
        private val eventImage: ImageView = view.findViewById(R.id.eventImage)
        private val eventName: TextView = view.findViewById(R.id.eventName)
        private val eventOwner: TextView = view.findViewById(R.id.eventOwner)
        private val eventDate: TextView = view.findViewById(R.id.eventDate)
        private val eventQuota: TextView = view.findViewById(R.id.eventQuota)

        init {
            // Tambahkan click listener di CardView
            cardView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    Log.d("EventListAdapter", "CardView clicked at position: $position")
                    onClick(events[position])
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(event: EventEntity) {
            // Set event name
            eventName.text = event.name

            // Set event owner/organizer
            eventOwner.text = event.ownerName

            // Format event date
            eventDate.text = "Waktu: ${event.beginTime}"

            // Format event quota
            eventQuota.text = "Sisa Kuota: ${event.quota - event.registrants}"

            // Load logo image using Glide
            try {
                Glide.with(itemView.context)
                    .load(event.imageLogo)
                    .placeholder(R.drawable.background_bangkit)
                    .error(R.drawable.ic_image_placeholder)
                    .into(eventImage)
            } catch (e: Exception) {
                Log.e("EventListAdapter", "Error loading image: ${e.message}")
                eventImage.setImageResource(R.drawable.ic_error)
            }

            // Tambahkan logging untuk debug
            Log.d(
                "EventListAdapter", """
                Binding event:
                ID: ${event.id}
                Name: ${event.name}
                Owner: ${event.ownerName}
                Begin Time: ${event.beginTime}
                Logo URL: ${event.imageLogo}
            """.trimIndent()
            )
        }
    }
}