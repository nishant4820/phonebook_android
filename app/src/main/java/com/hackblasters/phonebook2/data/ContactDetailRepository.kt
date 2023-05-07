package com.hackblasters.phonebook2.ui

import android.app.Application
import androidx.lifecycle.LiveData
import com.hackblasters.phonebook2.data.Contact


class ContactDetailRepository(context: Application){
    private val contactDetailDao: ContactDetailDao = ContactDatabase.getDatabase(context).contactDetailDao()

    fun getContact(id: Long): LiveData<Contact> {
        return contactDetailDao.getContact(id)
    }

    suspend fun insertContact(contact: Contact): Long{
        return contactDetailDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact){
        contactDetailDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact){
        contactDetailDao.deleteContact(contact)
    }
}