package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.easytravel.base.GeminiRequest
import com.project.easytravel.base.GeminiResponse
import com.project.easytravel.base.Content
import com.project.easytravel.base.Part
import com.project.easytravel.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TravelPlanner : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userProfileImage: ImageView
    private lateinit var userNameTextView: TextView  // TextView for username

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_planner)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.main)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        // Initialize the user profile image and username TextView
        userProfileImage = headerView.findViewById(R.id.user_profile_image)
        userNameTextView = headerView.findViewById(R.id.user_name)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val imageUrl = document.getString("profileimage")
                        val userName = document.getString("name")  // Assuming the user's name is stored in Firestore

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(userProfileImage)
                        }

                        // Set username text
                        if (!userName.isNullOrEmpty()) {
                            userNameTextView.text = userName
                        }
                    }
                }
                .addOnFailureListener {
                    userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
                    userNameTextView.text = "שם לא זמין"
                }
        } else {
            userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
            userNameTextView.text = "שם לא זמין"
        }

        val editDestination = findViewById<EditText>(R.id.editDestination)
        val editDays = findViewById<EditText>(R.id.editDays)
        val editInterests = findViewById<EditText>(R.id.editInterests)
        val btnGeneratePlan = findViewById<Button>(R.id.btnGeneratePlan)
        val txtPlanResult = findViewById<TextView>(R.id.txtPlanResult)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnGeneratePlan.setOnClickListener {
            val destination = editDestination.text.toString()
            val days = editDays.text.toString()
            val interests = editInterests.text.toString()

            if (destination.isEmpty() || days.isEmpty() || interests.isEmpty()) {
                Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prompt = "You are a travel planner. Create a detailed $days-day itinerary for $destination. " +
                    "The user's interests: $interests. " +
                    "Please respond only with the itinerary, without any additional explanation. " +
                    "Provide the itinerary in a list format where each day is structured as follows:\n" +
                    "Day 1:\n" +
                    "Hour - Activity - Details\n" +
                    "Day 2:\n" +
                    "Hour - Activity - Details\n" +
                    "... (continue for all days)"

            

            progressBar.visibility = View.VISIBLE
            txtPlanResult.text = ""

            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )

            RetrofitClient.instance.getTravelPlan(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            ).enqueue(object : Callback<GeminiResponse> {
                override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val responseText = responseBody?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        txtPlanResult.text = responseText ?: "לא התקבלה תשובה מהשרת."
                    } else {
                        txtPlanResult.text = "שגיאה בקבלת הנתונים: ${response.errorBody()?.string()}"
                        Log.e("TravelPlanner", "Error body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    txtPlanResult.text = "שגיאת חיבור: ${t.message}"
                }
            })
        }

        // Navigation item click handling
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_travelplanner -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
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
    }
}