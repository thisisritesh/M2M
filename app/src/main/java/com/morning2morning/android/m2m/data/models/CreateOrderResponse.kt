package com.morning2morning.android.m2m.data.models

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
data class CreateOrderResponse(
    val code: Int,
    val msg: String,
    val result: Result
){
    data class Result(
        val amount: String,
        val status: String,
        val transaction_id: String,
        val user_id: String
    )
}
/*
https://morning2morning.in/m2mapi/v1/write/verifyorder?razorpay_order_id=order_JbqBOKhrFomLAT&razorpay_payment_id=1&razorpay_signature=123
 */