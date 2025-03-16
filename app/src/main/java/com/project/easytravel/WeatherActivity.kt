package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.easytravel.databinding.ActivityWeatherBinding
import com.project.easytravel.model.WeatherViewModel

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // אתחול Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // הגדרת ה-DrawerLayout וה-Toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
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

        // הגדרת NavigationView ועדכון פרטי המשתמש בחלק העליון (Header)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        updateUserDetails(navigationView)

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
                // במקרה של WeatherActivity – ניתן לבחור שלא לבצע שינוי, כיוון שכבר נמצאים כאן

                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, SignIn::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // טיפול בלחיצת הכפתור לחיפוש מזג אוויר
        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString()
            if (city.isNotEmpty()) {
                viewModel.fetchWeather(city)
            }
        }

        // צפייה בנתוני מזג האוויר ועדכון ה-UI
        viewModel.weatherData.observe(this) { weather ->
            weather?.let {
                binding.tvTemperature.text = "${it.main.temp}°C"
                binding.tvDescription.text = it.weather[0].description
            }
        }
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
                .addOnFailureListener {
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
