package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.easytravel.model.User
import com.project.easytravel.model.dao.AppLocalDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val db = AppLocalDb.database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_settings -> {
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

        updateUserDetails(navigationView)
    }

    private fun updateUserDetails(navigationView: NavigationView) {
        val headerView = navigationView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.user_name)
        val userProfileImage = headerView.findViewById<ImageView>(R.id.user_profile_image)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            CoroutineScope(Dispatchers.IO).launch {
                val localUser = db.userDao().getUserById(userId)

                if (localUser != null) {
                    runOnUiThread {
                        if(localUser.name!="Unknown User"){
                        userNameTextView.text = localUser.name
                        userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)}
                    }
                } else {

                    firestore.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val fullName = document.getString("name") ?: "Unknown User"
                                val imageUrl = document.getString("image") ?: ""

                                runOnUiThread {
                                    userNameTextView.text = fullName
                                    userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
                                }


                                val user = User(userId, currentUser.email ?: "", fullName, "", imageUrl)
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.userDao().insertUser(user)
                                }
                            } else {
                                runOnUiThread {
                                    userNameTextView.text = "Unknown User"
                                    userProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
                                }
                                Log.e("MainActivity", "No user document found")
                            }
                        }
                        .addOnFailureListener { exception ->
                            runOnUiThread {
                                userNameTextView.text = "Error loading user"
                            }
                            Log.e("MainActivity", "Error fetching user details: ${exception.message}")
                        }
                }
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
