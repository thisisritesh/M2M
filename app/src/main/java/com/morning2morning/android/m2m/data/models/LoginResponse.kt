package com.morning2morning.android.m2m.data.models

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
data class LoginResponse(
    val code: Int,
    val msg: String,
    val result: List<Result>
){
    data class Result(
        val address: String,
        val created: String,
        val email: String,
        val id: String,
        val mobile: String,
        val name: String,
        val password: String,
        val status: String
    )
}