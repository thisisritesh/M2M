package com.morning2morning.android.m2m.ui.checkout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.models.CartTransactionResponse
import com.morning2morning.android.m2m.data.models.CreateOrderResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.databinding.ActivityCheckoutBinding
import com.morning2morning.android.m2m.ui.map.MapsActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.ProgressBarUtil
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.ui.Utils
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class CheckoutActivity : AppCompatActivity(), PaymentResultWithDataListener,
    ExternalWalletListener {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var apiClient: ApiClient
    private var totalDiscountedPrice = 0

    private lateinit var emailId: String
    private lateinit var phoneNumber: String
    private lateinit var userId: String

    private lateinit var address: String

    companion object{
        private const val TAG = "CheckoutActivity"
    }

    private var progressDialogUtil = ProgressBarUtil()
    
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_checkout)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiClient = NetworkHelper.getInstance(this)?.create(ApiClient::class.java)!!

        binding.backArrow.setOnClickListener {
            finish()
        }

        binding.originalPriceTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG


        binding.addNewLocation.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(this@CheckoutActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@CheckoutActivity){
                    if (it.isNotEmpty()){
                        val totalPrice = Utils.calculateTotalPrice(it)
                        totalDiscountedPrice = Utils.calculateDiscountedTotalPrice(it)
                        val discount = totalPrice - totalDiscountedPrice
                        val noOfItems = it.size

                        binding.originalPriceTextView.text = "\u20B9 $totalPrice"
                        binding.discountedPriceTextView.text = "\u20B9 $discount"
                        binding.quantityTextView.text = "$noOfItems items"
                        binding.totalPayableAmountTextView.text = "\u20B9 $totalDiscountedPrice"

                        binding.proceedToPayButton.text =
                            "Proceed to pay \u20B9 $totalDiscountedPrice"

                    }
                }
            }

        }



        userId = Pref(this@CheckoutActivity).getPrefString(Constants.USER_ID)
        emailId = Pref(this@CheckoutActivity).getPrefString(Constants.USER_EMAIL_ID)
        phoneNumber = Pref(this@CheckoutActivity).getPrefString(Constants.USER_PHONE_NUMBER)
        val displayName = Pref(this@CheckoutActivity).getPrefString(Constants.USER_DISPLAY_NAME)


        binding.proceedToPayButton.setOnClickListener {
            val sb = StringBuilder()
            lifecycleScope.launch(Dispatchers.IO){
                val list = CartDB.getInstance(this@CheckoutActivity).dao().getAllCartItems()

                for (item in list){
                    for (i in 0 until item.itemCount){
                        sb.append(item.id + ",")
                    }
                }

                withContext(Dispatchers.Main){
                    createOrderWithRazorpayRequest(userId,totalDiscountedPrice.toString() /*"1"*/,sb.toString().substring(0,sb.toString().length - 1))
                }


            }





//            verifyOrderWithRazorpay("order_JdL0O91OAICnEm","pay_JdL0dikAblypW5","6626b2b6e071c486471696cd8d64268f0643cbd89b377463245862d13a53d4f4")
        }

        if (emailId.isNotEmpty()){
            binding.emailEditText.setText(emailId)
        }

        if (phoneNumber.isNotEmpty()){
            binding.phoneNumberEditText.setText(phoneNumber)
        }

        if (displayName.isNotEmpty()){
            binding.nameEditText.setText(displayName)
        }


        
        
    }


    override fun onResume() {
        super.onResume()
        address = Pref(this@CheckoutActivity).getPrefString(Constants.USER_DELIVERY_ADDRESS)

        if (address.isNotEmpty()){
            binding.addressEditText.setText(address)
        }
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        finish()
    }

    private fun paymentRequest(amountStr: Int, email: String, phoneNumber: String, transactionId: String){
        val activity: Activity = this
        val checkout = Checkout()

        checkout.setKeyID(PaymentGatewayConstants.RAZORPAY_KEY_ID)



        try {
            val options = JSONObject()

            options.put("name","Morning 2 Morning")
            options.put("description","This is description")
            options.put("image",R.mipmap.high_resolution_horizon_m2m_logo)
            options.put("order_id",transactionId)
            options.put("currency","INR")
            options.put("amount",amountStr)
            options.put("send_sms_hash",true)
            options.put("theme.color","#3157B9")

            val prefill = JSONObject()
            prefill.put("email",email)
            prefill.put("contact",phoneNumber)

            options.put("prefill",prefill)

            checkout.open(activity,options)
        } catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            Log.e(TAG, "paymentRequest: ", e)
            e.printStackTrace()
        }


    }

    /*
    API - 1
    Create order with razorpay
    Endpoint -> /write/createorder
    Method -> POST
    Params -> user_id, amount

    API - 2
    Verify order with razorpay
    Endpoint -> /write/verifyorder
    Method -> GET
    Params -> razorpay_order_id, razorpay_payment_id, razorpay_signature_id
     */

    private fun verifyOrderWithRazorpay(orderId: String, paymentId: String, signatureId: String){
        lifecycleScope.launch(Dispatchers.IO){
            val loginCall = apiClient.verifyOrderWithRazorpay(orderId,paymentId, signatureId)

            loginCall.enqueue(object : Callback<CartTransactionResponse> {
                override fun onResponse(call: Call<CartTransactionResponse>, response: Response<CartTransactionResponse>) {
                    if (response.isSuccessful && response.code() == 200){
                        if (response.body()?.code == 1){


                            clearCartRequest(userId)

                        } else {
                            CustomToast().showToastMessage(this@CheckoutActivity,"Failed")
                        }
                    }
                }

                override fun onFailure(call: Call<CartTransactionResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ", t)
                }

            })

        }
    }


    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        verifyOrderWithRazorpay(p1?.orderId!!,p1.paymentId,p1.signature)
//        CustomToast().showToastMessage(this,"Success")
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Log.e(TAG, "onPaymentError: $p1")
//        CustomToast().showToastMessage(this,"Error")

        /*
        {"error":{"code":"BAD_REQUEST_ERROR","description":"Payment processing cancelled by user","source":"customer","step":"payment_authentication","reason":"payment_cancelled"}}
         */

        try {
            val jsonObject = JSONObject(p1)
            val errorObj = jsonObject.optJSONObject("error")
            val reason = errorObj.optString("code")
            if (reason == "BAD_REQUEST_ERROR"){
                CustomToast().showToastMessage(this,"Payment processing cancelled by user")
            }
        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {
        CustomToast().showToastMessage(this,"External wallet selected")
    }

    private fun createOrderWithRazorpayRequest(userId: String, amount: String, products: String){

        progressDialogUtil.showProgressDialog(this,"Please wait...")

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("amount",amount)
            .addFormDataPart("products",products)
            .build()

        lifecycleScope.launch(Dispatchers.IO){
            val call = apiClient.createOrderWithRazorpay(requestBody)

            call.enqueue(object : Callback<CreateOrderResponse> {
                override fun onResponse(call: Call<CreateOrderResponse>, response: Response<CreateOrderResponse>) {
                    val res = response.body()!!
                    if (res.code == 1){
                        progressDialogUtil.dismissProgressDialog()
//                        val amount = Math.round(res.result.amount.toFloat()) * 100
                        val amount = Math.round(1F) * 100

                        Log.e(TAG, "onResponse: $res")
                        paymentRequest(amount,emailId,phoneNumber,res.result.transaction_id)
                    }
                }

                override fun onFailure(call: Call<CreateOrderResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ", t)
                }

            })

        }

    }


    private fun clearCartRequest(userId: String){
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId)
            .build()

        lifecycleScope.launch(Dispatchers.IO){
            apiClient.clearCart(requestBody).enqueue(object : Callback<CartTransactionResponse>{
                override fun onResponse(
                    call: Call<CartTransactionResponse>,
                    response: Response<CartTransactionResponse>
                ) {
                    if (response.body()?.code == 1){

                        lifecycleScope.launch(Dispatchers.IO){
                            CartDB.getInstance(this@CheckoutActivity).clearAllTables()

                            withContext(Dispatchers.Main){
                                CustomToast().showToastMessage(this@CheckoutActivity,"Success")
                                finish()
                            }

                        }
                    }
                }

                override fun onFailure(call: Call<CartTransactionResponse>, t: Throwable) {

                }

            })
        }
    }


}