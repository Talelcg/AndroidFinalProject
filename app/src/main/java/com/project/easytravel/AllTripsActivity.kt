package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.project.easytravel.base.PostViewModel
import com.project.easytravel.model.Post
import com.project.easytravel.PostAdapter

class AllTripsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postViewModel: PostViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_trips)

        recyclerView = findViewById(R.id.recyclerViewPosts)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)

        postViewModel = ViewModelProvider(this).get(PostViewModel::class.java)

        postViewModel.allPosts.observe(this) { posts ->
            progressBar.visibility = View.GONE
            postAdapter.updatePosts(posts)
        }

        postAdapter = PostAdapter(mutableListOf(), postViewModel)
        recyclerView.adapter = postAdapter

        progressBar.visibility = View.VISIBLE
        postViewModel.loadPosts()
    }
}
