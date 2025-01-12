package com.project.easytravel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.easytravel.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initializing Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Handling navigation to the Sign In screen
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        // Handling Sign Up button click
        binding.button.setOnClickListener {
            val fullName = binding.fullNameEt.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()
            val confirmPass = binding.confirmPassEt.text.toString().trim()

            // Reset errors before validation
            binding.fullNameLayout.error = null
            binding.emailLayout.error = null
            binding.passwordLayout.error = null
            binding.confirmPasswordLayout.error = null

            if (fullName.isEmpty()) {
                binding.fullNameLayout.error = "Full name is required!"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.emailLayout.error = "Email is required!"
                return@setOnClickListener
            }

            if (pass.isEmpty()) {
                binding.passwordLayout.error = "Password is required!"
                return@setOnClickListener
            }

            if (pass.length < 6) {
                binding.passwordLayout.error = "Password must be at least 6 characters long!"
                return@setOnClickListener
            }

            if (confirmPass.isEmpty()) {
                binding.confirmPasswordLayout.error = "Confirm password is required!"
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                binding.confirmPasswordLayout.error = "Passwords do not match!"
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        saveUserInfo(currentUser.uid, fullName, email)
                    } else {
                        binding.emailLayout.error = "User authentication failed!"
                    }
                } else {
                    binding.emailLayout.error = task.exception?.message ?: "Sign Up Failed"
                    Log.e("SignUp", "Error: ${task.exception?.message}")
                }
            }
        }
    }

    private fun saveUserInfo(userId: String, fullName: String, email: String) {
        val userMap = hashMapOf(
            "uid" to userId,
            "email" to email,
            "name" to fullName,
            "bio" to "Hey! I am using EasyTravel",
            "image" to ""  // Empty image URL by default
        )

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    clearFields()
                    navigateToMainActivity()
                } else {
                    binding.fullNameLayout.error = task.exception?.message ?: "Failed to save user info"
                    Log.e("SignUp", "Firestore Error: ${task.exception?.message}")
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun clearFields() {
        binding.fullNameEt.text?.clear()
        binding.emailEt.text?.clear()
        binding.passET.text?.clear()
        binding.confirmPassEt.text?.clear()
    }
}
