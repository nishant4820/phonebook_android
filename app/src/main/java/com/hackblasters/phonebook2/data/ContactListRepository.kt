package com.hackblasters.phonebook2.ui

import android.app.Application
import androidx.lifecycle.LiveData
import com.hackblasters.phonebook2.data.Contact


class ContactListRepository(context: Application){
    private val contactListDao: ContactListDao =
        ContactDatabase.getDatabase(context).contactListDao()

    fun getContacts(): LiveData<List<Contact>> =
        contactListDao.getContacts()

    suspend fun insertContacts(contacts: List<Contact>){
        contactListDao.insertContacts(contacts)
    }

    suspend fun getContactList(): List<Contact>{
        return contactListDao.getContactList()
    }

    suspend fun getSearchResult(search: String): List<Contact> {
        return contactListDao.getSearchResult(search)
    }

}