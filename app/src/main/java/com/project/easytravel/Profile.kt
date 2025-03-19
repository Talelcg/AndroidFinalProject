package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.easytravel.base.PostViewModel
import com.project.easytravel.model.Post
import com.project.easytravel.PostAdapter

class Profile : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postViewModel: PostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        recyclerView = findViewById(R.id.recycler_view_posts)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        postAdapter = PostAdapter(mutableListOf(), postViewModel)
        recyclerView.adapter = postAdapter

        drawerLayout = findViewById(R.id.main)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        progressBar = findViewById(R.id.progress_bar)

        val updateButton = findViewById<ImageButton>(R.id.update_button)
        updateButton.setOnClickListener {
            val intent = Intent(this, Update_Details::class.java)
            startActivity(intent)
        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AllTripsActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_create_post -> {
                    startActivity(Intent(this, CreatePostActivity::class.java))
                }
                R.id.nav_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_travelplanner -> {
                    startActivity(Intent(this, TravelPlanner::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.Weather-> {

                    val intent = Intent(this, WeatherActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, SignIn::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        loadUserDetails(navigationView)
        loadUserPosts()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadUserDetails(navigationView: NavigationView) {
        val headerView = navigationView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.user_name)
        val userProfileImage = headerView.findViewById<ImageView>(R.id.user_profile_image)

        val userProfilePage = findViewById<ImageView>(R.id.profile_image)
        val email = findViewById<TextView>(R.id.user_email)
        val userpro = findViewById<TextView>(R.id.user_full_name)
        val bio = findViewById<TextView>(R.id.user_bio)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            progressBar.visibility = View.VISIBLE
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    progressBar.visibility = View.GONE
                    if (document != null && document.exists()) {
                        val fullName = document.getString("name")
                        userNameTextView.text = fullName ?: "Unknown User"
                        email.text = document.getString("email") ?: "No email available"
                        userpro.text = fullName ?: "Unknown User"
                        bio.text = document.getString("bio")
                        val imageUrl = document.getString("profileimage")
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(userProfileImage)

                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(userProfilePage)
                        } else {
                            userProfileImage.setImageResource(R.drawable.profile)
                        }
                    } else {
                        handleInvalidUser(userNameTextView, userProfileImage)
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    handleInvalidUser(userNameTextView, userProfileImage)
                }
        } else {
            userNameTextView.text = "Guest"
            userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserPosts() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val userPosts = mutableListOf<Post>()
                    for (document in documents) {
                        val post = document.toObject(Post::class.java)
                        userPosts.add(post)
                    }
                    postAdapter.updatePosts(userPosts)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load posts.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleInvalidUser(userNameTextView: TextView, userProfileImage: ImageView) {
        userNameTextView.text = "Invalid User"
        userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
        Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
