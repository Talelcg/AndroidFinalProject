package com.project.easytravel

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.project.easytravel.model.Comment
import com.project.easytravel.model.User
class CommentAdapter(
    private var comments: MutableList<Comment>,
    private var usersMap: Map<String, User> = emptyMap(),
    private val onDeleteClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commentText: TextView = itemView.findViewById(R.id.commentText)
        private val userNameText: TextView = itemView.findViewById(R.id.userName)
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteComment)

        fun bind(comment: Comment, currentUserId: String) {
            commentText.text = comment.text
            val user = usersMap[comment.userId]

            if (user != null) {
                userNameText.text = user.name
                Glide.with(itemView.context)
                    .load(user.profileimage)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(profileImage)
            } else {
                userNameText.text = "Unknown User"
                profileImage.setImageResource(R.drawable.profile)
            }

            if (comment.userId == currentUserId) {
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener { onDeleteClick(comment) }
            } else {
                deleteButton.visibility = View.GONE
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        holder.bind(comments[position], currentUserId)
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: MutableList<Comment>, newUsersMap: Map<String, User>) {
        comments.clear()
        comments.addAll(newComments)

        // בדיקה האם יש שינוי בפרטי המשתמשים
        val hasUserChanged = usersMap != newUsersMap
        usersMap = newUsersMap

        if (hasUserChanged) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(0, comments.size)
        }
    }



    fun removeComment(comment: Comment) {
        val position = comments.indexOfFirst { it.id == comment.id }
        if (position != -1) {
            comments.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
