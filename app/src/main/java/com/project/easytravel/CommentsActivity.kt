package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
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

    private lateinit var progressBar: ProgressBar
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
        progressBar = findViewById(R.id.progressBar) // מקבלים את הספינר

        postId = intent.getStringExtra("postId") ?: return

        val database = AppDatabase.getDatabase(this)
        commentDao = database.commentDao()
        firebaseModel = FirebaseModel()

        recyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(mutableListOf(), emptyMap())
        recyclerView.adapter = commentAdapter



        buttonPostComment.setOnClickListener { postComment() }
        buttonBack.setOnClickListener {
            val intent = Intent(this@CommentsActivity, AllTripsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)  // אם אתה רוצה לסגור את הפעילויות הקודמות
            intent.putExtra("refreshPosts", true)  // שליחה של פרמטר שירמז ל-AllTripsActivity לרענן את הפוסטים
            startActivity(intent)
            finish()
        }

        loadComments()
    }

    private fun postComment() {
        val commentText = editTextComment.text.toString().trim()
        if (commentText.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val comment = Comment(postId = postId, userId = userId, text = commentText, timestamp = System.currentTimeMillis())

            lifecycleScope.launch(Dispatchers.IO) {
                commentDao.insertComment(comment)
                firebaseModel.addComment(comment,postId)

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
        // הצגת הספינר כשהתגובות נטענות
        progressBar.visibility = View.VISIBLE

        postViewModel.getCommentsForPost(postId).observe(this) { roomComments ->

            // שליפת כל המשתמשים מה-DB
            lifecycleScope.launch(Dispatchers.IO) {
                val usersList = commentDao.getAllUsers() // שליפת כל המשתמשים
                val usersMap = usersList.associateBy { it.id } // יצירת מפה של userId -> User

                withContext(Dispatchers.Main) {
                    // עדכון התגובות עם המידע של המשתמשים
                    commentAdapter.updateComments(roomComments, usersMap)
                    progressBar.visibility = View.GONE // מסתירים את הספינר כשסיימנו
                }
            }

            // שליפת משתמשים מפיירבייס
            firebaseModel.getAllUsers { firebaseUsers ->
                lifecycleScope.launch(Dispatchers.IO) {
                    for (user in firebaseUsers) {
                        commentDao.insertUser(user) // שמירת המשתמשים ב- Room
                    }

                    // עדכון המידע על המשתמשים
                    val updatedUsersList = commentDao.getAllUsers()
                    val updatedUsersMap = updatedUsersList.associateBy { it.id }

                    withContext(Dispatchers.Main) {
                        commentAdapter.updateComments(roomComments, updatedUsersMap)
                        progressBar.visibility = View.GONE // מסתירים את הספינר כשסיימנו
                    }
                }
            }
        }


}



}