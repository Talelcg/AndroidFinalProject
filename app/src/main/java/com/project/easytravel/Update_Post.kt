package com.project.easytravel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.idz.colman24class2.model.CloudinaryModel
import com.project.easytravel.model.Post
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import java.util.UUID

class Update_Post : AppCompatActivity() {
    private lateinit var postTitle: EditText
    private lateinit var postContent: EditText
    private lateinit var postPlace: EditText
    private lateinit var postImage: ImageView
    private lateinit var ratingBar: MaterialRatingBar
    private lateinit var btnUpdate: Button
    private lateinit var postImagebtn: Button
    private var imageUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var postId: String? = null
    private var existingImageUrl: String? = null
    private var existingRating: Float = 0f
    private lateinit var btnBack: ImageView
    private lateinit var cloudinaryModel: CloudinaryModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_post)

        btnBack = findViewById(R.id.buttonBack)
        postTitle = findViewById(R.id.editTextTitle)
        postContent = findViewById(R.id.editTextDescription)
        postPlace = findViewById(R.id.editTextPlace)
        postImage = findViewById(R.id.imageView)
        postImagebtn = findViewById(R.id.buttonChoosePhoto)
        ratingBar = findViewById(R.id.ratingBar)
        btnUpdate = findViewById(R.id.buttonUpdate)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        CloudinaryModel.initCloudinary(this)
        cloudinaryModel = CloudinaryModel()

        postId = intent.getStringExtra("postId")
        if (postId != null) {
            loadPostData(postId!!)
        }

        btnBack.setOnClickListener {
            finish()
        }

        postImagebtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            getContent.launch(intent)
        }

        btnUpdate.setOnClickListener {
            updatePost()
        }
    }

    private fun loadPostData(postId: String) {
        firestore.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        postTitle.setText(it.title)
                        postContent.setText(it.description)
                        postPlace.setText(it.place)
                        existingImageUrl = it.imageUrl
                        Glide.with(this).load(it.imageUrl).into(postImage)
                        it.rating?.let { rating ->
                            existingRating = rating
                            ratingBar.rating = rating
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load post", Toast.LENGTH_SHORT).show()
            }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                postImage.setImageURI(it)
            }
        }
    }

    private fun updatePost() {
        if (postId == null) return

        val updatedTitle = postTitle.text.toString().trim()
        val updatedContent = postContent.text.toString().trim()
        val updatedRating = ratingBar.rating
        val updatedPlace = postPlace.text.toString().trim()

        val updates = mutableMapOf<String, Any>()
        if (updatedTitle.isNotEmpty()) updates["title"] = updatedTitle
        if (updatedPlace.isNotEmpty()) updates["place"] = updatedPlace
        if (updatedContent.isNotEmpty()) updates["description"] = updatedContent
        if (updatedRating != existingRating) updates["rating"] = updatedRating

        // Check if the image is updated
        if (imageUri != null) {
            uploadImageToCloudinary { imageUrl ->
                if (imageUrl != null) {
                    updates["imageUrl"] = imageUrl
                }
                saveUpdatedPostToFirebase(updates)
            }
        } else {
            saveUpdatedPostToFirebase(updates)
        }
    }

    private fun uploadImageToCloudinary(onUploadComplete: (String?) -> Unit) {
        cloudinaryModel.uploadImage(imageUri!!) { imageUrl, error ->
            if (error == null && imageUrl != null) {
                onUploadComplete(imageUrl)
            } else {
                Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
                onUploadComplete(null)
            }
        }
    }

    private fun saveUpdatedPostToFirebase(updates: Map<String, Any>) {
        if (postId == null) return

        firestore.collection("posts").document(postId!!)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Post Updated Successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AllTripsActivity::class.java)

                startActivity(intent)

                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update post", Toast.LENGTH_SHORT).show()
            }
    }
}


