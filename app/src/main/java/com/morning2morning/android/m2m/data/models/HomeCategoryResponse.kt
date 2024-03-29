package com.morning2morning.android.m2m.data.models

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
data class HomeCategoryResponse(
    val categories: Categories,
    val code: Int
){
    data class Categories(
        val category: List<Category>,
        val count: Int
    ){
        data class Category(
            val created: String,
            val id: String,
            val image: String,
            val name: String,
            val order_index: String,
            val products: Products,
            val status: String
        ){
            data class Products(
                val count: Int,
                val product: List<Product>?
            ){
                data class Product(
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
    }
}