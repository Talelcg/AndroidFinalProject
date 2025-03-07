package com.project.easytravel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.easytravel.model.Comment
import com.project.easytravel.model.User

class CommentAdapter(
    private var comments: MutableList<Comment>,
    private var usersMap: Map<String, User> = emptyMap() // הוספת מפה של ID -> משתמש
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commentText: TextView = itemView.findViewById(R.id.commentText)
        private val userNameText: TextView = itemView.findViewById(R.id.userName) // הוסף TextView בשם זה ל-layout

        fun bind(comment: Comment) {
            commentText.text = comment.text
            userNameText.text = usersMap[comment.userId]?.name ?: "Unknown User"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>, newUsersMap: Map<String, User>) {
        comments.clear()
        comments.addAll(newComments)
        usersMap = newUsersMap
        notifyDataSetChanged()
    }
}

