package com.morning2morning.android.m2m.ui.cart

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.CartAdapter
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.databinding.ActivityCartBinding
import com.morning2morning.android.m2m.ui.checkout.CheckoutActivity
import com.morning2morning.android.m2m.ui.login.LoginBottomSheet
import com.morning2morning.android.m2m.ui.productdetails.ProductDetailsActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class CartActivity : AppCompatActivity(), CartAdapter.AdapterCallbacks, OnConnectionChanged {

    private lateinit var binding: ActivityCartBinding
    private var list: List<CartItem> = listOf()

    private var isConnectedToInternet = true

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ConnectivityManager.init(this,this,this)

        Handler(Looper.myLooper()!!)
            .postDelayed(Runnable {
                if (isConnectedToInternet){
                    showSuccessUi()
                    fetchCartDetailsFromLocalServer()
                } else {
                    showNoInternetConnectionUi()
                }
            }, 100)


        binding.backArrow.setOnClickListener {
            finish()
        }


        binding.originalPriceSumTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG


        binding.proceedText.setOnClickListener {
            if (Utils.isUserLoggedIn(this)){
                startActivity(Intent(this,CheckoutActivity::class.java))
                overridePendingTransition(R.anim.right_in, R.anim.left_out)
            } else {
                LoginBottomSheet().show(supportFragmentManager,"LoginTag")
            }
        }


        lifecycleScope.launch(Dispatchers.IO){

            val liveData = CartDB.getInstance(this@CartActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){

                liveData.observe(this@CartActivity){

                    if (it.isEmpty()){
                        binding.emptyView.visibility = View.VISIBLE
                        binding.cartDetailsCard.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.cartDetailsCard.visibility = View.VISIBLE
                        binding.itemCountTextView.text = "${it.size} items"
                        binding.discountedPriceSumTextView.text = "\u20B9" +" " + Utils.calculateDiscountedTotalPrice(it)
                        binding.originalPriceSumTextView.text = "\u20B9" +" " + Utils.calculateTotalPrice(it)
                    }
                }

            }
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        finish()
    }

    private fun showNoInternetConnectionUi() {
        binding.noConnectionView.visibility = View.VISIBLE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.cartDetailsCard.visibility = View.GONE
    }

    private fun showSuccessUi() {
        binding.shimmerViewContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
    }


    @SuppressLint("SetTextI18n")
    private fun fetchCartDetailsFromLocalServer() {

        lifecycleScope.launch(Dispatchers.IO){
            list = CartDB.getInstance(this@CartActivity)
                .dao()
                .getAllCartItems()

            val liveData = CartDB.getInstance(this@CartActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){

                val adapter = CartAdapter(list,this@CartActivity,this@CartActivity)
                binding.recyclerView.adapter = adapter

                liveData.observe(this@CartActivity){
                    adapter.setData(it)

                    if (it.isEmpty()){
                        binding.emptyView.visibility = View.VISIBLE
                        binding.cartDetailsCard.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.cartDetailsCard.visibility = View.VISIBLE
                        binding.itemCountTextView.text = "${it.size} items"
                        binding.discountedPriceSumTextView.text = "\u20B9" +" " + Utils.calculateDiscountedTotalPrice(it)
                        binding.originalPriceSumTextView.text = "\u20B9" +" " + Utils.calculateTotalPrice(it)
                    }

                }

            }
        }



    }




    override fun onItemClicked(productId: String) {
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtra(Constants.PRODUCT_ID,productId)
        overridePendingTransition(R.anim.right_in,R.anim.left_out)
        startActivity(intent)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
    }
}