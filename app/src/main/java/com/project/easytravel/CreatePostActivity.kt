package com.project.easytravel

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.project.easytravel.base.TripsViewModel
import com.project.easytravel.model.Trip
import com.idz.colman24class2.model.CloudinaryModel
import com.squareup.picasso.Picasso
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import java.io.IOException

class CreatePostActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewModel: TripsViewModel
    private lateinit var cloudinaryModel: CloudinaryModel
    private lateinit var titleEditText: EditText
    private lateinit var placeEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var choosePhotoButton: Button
    private lateinit var createButton: Button
    private lateinit var ratingBar: MaterialRatingBar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private var imageUri: Uri? = null
    private var capturedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        // Initialize Cloudinary
        CloudinaryModel.initCloudinary(this)

        initializeUI()
        setupListeners()
        setupNavigationBar()
    }

    private fun initializeUI() {


        try {
            viewModel = ViewModelProvider(this).get(TripsViewModel::class.java)
            cloudinaryModel = CloudinaryModel()
            titleEditText = findViewById(R.id.editTextTitle) ?: throw NullPointerException("editTextTitle not found")
            placeEditText = findViewById(R.id.editTextPlace) ?: throw NullPointerException("editTextPlace not found")
            descriptionEditText = findViewById(R.id.editTextDescription) ?: throw NullPointerException("editTextDescription not found")
            imageView = findViewById(R.id.imageView) ?: throw NullPointerException("imageView not found")
            choosePhotoButton = findViewById(R.id.buttonChoosePhoto) ?: throw NullPointerException("buttonChoosePhoto not found")
            createButton = findViewById(R.id.buttonCreate) ?: throw NullPointerException("buttonCreate not found")
            ratingBar = findViewById(R.id.ratingBar) ?: throw NullPointerException("ratingBar not found")
            drawerLayout = findViewById(R.id.drawer_layout) ?: throw NullPointerException("drawer_layout not found")
            navView = findViewById(R.id.nav_view) ?: throw NullPointerException("nav_view not found")
        } catch (e: NullPointerException) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        choosePhotoButton.setOnClickListener { showImagePickerDialog() }
        createButton.setOnClickListener { createPost() }
    }

    private fun setupNavigationBar() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener(this)
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Gallery", "Camera")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Photo From")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> selectImageFromGallery()
                1 -> takePhoto()
            }
        }
        builder.show()
    }

    private fun createPost() {
        val title = titleEditText.text.toString().trim()
        val place = placeEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val rating = ratingBar.rating

        if (title.isNotEmpty() && place.isNotEmpty() && description.isNotEmpty()) {
            if (imageUri != null) {
                uploadImageToCloudinary(title, place, description, rating)
            } else if (capturedBitmap != null) {
                uploadBitmapToCloudinary(title, place, description, rating)
            } else {
                Toast.makeText(this, "Please select or capture an image!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToCloudinary(title: String, place: String, description: String, rating: Float) {
        cloudinaryModel.uploadImage(imageUri!!) { imageUrl, error ->
            if (error == null && imageUrl != null) {
                savePostToDatabase(title, place, description, rating, imageUrl)
            } else {
                Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadBitmapToCloudinary(title: String, place: String, description: String, rating: Float) {
        cloudinaryModel.uploadBitmap(capturedBitmap!!, onSuccess = { imageUrl ->
            savePostToDatabase(title, place, description, rating, imageUrl)
        }, onError = {
            Toast.makeText(this, "Bitmap upload failed!", Toast.LENGTH_SHORT).show()
        })
    }

    private fun savePostToDatabase(title: String, place: String, description: String, rating: Float, imageUrl: String) {
        val trip = Trip(title = title, description = "$place\n$description\nRating: $rating", imageUrl = imageUrl)

        viewModel.insertTrip(trip)

        // 🔴 Force UI Refresh
        viewModel.allTrips.observe(this) { trips ->
            Log.d("DEBUG", "Updated Trip List: ${trips.size} trips available")
        }

        Toast.makeText(this, "Post Created!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                loadImage(it)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            capturedBitmap = result.data?.extras?.get("data") as? Bitmap
            imageView.setImageBitmap(capturedBitmap)
        }
    }

    private fun loadImage(uri: Uri) {
        Picasso.get().load(uri).into(imageView)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, AllTripsActivity::class.java))
            R.id.nav_create_post -> startActivity(Intent(this, CreatePostActivity::class.java))
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}