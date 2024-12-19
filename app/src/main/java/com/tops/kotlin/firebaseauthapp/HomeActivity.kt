package com.tops.kotlin.firebaseauthapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.tops.kotlin.firebaseauthapp.databinding.ActivityHomeBinding
import com.tops.kotlin.firebaseauthapp.databinding.ContactUpdateDialogBinding
import com.tops.kotlin.firebaseauthapp.models.Contact

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var contacts: MutableList<Contact>
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val auth = Firebase.auth
        val user = auth.currentUser?.displayName ?: firebaseAuth.currentUser?.email

        val toolbar: MaterialToolbar = binding.materialToolbar

        setSupportActionBar(toolbar)

        if (user != null) {
            Toast.makeText(this, "Hello, $user", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "User is not found!!!", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddContact.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
        }

        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        contacts = mutableListOf()
        getContacts()
    }

    private fun getContacts() {
        binding.rvContacts.visibility = View.GONE
        binding.progressCircular.visibility = View.VISIBLE

        databaseReference = FirebaseDatabase.getInstance().getReference("contacts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contacts.clear()
                if (snapshot.exists()) {
                    for (contactSnapshot in snapshot.children) {
                        val contact = contactSnapshot.getValue(Contact::class.java)
                        contact?.let { contacts.add(it) }
                    }
                    val contactAdapter =
                        ContactAdapter(this@HomeActivity, contacts, onItemClick = { contact ->
                            showUpdateDialog(contact)
                        })
                    binding.rvContacts.adapter = contactAdapter

                    binding.rvContacts.visibility = View.VISIBLE
                    binding.progressCircular.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "No Data Found!!!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showUpdateDialog(contact: Contact) {
        val dialog = Dialog(this)
        val dialogBinding =
            ContactUpdateDialogBinding.inflate(LayoutInflater.from(this), null, false)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.etName.setText(contact.name)
        dialogBinding.etContact.setText(contact.contact)
        dialogBinding.etAddress.setText(contact.address)
        dialogBinding.btnUpdate.setOnClickListener {
            val name = dialogBinding.etName.text.toString()
            val contactNumber = dialogBinding.etContact.text.toString()
            val address = dialogBinding.etAddress.text.toString()
            if (name.isNotEmpty() && contactNumber.isNotEmpty() && address.isNotEmpty()) {
                val updatedContact = Contact(contact.id, name, contactNumber, address)
                databaseReference.child(contact.id.toString()).setValue(updatedContact)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Contact updated successfully!!!", Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                        getContacts()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to update contact!!!", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialogBinding.btnDelete.setOnClickListener {
            val id = contact.id
            databaseReference.child(id.toString()).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Contact deleted successfully!!!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                getContacts()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to delete contact!!!", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            (resources.displayMetrics.heightPixels * 0.45).toInt()
        )
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                firebaseAuth.signOut()

                googleSignInClient.signOut().addOnCompleteListener {
                    Toast.makeText(this, "Logout successfully!!!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}