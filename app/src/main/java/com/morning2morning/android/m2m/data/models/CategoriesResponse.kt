package com.morning2morning.android.m2m.data.models

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
data class CategoriesResponse(
    val code: Int,
    val result: List<Result>
){
    data class Result(
        val created: String,
        val id: String,
        val image: String,
        val name: String,
        val order_index: String,
        val status: String
    ){
        var isExpanded = false
    }
}