package com.tops.kotlin.firebaseauthapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import com.tops.kotlin.firebaseauthapp.databinding.ActivityMobileOtpactivityBinding
import java.util.concurrent.TimeUnit

class MobileOTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMobileOtpactivityBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var otp: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mobileNo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Retrieve data from Intent
        otp = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        mobileNo = intent.getStringExtra("phoneNumber")!!

        setupOtpFields()

        // Handle resend OTP button
        resendOTPVisibility()
        binding.tvResendOtp.setOnClickListener {
            resendVerificationCode()
            resendOTPVisibility()
        }

        // Verify OTP
        binding.btnVerify.setOnClickListener {
            val typedOtp = collectOtpInput()
            if (typedOtp.isNotEmpty() && typedOtp.length == 6) {
                val credential = PhoneAuthProvider.getCredential(otp, typedOtp)
                binding.progressBar.visibility = View.VISIBLE
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle auto-focus for OTP input fields
    private fun setupOtpFields() {
        binding.etOtp1.addTextChangedListener(EditTextWatcher(binding.etOtp1))
        binding.etOtp2.addTextChangedListener(EditTextWatcher(binding.etOtp2))
        binding.etOtp3.addTextChangedListener(EditTextWatcher(binding.etOtp3))
        binding.etOtp4.addTextChangedListener(EditTextWatcher(binding.etOtp4))
        binding.etOtp5.addTextChangedListener(EditTextWatcher(binding.etOtp5))
        binding.etOtp6.addTextChangedListener(EditTextWatcher(binding.etOtp6))
    }

    // Collect OTP input from all fields
    private fun collectOtpInput(): String {
        return binding.etOtp1.text.toString() +
                binding.etOtp2.text.toString() +
                binding.etOtp3.text.toString() +
                binding.etOtp4.text.toString() +
                binding.etOtp5.text.toString() +
                binding.etOtp6.text.toString()
    }

    // Handle resend OTP visibility
    private fun resendOTPVisibility() {
        clearOtpFields()
        binding.tvResendOtp.visibility = View.INVISIBLE
        binding.tvResendOtp.isEnabled = false
        Handler(Looper.myLooper()!!).postDelayed({
            binding.tvResendOtp.visibility = View.VISIBLE
            binding.tvResendOtp.isEnabled = true
        }, 60000)
    }

    // Clear OTP input fields
    private fun clearOtpFields() {
        binding.etOtp1.setText("")
        binding.etOtp2.setText("")
        binding.etOtp3.setText("")
        binding.etOtp4.setText("")
        binding.etOtp5.setText("")
        binding.etOtp6.setText("")
    }

    // Resend OTP
    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobileNo)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Firebase Callbacks
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("MobileAuth", "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("MobileAuth", "onVerificationFailed: ${e.message}")
            binding.progressBar.visibility = View.INVISIBLE
            Toast.makeText(
                this@MobileOTPActivity,
                "Verification Failed: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("MobileAuth", "onCodeSent: $verificationId")
            otp = verificationId
            resendToken = token
        }
    }

    // Sign in with Phone Auth Credential
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.INVISIBLE
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Toast.makeText(
                        this@MobileOTPActivity,
                        "Login Successful! Welcome, $user",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@MobileOTPActivity, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } else {
                    Log.e("MobileAuth", "SignIn Failed: ${task.exception?.message}")
                    Toast.makeText(
                        this@MobileOTPActivity,
                        "Sign-In Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // TextWatcher for OTP fields
    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            when (view.id) {
                binding.etOtp1.id -> if (s?.length == 1) binding.etOtp2.requestFocus()
                binding.etOtp2.id -> if (s?.length == 1) binding.etOtp3.requestFocus() else if (s.isNullOrEmpty()) binding.etOtp1.requestFocus()
                binding.etOtp3.id -> if (s?.length == 1) binding.etOtp4.requestFocus() else if (s.isNullOrEmpty()) binding.etOtp2.requestFocus()
                binding.etOtp4.id -> if (s?.length == 1) binding.etOtp5.requestFocus() else if (s.isNullOrEmpty()) binding.etOtp3.requestFocus()
                binding.etOtp5.id -> if (s?.length == 1) binding.etOtp6.requestFocus() else if (s.isNullOrEmpty()) binding.etOtp4.requestFocus()
                binding.etOtp6.id -> if (s.isNullOrEmpty()) binding.etOtp5.requestFocus()
            }
        }
    }
}