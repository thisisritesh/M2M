package com.morning2morning.android.m2m.data.models

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
data class M2mProductsResponse(
    val code: Int,
    val product: Product
){
    data class Product(
        val count: Int,
        val product: List<ProductX>
    ){
        data class ProductX(
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
            val title: String
        )
    }
}