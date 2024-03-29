package com.morning2morning.android.m2m.data.work

import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.google.gson.JsonSyntaxException
import com.morning2morning.android.m2m.data.callbacks.CartTransactionListener
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.data.models.CartResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.repositories.CartTransactionRepository
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class CartWorker(val context : Context, params : WorkerParameters)
    : Worker(context, params) {

    companion object{
        private const val TAG = "CartWorker"
    }

    override fun doWork(): Result {
        fetchUserCartList()
        return Result.success()
    }


    private fun fetchUserCartList() {

        val userId = Pref(context).getPrefString(Constants.USER_ID)

        NetworkHelper.getInstance(context)?.create(ApiClient::class.java)!!.getCartData(userId).enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {

                Log.d(TAG, "onResponse:fetchUserCartListAndSaveToLocalDb " + response.body())

                if (response.body()?.result?.isNotEmpty()!!){
                    GlobalScope.launch(Dispatchers.IO){

                        val list = CartDB.getInstance(context).dao().getAllCartItems()

                        for (cart in response.body()?.result!!) {
                            for(item in list){
                                if (item.id == cart.id){
                                    if (item.itemCount > cart.total.toInt()){
                                        for(i in cart.total.toInt() until item.itemCount){
                                            CartTransactionRepository.addToCart(item.id, object : CartTransactionListener {
                                                override fun onTransactionSuccess() {

                                                }

                                                override fun onTransactionFailed() {

                                                }

                                            },context)
                                        }
                                        CartDB.getInstance(context).clearAllTables()
                                        // fetch user cart list and save to local DB
                                        fetchUserCartListAndSaveToLocalDb()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    uploadCartListToRemoteServer()
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                if (t is JsonSyntaxException){
                    uploadCartListToRemoteServer()
                }
                Log.e(TAG, "onFailure:fetchUserCartListAndSaveToLocalDb ",t)
            }

        })

    }

    private fun fetchUserCartListAndSaveToLocalDb() {
        val userId = Pref(context).getPrefString(Constants.USER_ID)

        NetworkHelper.getInstance(context)?.create(ApiClient::class.java)!!.getCartData(userId).enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                if (response.body()?.result?.isNotEmpty()!!){
                    GlobalScope.launch(Dispatchers.IO){
                        for (cart in response.body()?.result!!) {
                            val cartItem = CartItem(cart.banner,cart.best,cart.cat_id,cart.created,cart.description,cart.disscount,cart.disscounted_price,cart.featured,
                                cart.id,cart.image,cart.order_index,cart.price,cart.quantity,0,cart.status,cart.title,cart.total.toInt(), Utils.calculateTotalPrice(cart.total.toInt(),cart.price.toInt())
                                , cart.total.toInt() * cart.disscounted_price.toInt())

                            Log.d(TAG, "Saving cart item to local DB  $cartItem")

                            CartDB.getInstance(context)
                                .dao()
                                .addItemToCart(cartItem)
                        }
                    }
                } else {
                    uploadCartListToRemoteServer()
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                Log.e(TAG, "onFailure:fetchUserCartListAndSaveToLocalDb ",t)
            }

        })
    }


    private fun uploadCartListToRemoteServer(){
        GlobalScope.launch(Dispatchers.IO){
            val list: List<CartItem> = CartDB.getInstance(context).dao().getAllCartItems()

            for (item in list){
                for(i in 0 until item.itemCount){
                    CartTransactionRepository.addToCart(item.id, object : CartTransactionListener {
                        override fun onTransactionSuccess() {

                        }

                        override fun onTransactionFailed() {

                        }

                    },context)
                }
            }
//            fetchUserCartListAndSaveToLocalDb()
//            Pref(requireContext()).putPref(Constants.IS_NEED_CART_TO_BE_SYNC,false)
        }
    }


}