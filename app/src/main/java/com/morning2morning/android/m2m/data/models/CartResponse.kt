package com.morning2morning.android.m2m.data.models

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
data class CartResponse(
    val code: Int,
    val result: List<Result>
){
    data class Result(
        val banner: String,
        val best: String,
        val cat_id: String,
        val created: String,
        val description: String,
        val disscount: String,
        val disscounted_price: String,
        val featured: String,
        val id: String,
        val image: String,
        val order_index: String,
        val price: String,
        val quantity: String,
        val status: String,
        val title: String,
        val total: String
    )
}