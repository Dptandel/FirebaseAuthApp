package com.tops.kotlin.firebaseauthapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.tops.kotlin.firebaseauthapp.databinding.ContactItemBinding
import com.tops.kotlin.firebaseauthapp.models.Contact

class ContactAdapter(
    private val context: Context,
    private val contacts: MutableList<Contact>,
    private val onItemClick: ((contact: Contact) -> Unit)? = null
) :
    Adapter<ContactAdapter.ContactViewHolder>() {
    inner class ContactViewHolder(var binding: ContactItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ContactItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.binding.tvContactName.text = contact.name
        holder.binding.tvContact.text = contact.contact

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(contact)
        }
    }
}