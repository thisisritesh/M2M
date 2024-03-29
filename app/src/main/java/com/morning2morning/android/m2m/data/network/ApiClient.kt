package com.morning2morning.android.m2m.data.network

import com.morning2morning.android.m2m.data.models.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
interface ApiClient {

    @GET("read/featurebanner")
    suspend fun getHomeBannerData() : HomeBannerResponse

    // home?cat_count=3&cat_offset=0&product_count=2&product_offset=0
    @GET("read/home")
    suspend fun getHomeCategoryData(@Query("cat_count") cat_count: Int,
                                    @Query("cat_offset") cat_offset: Int,
                                    @Query("product_count") product_count: Int,
                                    @Query("product_offset") product_offset: Int) : HomeCategoryResponse


    // https://morning2morning.in/m2mapi/v1/read/categories
    @GET("read/categories")
    suspend fun getCategories() : CategoriesResponse

    // product?cat_id=1&product_count=1&offset=0
    @GET("read/product")
    suspend fun getProductsListByCatId(@Query("cat_id") cat_id: String, @Query("product_count") product_count: Int, @Query("product_offset") offset: Int) : ProductsListByCatIdResponse

    // https://morning2morning.in/m2mapi/v1/read/product?title=tv&product_count=1&offset=1
    @GET("read/product")
    suspend fun getSearchResult(@Query("title") title: String, @Query("product_count") product_count: Int, @Query("offset") offset: Int) : SearchResponse

    // product?best=1&product_count=2&product_offset=0
    @GET("read/product")
    suspend fun getM2mProducts(@Query("best") best: Int, @Query("product_count") product_count: Int, @Query("product_offset") offset: Int) : M2mProductsResponse

    // https://morning2morning.in/m2mapi/v1/read/product?id=43
    @GET("read/product")
    suspend fun getProductDetails(@Query("id") id: String) : ProductDetailsResponse

    // https://morning2morning.in/m2mapi/v1/write/mobilelogin
    @POST("write/mobilelogin")
    fun loginWithPhoneNumber(@Body body: RequestBody) : Call<LoginResponse>

    //https://morning2morning.in/m2mapi/v1/write/addtocart
    @POST("write/addtocart")
    fun addToCart(@Body body: RequestBody) : Call<CartTransactionResponse>

    //https://morning2morning.in/m2mapi/v1/write/clearcart
     @POST("write/clearcart")
    fun removeFromCart(@Body body: RequestBody) : Call<CartTransactionResponse>

    //https://morning2morning.in/m2mapi/v1/write/updateProfile
     @POST("write/updateProfile")
    fun updateUserProfile(@Body body: RequestBody) : Call<CartTransactionResponse>

    //https://morning2morning.in/m2mapi/v1/write/clearcart
    @POST("write/clearcart")
    fun clearCart(@Body body: RequestBody) : Call<CartTransactionResponse>

    //https://morning2morning.in/m2mapi/v1/read/getcartList?user_id=5
    @GET("read/getcartList")
    fun getCartData(@Query("user_id") userId: String) : Call<CartResponse>

    @POST("write/createorder")
    fun createOrderWithRazorpay(@Body body: RequestBody) : Call<CreateOrderResponse>

    //https://morning2morning.in/m2mapi/v1/write/verifyorder?
    // razorpay_order_id=order_JbqBOKhrFomLAT&
    // razorpay_payment_id=1&
    // razorpay_signature=123
    @GET("write/verifyorder")
    fun verifyOrderWithRazorpay(@Query("razorpay_order_id") razorpayOrderId: String, @Query("razorpay_payment_id") razorpayPaymentId: String, @Query("razorpay_signature") razorpaySignatureId: String) : Call<CartTransactionResponse>


}