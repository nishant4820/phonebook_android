package com.hackblasters.phonebook2.ui


import android.app.Application
import androidx.lifecycle.*
import com.hackblasters.phonebook2.data.Contact
import kotlinx.coroutines.launch

class ContactDetailViewModel(application: Application): AndroidViewModel(application){
    private val repo: ContactDetailRepository =
        ContactDetailRepository(application)

    private val _contactId = MutableLiveData<Long>(0)

    val contactId: LiveData<Long>
        get() = _contactId

    val contact: LiveData<Contact> = Transformations
        .switchMap(_contactId) { id ->
            repo.getContact(id)
        }

    fun setContactId(id: Long){
        if(_contactId.value != id ) {
            _contactId.value = id
        }
    }

    fun saveContact(contact: Contact){
        viewModelScope.launch {
            if (_contactId.value == 0L) {
                _contactId.value = repo.insertContact(contact)
            } else {
                repo.updateContact(contact)
            }
        }
    }

    fun deleteContact(){
        viewModelScope.launch {
            contact.value?.let { repo.deleteContact(it) }
        }
    }

}