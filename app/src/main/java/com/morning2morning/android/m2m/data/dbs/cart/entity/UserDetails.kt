package com.morning2morning.android.m2m.data.dbs.cart.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Entity(tableName = "user_details_table")
data class UserDetails(
    @PrimaryKey(autoGenerate = false)
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
)
