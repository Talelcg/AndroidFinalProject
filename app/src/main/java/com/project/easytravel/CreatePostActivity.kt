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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.project.easytravel.model.Post
import com.project.easytravel.model.FirebaseModel
import com.idz.colman24class2.model.CloudinaryModel
import com.project.easytravel.model.AppDatabase
import com.project.easytravel.model.dao.PostDao
import com.project.easytravel.model.dao.AppLocalDb.database
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.materialratingbar.MaterialRatingBar

class CreatePostActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var postDao: PostDao

    private lateinit var cloudinaryModel: CloudinaryModel
    private lateinit var firebaseModel: FirebaseModel
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
        CloudinaryModel.initCloudinary(this)
        cloudinaryModel = CloudinaryModel()
        val database = AppDatabase.getDatabase(this)
        postDao = database.postDao()
        firebaseModel = FirebaseModel()

        initializeUI()
        setupListeners()
        setupNavigationBar()
    }

    private fun initializeUI() {
        titleEditText = findViewById(R.id.editTextTitle)
        placeEditText = findViewById(R.id.editTextPlace)
        descriptionEditText = findViewById(R.id.editTextDescription)
        imageView = findViewById(R.id.imageView)
        choosePhotoButton = findViewById(R.id.buttonChoosePhoto)
        createButton = findViewById(R.id.buttonCreate)
        ratingBar = findViewById(R.id.ratingBar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
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
    private fun uploadImageToCloudinary(postId: String,title: String, place: String, description: String, rating: Float) {
        cloudinaryModel.uploadImage(imageUri!!) { imageUrl, error ->
            if (error == null && imageUrl != null) {
                Log.e("trying2","trying2")
                savePostToFirebase(postId =postId,title, place, description, rating, imageUrl)
            } else {
                Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadBitmapToCloudinary(postId: String ,title: String, place: String, description: String, rating: Float) {
        cloudinaryModel.uploadBitmap(capturedBitmap!!, onSuccess = { imageUrl ->
            savePostToFirebase(postId =postId ,title, place, description, rating, imageUrl)
        }, onError = {
            Toast.makeText(this, "Bitmap upload failed!", Toast.LENGTH_SHORT).show()
        })
    }

    private fun createPost() {
        val title = titleEditText.text.toString().trim()
        val place = placeEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val rating = ratingBar.rating

        if (title.isNotEmpty() && place.isNotEmpty() && description.isNotEmpty()) {
            val postId = generatePostId()  // ✅ Generate a unique ID for this post

            lifecycleScope.launch {
                val existingPost = withContext(Dispatchers.IO) {
                    postDao.getPostById(postId)  // ✅ Check by unique ID
                }

                withContext(Dispatchers.Main) {
                    if (existingPost != null) {
                        Toast.makeText(this@CreatePostActivity, "This post already exists!", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }

                    if (imageUri != null) {
                        uploadImageToCloudinary(postId, title, place, description, rating)
                    } else if (capturedBitmap != null) {
                        uploadBitmapToCloudinary(postId, title, place, description, rating)
                    } else {
                        Toast.makeText(this@CreatePostActivity, "Please select or capture an image!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun generatePostId(): String {
        return java.util.UUID.randomUUID().toString()  // ✅ Generates a unique ID
    }
    private fun savePostToFirebase(postId: String, title: String, place: String, description: String, rating: Float, imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val post = Post(
            id = postId,
            place = place,// ✅ Use generated Post ID
            title = title,
            description =  description,
            imageUrl = imageUrl,
            rating = rating,
            likes = mutableListOf(),
            comments = mutableListOf(),
            userId = userId
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                postDao.insertPost(post)  // ✅ Save in Room with the same ID
            }

            withContext(Dispatchers.Main) {
                firebaseModel.createPost(post) { success ->
                    if (success) {
                        Toast.makeText(this@CreatePostActivity, "Post Created!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreatePostActivity, "Error creating post!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun fetchPostDetails(postId: String) {  // ✅ Pass postId as parameter
        lifecycleScope.launch {
            val existingPost = withContext(Dispatchers.IO) {
                postDao.getPostById(postId)  // ✅ Retrieve post by unique ID
            }

            if (existingPost != null) {
                withContext(Dispatchers.Main) {
                    titleEditText.setText(existingPost.title)
                    placeEditText.setText(existingPost.description.split("\n")[0])
                    descriptionEditText.setText(existingPost.description.split("\n")[1])
                    ratingBar.rating = existingPost.rating

                    Glide.with(this@CreatePostActivity)
                        .load(existingPost.imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                }
            } else {
                firebaseModel.getPostById(postId) { post ->
                    if (post != null) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                postDao.insertPost(post)  // ✅ Cache in Room
                            }
                        }

                        runOnUiThread {
                            titleEditText.setText(post.title)
                            placeEditText.setText(post.description.split("\n")[0])
                            descriptionEditText.setText(post.description.split("\n")[1])
                            ratingBar.rating = post.rating

                            Glide.with(this@CreatePostActivity)
                                .load(post.imageUrl)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(imageView)
                        }
                    }
                }
            }
        }
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