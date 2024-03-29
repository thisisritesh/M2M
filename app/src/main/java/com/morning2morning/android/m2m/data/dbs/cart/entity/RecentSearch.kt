package com.morning2morning.android.m2m.data.dbs.cart.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Entity(tableName = "recent_search_table")
data class RecentSearch(

    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String

)