package com.hackblasters.phonebook2.ui

import android.net.Uri
import com.hackblasters.phonebook2.R


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hackblasters.phonebook2.data.Contact
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.contact_item.*
import kotlinx.android.synthetic.main.contact_item.contact_name
import kotlinx.android.synthetic.main.contact_item.contact_number
import kotlinx.android.synthetic.main.contact_item.contact_name
import kotlinx.android.synthetic.main.contact_item.contact_photo


class ContactAdapter(private val listener: (Long) -> Unit):
    ListAdapter<Contact, ContactAdapter.ViewHolder>(
        DiffCallback()
    ){

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)

        return ViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder (override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        init{
            itemView.setOnClickListener{
                listener.invoke(getItem(adapterPosition).id)
            }
        }

        fun bind(contact: Contact){
            with(contact) {

                contact_name.text = name
                contact_number.text = number
                with(photo){
                    if(isNotEmpty()){
                        contact_photo.setImageURI(Uri.parse(this))
                    } else{
                        contact_photo.setImageResource(R.drawable.blank_photo)
                    }
                }
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }
}