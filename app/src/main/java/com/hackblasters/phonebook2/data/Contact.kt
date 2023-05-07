package com.hackblasters.phonebook2.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "contacts")
data class Contact(@PrimaryKey(autoGenerate = true) val id: Long,
                   val name: String,
                   val number: String,
                   val address: String,
                   val photo: String)