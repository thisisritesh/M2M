package com.morning2morning.android.m2m.data.dbs.cart.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Entity(tableName = "product_table")
data class CartItem(

    val banner: String,
    val best: String,
    val cat_id: String,
    val created: String,
    val description: String,
    val disscount: String,
    val disscounted_price: String,
    val featured: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val image: String,
    val order_index: String,
    val price: String,
    val quantity: String,
    val rating: Int,
    val status: String,
    val title: String,
    // New Fields
    var itemCount: Int,
    var totalPrice: Int,
    var totalDiscountedPrice: Int

){
    var isExpanded = false
}