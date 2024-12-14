package com.tops.kotlin.firebaseauthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tops.kotlin.firebaseauthapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val userName = binding.tvUsername

        val auth = Firebase.auth
        val user = auth.currentUser?.displayName ?: firebaseAuth.currentUser?.email

        if (user != null) {
            userName.text = "Hello, $user"
        } else {
            Toast.makeText(this, "User is not found!!!", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddContact.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()

            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(this, "Logout successfully!!!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}