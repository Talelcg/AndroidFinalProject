package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.project.easytravel.base.TripsViewModel
import com.project.easytravel.model.Trip
import com.project.easytravel.ui.TripsAdapter
import com.project.easytravel.model.TripDao

class AllTripsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewModel: TripsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripsAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_trips)

        initializeUI()
        setupNavigationBar()
        setupRecyclerView()
        loadTrips()
    }

    private fun initializeUI() {
        viewModel = ViewModelProvider(this).get(TripsViewModel::class.java)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
    }

    private fun setupNavigationBar() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, AllTripsActivity::class.java))
            R.id.nav_create_post -> startActivity(Intent(this, CreatePostActivity::class.java))
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewTrips)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TripsAdapter()
        recyclerView.adapter = adapter
    }
    private fun loadTrips() {
        viewModel.allTrips.observe(this) { trips: List<Trip> ->
            if (trips.isEmpty()) {
                Log.d("DEBUG", "No trips found in database.")
            } else {
                for (trip in trips) {
                    Log.d("DEBUG", "Retrieved Trip - Title: ${trip.title}, Description: ${trip.description}, Image: ${trip.imageUrl}")
                }
                adapter.updateList(trips) // 🔴 Force UI update
            }
        }
    }
}
