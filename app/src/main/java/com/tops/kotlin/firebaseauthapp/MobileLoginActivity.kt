package com.tops.kotlin.firebaseauthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.tops.kotlin.firebaseauthapp.databinding.ActivityMobileLoginBinding
import java.util.concurrent.TimeUnit

class MobileLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMobileLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mobileNo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnGetOtp.setOnClickListener {
            mobileNo = binding.etMobile.text.toString().trim()

            if (mobileNo.isNotEmpty()) {
                if (mobileNo.matches(Regex("\\d{10}"))) { // Validate for 10 digits
                    mobileNo = "+91$mobileNo" // Add country code
                    binding.progressBar.visibility = View.VISIBLE

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(mobileNo)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } else {
                    binding.etMobile.error = "Enter a valid 10-digit number"
                }
            } else {
                binding.etMobile.error = "Mobile Number Required"
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("MobileAuth", "onVerificationCompleted:$credential")
            Toast.makeText(
                this@MobileLoginActivity,
                "Auto-Verification Successful",
                Toast.LENGTH_SHORT
            ).show()
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("MobileAuth", "onVerificationFailed: ${e.message}")
            binding.progressBar.visibility = View.INVISIBLE
            Toast.makeText(
                this@MobileLoginActivity,
                "Verification Failed: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("MobileAuth", "onCodeSent:$verificationId")
            binding.progressBar.visibility = View.INVISIBLE

            val intent = Intent(this@MobileLoginActivity, MobileOTPActivity::class.java)
            intent.putExtra("OTP", verificationId)
            intent.putExtra("resendToken", token)
            intent.putExtra("phoneNumber", mobileNo)
            startActivity(intent)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.INVISIBLE
                if (task.isSuccessful) {
                    Log.d("MobileAuth", "signInWithCredential:success")
                    val user = task.result?.user
                    Toast.makeText(
                        this@MobileLoginActivity,
                        "Login Successful: Welcome $user",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(
                        Intent(this@MobileLoginActivity, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                } else {
                    Log.w("MobileAuth", "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this@MobileLoginActivity,
                        "Sign-In Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}