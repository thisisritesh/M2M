package com.morning2morning.android.m2m.ui.productdetails

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.callbacks.CartTransactionListener
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.data.models.ProductDetailsResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.CartTransactionRepository
import com.morning2morning.android.m2m.data.repositories.ProductDetailsRepository
import com.morning2morning.android.m2m.databinding.ActivityProductDetailsBinding
import com.morning2morning.android.m2m.ui.cart.CartActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.ui.Utils
import com.morning2morning.android.m2m.utils.ui.animations.Animations
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class ProductDetailsActivity : AppCompatActivity(), OnConnectionChanged {

    private lateinit var viewModel: ProductDetailsViewModel

    private lateinit var binding: ActivityProductDetailsBinding

    private var haveToUpdate: Boolean = false

    private var isConnectedToInternet = true

    private lateinit var productId: String

    companion object{
        public const val TAG = "ProductDetailsFragment"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_product_details)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productId = if (intent != null){
            intent.getStringExtra(Constants.PRODUCT_ID)!!
        } else {
            ""
        }

        ConnectivityManager.init(this,this,this)

        val apiClient = NetworkHelper.getInstance(this)?.create(ApiClient::class.java)!!

        val viewModelFactory = ProductDetailsViewModelFactory(ProductDetailsRepository(apiClient,productId))
        val viewModel = ViewModelProvider(this,viewModelFactory)[ProductDetailsViewModel::class.java]

        lifecycleScope.launch(Dispatchers.IO){
            val list = CartDB.getInstance(this@ProductDetailsActivity)
                .dao()
                .getAllCartItems()

            for (item in list){
                if (item.id == productId){
//                    itemCount = item.itemCount
                    haveToUpdate = true
                    withContext(Dispatchers.Main){
                        binding.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                        binding.removeLl.visibility = View.VISIBLE
                        binding.quantityIndicatorTextView.text = item.itemCount.toString()
                    }
                }
            }
        }

        viewModel.getProductDetailsData()

        viewModel.productDetailsData.observe(this){
            when(it){
                is NetworkState.Loading -> {
                    showLoadingUi()
                }
                is NetworkState.Failed -> {
                    if (it.message == NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE){
                        showNoInternetConnectionUi()
                    } else {
                        showErrorUi()
                    }
                }
                is NetworkState.Success -> {
                    showSuccessUi()
                    if (it.data.code == 1){
                        setData(it.data.product.product[0])
                    }
                }
            }
        }

        binding.backArrow.setOnClickListener {
            finish()
        }

        binding.proceedText.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }

        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(this@ProductDetailsActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@ProductDetailsActivity){
                    if (it.isEmpty()) {
                        binding.cartDetailsCard.visibility = View.GONE
                    } else {
                        binding.cartDetailsCard.visibility = View.VISIBLE
                        binding.itemCountTextView.text = "${it.size} items"
                        binding.discountedPriceSumTextView.text = "\u20B9" +" " + Utils.calculateDiscountedTotalPrice(it)
                        binding.originalPriceSumTextView.text = "\u20B9" +" " + Utils.calculateTotalPrice(it)
                    }
                }
            }

        }

    }


    private fun showErrorUi() {

    }

    private fun showNoInternetConnectionUi() {

    }


    private fun showLoadingUi(){
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.successUiRoot.visibility = View.GONE
    }


    private fun showSuccessUi(){
        binding.shimmerViewContainer.visibility = View.GONE
        binding.successUiRoot.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun setData(data: ProductDetailsResponse.Product.ProductX){

        if (data.image.isNotEmpty()){
            Glide.with(this)
                .load(data.image)
                .placeholder(R.mipmap.placeholder_portrait)
                .into(binding.productImageView)
        } else {
            Glide.with(this)
                .load(data.banner)
                .placeholder(R.mipmap.placeholder_portrait)
                .into(binding.productImageView)
        }

        if (data.title.isNotEmpty()){
            binding.productTitleTextView.text = data.title
        }

        if (data.quantity.isNotEmpty()){
            binding.quantityTextView.text = data.quantity
        }

        if (data.disscounted_price.isNotEmpty()){
            binding.discountedPriceTextView.text = "\u20B9" +" " + data.disscounted_price
        }

        if (data.price.isNotEmpty()){
            binding.priceTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            binding.priceTextView.text = "\u20B9" +" " + data.price
        }

        if (data.disscount.isNotEmpty()){
            binding.offerTextView.text = data.disscount + "% off"
        }

        if (data.description.isNotEmpty()){
            binding.descriptionTextView.text = data.description
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val list = CartDB.getInstance(this@ProductDetailsActivity)
                .dao()
                .getAllCartItems()

            var foundItem: CartItem? = null

            for (item in list) {
                if (item.id == productId) {
                    foundItem = item
                }
            }

            if (foundItem?.id == productId) {
                val cartItem: CartItem =
                    CartDB.getInstance(this@ProductDetailsActivity).dao().getCartItemById(productId)
                withContext(Dispatchers.Main) {

                    // Show expanded view
                    binding?.quantityIndicatorTextView?.text = cartItem.itemCount.toString()
                    toggleLayout(true, binding?.removeLl!!)
//                    holder.totalPriceTv.text = "\u20B9" +" " + cartItem.totalPrice
                    bind(cartItem, data)
                }
            } else {
                withContext(Dispatchers.Main) {

                    toggleLayout(false, binding?.removeLl!!)

                    val newCartItem = CartItem(
                        "",
                        data.best,
                        data.cat_id,
                        data.created,
                        data.description,
                        data.disscount,
                        data.disscounted_price,
                        data.featured,
                        data.id,
                        data.image,
                        data.order_index,
                        data.price,
                        data.quantity,
                        0,
                        data.status,
                        data.title,
                        0,
                        0,
                        0
                    )

                    bind(newCartItem,data)

                }
            }

        }


    }

    fun bind(cartItem: CartItem, data: ProductDetailsResponse.Product.ProductX) {

        var isExpanded = false

        binding.quantityIndicatorTextView.text = cartItem.itemCount.toString()

        binding.addButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                if (Utils.isUserLoggedIn(this@ProductDetailsActivity)){
                    CartTransactionRepository.addToCart(productId, object :
                        CartTransactionListener {
                        override fun onTransactionSuccess() {
                            lifecycleScope.launch(Dispatchers.IO){
                                cartItem.itemCount++
                                withContext(Dispatchers.Main){
                                    binding.quantityIndicatorTextView.text = cartItem.itemCount.toString()
                                }
                                cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.disscounted_price.toInt())
                                cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.price.toInt())
                                insertCartItem(cartItem)

                                withContext(Dispatchers.Main){
                                    if (!isExpanded){
                                        binding.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                                        Animations.expand(binding.removeLl)
                                        isExpanded = true
                                    }
                                }
                            }
                        }

                        override fun onTransactionFailed() {

                        }

                    }, this@ProductDetailsActivity)
                } else {
                    withContext(Dispatchers.Main){
                        cartItem.itemCount++
                        binding?.quantityIndicatorTextView?.text = cartItem.itemCount.toString()
                        cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.disscounted_price.toInt())
                        cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.price.toInt())
                        withContext(Dispatchers.IO){
                            insertCartItem(cartItem)
                        }
                        if (!isExpanded){
                            binding.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                            Animations.expand(binding.removeLl)
                            isExpanded = true
                        }

                    }
                }
            }
        }

        binding?.removeButton?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                if (Utils.isUserLoggedIn(this@ProductDetailsActivity)){
                    CartTransactionRepository.removeFromCart(productId, object :
                        CartTransactionListener {
                        override fun onTransactionSuccess() {
                            lifecycleScope.launch(Dispatchers.IO){
                                cartItem.itemCount--
                                withContext(Dispatchers.Main){
                                    binding?.quantityIndicatorTextView?.text = cartItem.itemCount.toString()
                                }
                                cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.disscounted_price.toInt())
                                cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.price.toInt())
                                if (cartItem.itemCount == 0){
                                    deleteCartItem(cartItem)
                                    withContext(Dispatchers.Main){
                                        binding.addButton.setImageResource(R.drawable.circle_button_add_bg)
                                        Animations.collapse(binding.removeLl)
                                        isExpanded = false
                                    }
                                } else {
                                    updateCartItem(cartItem)
                                }
                            }




                        }

                        override fun onTransactionFailed() {

                        }

                    }, this@ProductDetailsActivity)
                } else {

                    withContext(Dispatchers.Main){
                        cartItem.itemCount--
                        binding?.quantityIndicatorTextView?.text = cartItem.itemCount.toString()
                        cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.disscounted_price.toInt())
                        cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,data.price.toInt())
                        if (cartItem.itemCount == 0){
                            withContext(Dispatchers.IO){
                                deleteCartItem(cartItem)

                            }
                            binding.addButton.setImageResource(R.drawable.circle_button_add_bg)
                            Animations.collapse(binding.removeLl)
                            isExpanded = false
                        } else {
                            withContext(Dispatchers.IO){
                                updateCartItem(cartItem)
                            }
                        }

                    }

                }
            }
        }

    }


    private fun toggleLayout(isExpanded: Boolean, layoutExpand: LinearLayout): Boolean {
        if (isExpanded) {
            Animations.expand(layoutExpand)
        } else {
            Animations.collapse(layoutExpand)
        }
        return isExpanded
    }

    private fun insertCartItem(cartItem: CartItem){
        lifecycleScope.launch(Dispatchers.IO){
            CartDB.getInstance(this@ProductDetailsActivity)
                .dao()
                .addItemToCart(cartItem)
        }
    }

    private fun updateCartItem(cartItem: CartItem){
        lifecycleScope.launch(Dispatchers.IO){
            CartDB.getInstance(this@ProductDetailsActivity)
                .dao()
                .updateSingleCartItem(cartItem)
        }
    }

    private fun deleteCartItem(cartItem: CartItem){
        lifecycleScope.launch(Dispatchers.IO){
            CartDB.getInstance(this@ProductDetailsActivity)
                .dao()
                .removeItemFromCart(cartItem)
        }
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        finish()
    }

}