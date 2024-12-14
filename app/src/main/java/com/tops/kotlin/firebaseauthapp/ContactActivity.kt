package com.tops.kotlin.firebaseauthapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tops.kotlin.firebaseauthapp.databinding.ActivityContactBinding
import com.tops.kotlin.firebaseauthapp.models.Contact

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().getReference("contacts")

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val contact = binding.etContact.text.toString()
            val address = binding.etAddress.text.toString()
            val id = databaseReference.push().key!!

            saveContactInRD(name, contact, address, id)
        }
    }

    private fun saveContactInRD(name: String, contact: String, address: String, id: String) {
        if (name.isEmpty()) {
            binding.etName.error = "Please enter name"
        }
        if (contact.isEmpty()) {
            binding.etContact.error = "Please enter contact"
        }
        if (address.isEmpty()) {
            binding.etAddress.error = "Please enter address"
        }

        if (name.isNotEmpty() && contact.isNotEmpty() && address.isNotEmpty()) {
            val contactData = Contact(id, name, contact, address)
            databaseReference.child(id).setValue(contactData)
                .addOnCompleteListener {
                    Toast.makeText(this, "Contact saved successfully!!!", Toast.LENGTH_SHORT).show()
                    binding.etName.text?.clear()
                    binding.etContact.text?.clear()
                    binding.etAddress.text?.clear()
                    resetFocus()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Failed to save contact: ${it.message}!!!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        } else {
            Toast.makeText(this, "Please fill all fields!!!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetFocus() {
        binding.etName.clearFocus()
        binding.etContact.clearFocus()
        binding.etAddress.clearFocus()
    }
}