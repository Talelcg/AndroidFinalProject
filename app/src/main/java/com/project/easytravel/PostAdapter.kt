package com.project.easytravel

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.easytravel.R
import com.project.easytravel.model.Post
import com.project.easytravel.CommentsActivity
import com.project.easytravel.base.PostViewModel

class PostAdapter(private var postList: MutableList<Post>, private val viewModel: PostViewModel) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.tripImage)
        private val postTitle: TextView = itemView.findViewById(R.id.tripTitle)
        private val postDescription: TextView = itemView.findViewById(R.id.tripDescription)
        private val postLikes: TextView = itemView.findViewById(R.id.postLikes)
        private val likeButton: ImageButton = itemView.findViewById(R.id.buttonLike)
        private val commentButton: ImageButton = itemView.findViewById(R.id.buttonComment)

        fun bind(post: Post) {
            postTitle.text = post.title
            postDescription.text = post.description
            postLikes.text = "${post.likes.size} Likes"

            Glide.with(itemView.context).load(post.imageUrl).into(postImage)

            likeButton.setOnClickListener {
                viewModel.toggleLike(post)
            }

            commentButton.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("postId", post.id)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPosts: List<Post>) {
        postList.clear()
        postList.addAll(newPosts)
        notifyDataSetChanged()
    }
}