package com.morning2morning.android.m2m.data.repositories

import android.content.Context
import com.morning2morning.android.m2m.data.callbacks.CartTransactionListener
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.models.CartTransactionResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.ui.Utils
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
object CartTransactionRepository {

    fun addToCart(productId: String, cartTransactionListener: CartTransactionListener, context: Context){

        if (Utils.isUserLoggedIn(context)){
            val userId = Pref(context).getPrefString(Constants.USER_ID)
            val apiClient = NetworkHelper.getInstance(context)!!.create(ApiClient::class.java)
            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart("product_id", productId)
                .build()
            apiClient.addToCart(requestBody).enqueue(object : Callback<CartTransactionResponse>{
                override fun onResponse(call: Call<CartTransactionResponse>, response: Response<CartTransactionResponse>) {
                    if (response.body()!!.code == 1){
                        cartTransactionListener.onTransactionSuccess()
                    } else {
                        cartTransactionListener.onTransactionFailed()
                    }
                }

                override fun onFailure(call: Call<CartTransactionResponse>, t: Throwable) {
                    cartTransactionListener.onTransactionFailed()
                }

            })
        }

    }

    fun removeFromCart(productId: String, cartTransactionListener: CartTransactionListener, context: Context){
        if (Utils.isUserLoggedIn(context)) {
            val userId = Pref(context).getPrefString(Constants.USER_ID)
            val apiClient = NetworkHelper.getInstance(context)!!.create(ApiClient::class.java)
            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart("product_id", productId)
                .build()

            apiClient.removeFromCart(requestBody)
                .enqueue(object : Callback<CartTransactionResponse> {
                    override fun onResponse(
                        call: Call<CartTransactionResponse>,
                        response: Response<CartTransactionResponse>
                    ) {
                        if (response.body()!!.code == 1) {
                            cartTransactionListener.onTransactionSuccess()
                        } else {
                            cartTransactionListener.onTransactionFailed()
                        }
                    }

                    override fun onFailure(call: Call<CartTransactionResponse>, t: Throwable) {
                        cartTransactionListener.onTransactionFailed()
                    }

                })
        }
    }

}