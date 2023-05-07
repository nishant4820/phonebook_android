package com.hackblasters.phonebook2.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hackblasters.phonebook2.data.Contact

class ContactListViewModel(application: Application): AndroidViewModel(application){
    private val repo: ContactListRepository =
        ContactListRepository(application)

    val contacts: LiveData<List<Contact>> = repo.getContacts()

    suspend fun insertContacts(contacts: List<Contact>){
        repo.insertContacts(contacts)
    }

    suspend fun getContactList(): List<Contact>{
        return repo.getContactList()
    }

    suspend fun getSearchResult(search: String): List<Contact> {
        return repo.getSearchResult(search)
    }
}