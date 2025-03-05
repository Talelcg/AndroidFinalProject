package com.project.easytravel.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.easytravel.R
import com.project.easytravel.model.Post
import com.squareup.picasso.Picasso

class PostsAdapter : RecyclerView.Adapter<PostsAdapter.TripViewHolder>() {
    private var tripList = mutableListOf<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = tripList[position]
        holder.bind(trip)
    }

    override fun getItemCount(): Int = tripList.size

    fun updateList(newList: List<Post>) {
        tripList.clear()
        tripList.addAll(newList)
        notifyDataSetChanged() // 🔴 Forces UI to refresh
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tripTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tripDescription)
        private val tripImageView: ImageView = itemView.findViewById(R.id.tripImage)

        fun bind(trip: Post) {
            titleTextView.text = trip.title
            descriptionTextView.text = trip.description
            if (!trip.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(trip.imageUrl).into(tripImageView)
            }
        }
    }
}