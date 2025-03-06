package com.project.easytravel

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.project.easytravel.base.PostViewModel
import com.project.easytravel.model.AppDatabase
import com.project.easytravel.model.Comment
import com.project.easytravel.model.FirebaseModel
import com.project.easytravel.model.dao.CommentDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsActivity : AppCompatActivity() {
    private lateinit var commentDao: CommentDao
    private lateinit var firebaseModel: FirebaseModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var editTextComment: EditText
    private lateinit var buttonPostComment: Button
    private lateinit var buttonBack: ImageButton
    private lateinit var postId: String
    private val postViewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        recyclerView = findViewById(R.id.recyclerViewComments)
        editTextComment = findViewById(R.id.editTextComment)
        buttonPostComment = findViewById(R.id.buttonPostComment)
        buttonBack = findViewById(R.id.buttonBack)

        postId = intent.getStringExtra("postId") ?: return

        val database = AppDatabase.getDatabase(this)
        commentDao = database.commentDao()
        firebaseModel = FirebaseModel()

        recyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(mutableListOf())
        recyclerView.adapter = commentAdapter

        buttonBack.setOnClickListener { finish() }

        buttonPostComment.setOnClickListener { postComment() }

        loadComments()
    }

    private fun postComment() {
        val commentText = editTextComment.text.toString().trim()
        if (commentText.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val comment = Comment(postId = postId, userId = userId, text = commentText, timestamp = System.currentTimeMillis())

            lifecycleScope.launch(Dispatchers.IO) {
                commentDao.insertComment(comment)
                firebaseModel.addComment(comment)

                withContext(Dispatchers.Main) {
                    editTextComment.text.clear()
                    loadComments()
                }
            }
        } else {
            Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComments() {
        postViewModel.getCommentsForPost(postId).observe(this) { roomComments ->
            commentAdapter.updateComments(roomComments)

            // Fetch from Firebase to ensure latest comments
            firebaseModel.getCommentsForPost(postId) { firebaseComments ->
                lifecycleScope.launch(Dispatchers.IO) {
                    for (comment in firebaseComments) {
                        commentDao.insertComment(comment) // Save new comments in Room
                    }
                }
            }
        }
    }

}