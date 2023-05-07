package com.hackblasters.phonebook2.ui

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hackblasters.phonebook2.data.Contact


@Dao
interface ContactDetailDao{
    @Query("SELECT * FROM contacts WHERE `id` = :id")
    fun getContact(id: Long): LiveData<Contact>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: Contact): Long

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

}