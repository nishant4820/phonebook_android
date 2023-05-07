package com.hackblasters.phonebook2.ui

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hackblasters.phonebook2.ui.ContactDetailDao
import com.hackblasters.phonebook2.ui.ContactListDao
import com.hackblasters.phonebook2.data.Contact

@Database(entities = [Contact::class], version = 1)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactListDao(): ContactListDao
    abstract fun contactDetailDao(): ContactDetailDao

    companion object {
        @Volatile
        private var instance: ContactDatabase? = null

        fun getDatabase(context: Context) = instance
            ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "contact_database"
                ).build().also { instance = it }
            }
    }
}
