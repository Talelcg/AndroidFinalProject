package com.project.easytravel

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.project.easytravel.model.Post
import com.project.easytravel.CommentsActivity
import com.google.firebase.auth.FirebaseAuth
import com.project.easytravel.base.PostViewModel
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class PostAdapter(private var postList: MutableList<Post>, private val viewModel: PostViewModel) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.tripImage)
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        private val postAuthor: TextView = itemView.findViewById(R.id.postAuthor)
        private val ratingBar: MaterialRatingBar = itemView.findViewById(R.id.ratingBar)
        private val postTitle: TextView = itemView.findViewById(R.id.tripTitle)
        private val postLocation: TextView = itemView.findViewById(R.id.postLocation)
        private val postDescription: TextView = itemView.findViewById(R.id.tripDescription)
        private val postLikes: TextView = itemView.findViewById(R.id.postLikes)
        private val postComments: TextView = itemView.findViewById(R.id.postComments)
        private val likeButton: ImageButton = itemView.findViewById(R.id.buttonLike)
        private val commentButton: ImageButton = itemView.findViewById(R.id.buttonComment)
        private val optionsButton: ImageButton = itemView.findViewById(R.id.buttonUpdateDelete)
        private val postUploadDate: TextView = itemView.findViewById(R.id.postUploadDate)

        fun bind(post: Post) {
            postTitle.text = post.title
            postLocation.text = " ${post.place}"
            postDescription.text = post.description
            postLikes.text = "${post.likes.size} Likes"
            postComments.text = "${post.comments.size} Comments"
            ratingBar.rating = post.rating
            ratingBar.isEnabled = false
            Glide.with(itemView.context).load(post.imageUrl).into(postImage)
            val date = Date(post.uploadDate)

            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = formatter.format(date)
            postUploadDate.text = formattedDate

            firestore.collection("users").document(post.userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userName = document.getString("name")
                        val userProfileImageUrl = document.getString("profileimage")
                        postAuthor.text = userName ?: "Unknown User"
                        if (!userProfileImageUrl.isNullOrEmpty()) {
                            Glide.with(itemView.context)
                                .load(userProfileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(profileImage)
                        } else {
                            profileImage.setImageResource(R.drawable.ic_launcher_foreground)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PostAdapter", "Error fetching user details: $e")
                }

            // Check if current user has liked the post
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (post.likes.contains(currentUserId)) {
                likeButton.setImageResource(R.drawable.ic_like) // Full heart icon
            } else {
                likeButton.setImageResource(R.drawable.unl) // Empty heart icon
            }

            likeButton.setOnClickListener {
                viewModel.toggleLike(post)
                // Update the icon after clicking
                if (post.likes.contains(currentUserId)) {
                    likeButton.setImageResource(R.drawable.unl)
                } else {
                    likeButton.setImageResource(R.drawable.ic_like)
                }
            }

            commentButton.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("postId", post.id)
                context.startActivity(intent)
            }

            // Show options button only if current user is the post author
            optionsButton.visibility = if (currentUserId == post.userId) {
                View.VISIBLE
            } else {
                View.GONE
            }

            optionsButton.setOnClickListener {
                val context = itemView.context
                val alertDialog = android.app.AlertDialog.Builder(context)
                    .setTitle("Choose Action")
                    .setMessage("Would you like to update or delete this post?")
                    .setPositiveButton("Update") { dialog, which ->
                        // Start the CreatePostActivity for editing the post (update)
                        val intent = Intent(context, CreatePostActivity::class.java)
                        intent.putExtra("postId", post.id)
                        context.startActivity(intent)
                    }
                    .setNegativeButton("Delete") { dialog, which ->
                        // Confirm post deletion
                        val deleteDialog = android.app.AlertDialog.Builder(context)
                            .setTitle("Delete Post")
                            .setMessage("Are you sure you want to delete this post?")
                            .setPositiveButton("Yes") { deleteDialog, _ ->
                                // Delete the post from Firestore
                                firestore.collection("posts").document(post.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        // Optionally notify the adapter to remove the post from the list
                                        val position = adapterPosition
                                        if (position != RecyclerView.NO_POSITION) {
                                            postList.removeAt(position)
                                            notifyItemRemoved(position)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("PostAdapter", "Error deleting post: $e")
                                    }
                                deleteDialog.dismiss()
                            }
                            .setNegativeButton("No") { deleteDialog, _ -> deleteDialog.dismiss() }
                            .show()
                    }
                    .setCancelable(true)
                    .show()
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
        postList.sortByDescending { it.uploadDate }
        notifyDataSetChanged()
    }
}
