package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.easytravel.base.PostViewModel
import com.project.easytravel.model.Post
import com.project.easytravel.PostAdapter
import kotlinx.coroutines.launch

class AllTripsActivity : AppCompatActivity() {
    private lateinit var searchLocation: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postViewModel: PostViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_trips)

        firebaseAuth = FirebaseAuth.getInstance()
        searchLocation = findViewById(R.id.searchLocation)


        recyclerView = findViewById(R.id.recyclerViewPosts)
        progressBar = findViewById(R.id.progressBar)
        drawerLayout = findViewById(R.id.drawer_layout)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager = LinearLayoutManager(this)

        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        postViewModel.allPosts.observe(this) { posts ->
            progressBar.visibility = View.GONE
            postAdapter.updatePosts(posts)

            // Close the SwipeRefreshLayout once posts are loaded
            swipeRefreshLayout.isRefreshing = false
        }

        postAdapter = PostAdapter(mutableListOf(), postViewModel)
        recyclerView.adapter = postAdapter

        progressBar.visibility = View.VISIBLE
        postViewModel.loadPosts()


        swipeRefreshLayout.setOnRefreshListener {
            // Reload the posts when the user swipes to refresh
            postViewModel.loadPosts()
        }

        // Set up DrawerLayout and Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up NavigationView
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, AllTripsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_create_post -> {
                    val intent = Intent(this, CreatePostActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
                R.id.nav_travelplanner -> {
                    val intent = Intent(this, TravelPlanner::class.java)
                    startActivity(intent)
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
        searchLocation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPostsByLocation(s.toString())
            }
        })
        updateUserDetails(navigationView)
    }

    fun refreshPosts() {
        postViewModel.loadPosts()
    }

    private fun filterPostsByLocation(location: String) {
        val filteredList = postViewModel.allPosts.value?.filter { post ->
            post.place.contains(location, ignoreCase = true)
        } ?: emptyList()

        postAdapter.updatePosts(filteredList)
    }

    private fun updateUserDetails(navigationView: NavigationView) {
        val headerView = navigationView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.user_name)
        val userProfileImage = headerView.findViewById<ImageView>(R.id.user_profile_image)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid


            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("name") ?: "Unknown User"
                        val imageUrl = document.getString("profileimage") ?: ""

                        // Update UI with user details
                        userNameTextView.text = fullName

                        if (imageUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(userProfileImage)
                        } else {
                            userProfileImage.setImageResource(R.drawable.profile)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    userNameTextView.text = "Error loading user"
                    userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
                }
        } else {
            userNameTextView.text = "Guest"
            userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
