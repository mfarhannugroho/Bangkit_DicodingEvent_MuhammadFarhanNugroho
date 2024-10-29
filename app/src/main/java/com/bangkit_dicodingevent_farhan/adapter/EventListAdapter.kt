package com.bangkit_dicodingevent_farhan.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bangkit_dicodingevent_farhan.R
import com.bangkit_dicodingevent_farhan.data.local.model.EventEntity
import com.bumptech.glide.Glide

class EventListAdapter(
    private val onClick: (EventEntity) -> Unit
) : ListAdapter<EventEntity, EventListAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        Log.d("EventListAdapter", "Creating new ViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_list, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        Log.d("EventListAdapter", "Binding data for event: ${event.name}")
        holder.bind(event)
    }

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: CardView = view.findViewById(R.id.eventCard)
        private val eventImage: ImageView = view.findViewById(R.id.eventImage)
        private val eventName: TextView = view.findViewById(R.id.eventName)
        private val eventOwner: TextView = view.findViewById(R.id.eventOwner)
        private val eventDate: TextView = view.findViewById(R.id.eventDate)
        private val eventQuota: TextView = view.findViewById(R.id.eventQuota)

        init {
            cardView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    Log.d("EventListAdapter", "CardView clicked at position: $position")
                    onClick(getItem(position))
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(event: EventEntity) {
            eventName.text = event.name
            eventOwner.text = event.ownerName
            eventDate.text = event.getFormattedBeginTime()
            eventQuota.text = "Sisa Kuota: ${event.quota - event.registrants}"

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

            Log.d(
                "EventListAdapter", """
                Binding event:
                ID: ${event.id}
                Name: ${event.name}
                Owner: ${event.ownerName}
                Begin Time: ${event.getFormattedBeginTime()}
                Logo URL: ${event.imageLogo}
                """.trimIndent()
            )
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<EventEntity>() {
        override fun areItemsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean {
            return oldItem == newItem
        }
    }
}