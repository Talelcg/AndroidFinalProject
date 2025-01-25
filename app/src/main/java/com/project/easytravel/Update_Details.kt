package com.project.easytravel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.idz.colman24class2.model.CloudinaryModel
import com.project.easytravel.model.FirebaseModel
import com.project.easytravel.model.User
import com.project.easytravel.model.dao.AppLocalDbRepository
import com.project.easytravel.model.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Update_Details : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreModel: FirebaseModel
    private lateinit var userDao: UserDao
    private var cloudinaryModel: CloudinaryModel? = null

    private lateinit var editTextName: EditText
    private lateinit var editTextBio: EditText
    private lateinit var buttonSave: Button
    private lateinit var imageViewProfile: ImageView

    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imageViewProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)

        val database = Room.databaseBuilder(applicationContext, AppLocalDbRepository::class.java, "user-db").build()
        userDao = database.userDao()

        firebaseAuth = FirebaseAuth.getInstance()
        firestoreModel = FirebaseModel()

        editTextName = findViewById(R.id.editTextName)
        editTextBio = findViewById(R.id.editTextBio)
        buttonSave = findViewById(R.id.buttonSave)
        imageViewProfile = findViewById(R.id.imageView2)
        val buttonChangePassword = findViewById<Button>(R.id.buttonChangePassword)
        buttonChangePassword.setOnClickListener {
            onUpdatePasswordClick()
        }
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        imageViewProfile.setOnClickListener {
            pickImage.launch("image/*")
        }

        fetchUserDetails()

        buttonSave.setOnClickListener {
            if (validateInputs()) {
                updateUserDetails()
            } else {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserDetails() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            lifecycleScope.launch {
                val userFromRoom = withContext(Dispatchers.IO) {
                    userDao.getUserById(userId)
                }

                if (userFromRoom != null&&userFromRoom.profileimage!="") {
                    // If user data found in local DB (Room)
                    withContext(Dispatchers.Main) {
                        editTextName.setText(userFromRoom.name)
                        editTextBio.setText(userFromRoom.bio)
                        Glide.with(this@Update_Details)
                            .load(userFromRoom.profileimage)
                            .circleCrop()
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(imageViewProfile)
                    }
                } else {
                   firestoreModel.getUserById(userId) { user ->
                        if (user != null) {
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    userDao.insertUser(user)
                                }
                            }

                            runOnUiThread {
                                editTextName.setText(user.name)
                                editTextBio.setText(user.bio)
                                Glide.with(this@Update_Details)
                                    .load(user.profileimage)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(imageViewProfile)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@Update_Details, "Failed to fetch user details.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUserDetails() {
        val updatedName = editTextName.text.toString()
        val updatedBio = editTextBio.text.toString()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val updatedUser = User(
                id = userId,
                name = updatedName,
                bio = updatedBio,
                email = "",
                profileimage = imageUri?.toString() ?: ""
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    userDao.updateUser(updatedUser)
                }

                firestoreModel.updateUserDetails(userId, updatedName, updatedBio) { success ->
                    if (success) {
                        if (imageUri != null) {
                            uploadImageToCloudinary()
                        } else {
                            Toast.makeText(this@Update_Details, "Details updated successfully.", Toast.LENGTH_SHORT).show()
                            navigateToProfile()
                        }
                    } else {
                        Toast.makeText(this@Update_Details, "Failed to update details.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun uploadImageToCloudinary() {
        imageUri?.let { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            if (cloudinaryModel == null) {
                cloudinaryModel = CloudinaryModel()
            }
            cloudinaryModel?.uploadBitmap(
                bitmap = bitmap,
                onSuccess = { imageUrl ->
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        firestoreModel.updateUserProfileImage(currentUser.uid, imageUrl) { success ->
                            if (success) {
                                Toast.makeText(this, "Profile image updated.", Toast.LENGTH_SHORT).show()
                                navigateToProfile()
                            } else {
                                Toast.makeText(this, "Failed to update profile image.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onError = { error ->
                    Toast.makeText(this, "Error uploading image: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun navigateToProfile() {
        val intent = Intent(this@Update_Details, Profile::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun validateInputs(): Boolean {
        return editTextName.text.isNotEmpty() && editTextBio.text.isNotEmpty()
    }

    private fun onUpdatePasswordClick() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val email = currentUser.email

            if (email != null) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "No email associated with this account.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
