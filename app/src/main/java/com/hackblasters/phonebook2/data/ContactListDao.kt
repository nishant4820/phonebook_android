package com.hackblasters.phonebook2.ui

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hackblasters.phonebook2.data.Contact


@Dao
interface ContactListDao{
    @Query("SELECT * FROM contacts ORDER BY name")
    fun getContacts(): LiveData<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Query("SELECT * FROM contacts ORDER BY name")
    suspend fun getContactList(): List<Contact>

    @Query("SELECT * FROM contacts WHERE name LIKE :search ")
    suspend fun getSearchResult(search: String): List<Contact>
}