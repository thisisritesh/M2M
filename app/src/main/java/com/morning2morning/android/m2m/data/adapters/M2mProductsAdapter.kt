package com.morning2morning.android.m2m.data.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.callbacks.CartTransactionListener
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.data.models.M2mProductsResponse
import com.morning2morning.android.m2m.data.repositories.CartTransactionRepository
import com.morning2morning.android.m2m.databinding.CategoryDetailsItemLayoutBinding
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.ui.Utils
import com.morning2morning.android.m2m.utils.ui.animations.Animations
import kotlinx.coroutines.*

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class M2mProductsAdapter(val productList: List<M2mProductsResponse.Product.ProductX>, val context: Context, val adapterCallbacks: AdapterCallbacks, val recyclerView: RecyclerView) : RecyclerView.Adapter<M2mProductsAdapter.CategoryDetailsViewHolder>() {

    companion object{
        private const val TAG = "M2mProductsAdapter"
    }

    interface AdapterCallbacks{
        fun onItemClicked(productId: String)
        fun onRequestLoadMore()
    }

    private var lastVisibleItem = 0
    private var totalItemCount = 0
    private var isLoading = false

    init {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = recyclerView.adapter!!.itemCount
                try {
                    lastVisibleItem =
                        recyclerView.getChildAdapterPosition(recyclerView.getChildAt(recyclerView.childCount - 1))
                } catch (e: Exception) {
                    Log.e(TAG, "onScrolled: exception", e)
                    lastVisibleItem = 0
                }
                if (!isLoading && totalItemCount == lastVisibleItem + 1) {
                    Log.d(TAG, "onRequestLoadMore: ")
                    adapterCallbacks.onRequestLoadMore()
                    isLoading = true
                }
            }
        })

    }

    @DelicateCoroutinesApi
    inner class CategoryDetailsViewHolder(binding: CategoryDetailsItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleTextView = binding.titleTextViewProduct
        val imageView = binding.imageView
        val quantityIndicator = binding.quantityIndicatorTextView
        val quantityTextView = binding.productQuantityTextView
        val priceTextView = binding.productPriceTextView
        val removeButton = binding.removeButton
        val discountedPriceTextView = binding.discountedPriceTextView
        val addToCartCustomView = binding.addToCartCustomView
        val addButton = binding.addButton
        val removeLl = binding.removeLl
        val offerTextView = binding.offersTextView
        val outOfStockMessage = binding.outOfStockMessage
        val offerBgPart1 = binding.offersBgPart1
        val offersBgPart2 = binding.offersBgPart2

        init {
            binding.root.setOnClickListener {
                adapterCallbacks.onItemClicked(productList[adapterPosition].id)
            }
        }

        fun bind(cartItem: CartItem, holder: CategoryDetailsViewHolder) {

            var isExpanded = false

            holder.quantityIndicator.text = cartItem.itemCount.toString()

            holder.addButton.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO){
                    if (Pref(context).getPrefBoolean(Constants.LOGIN_STATUS)){
                        CartTransactionRepository.addToCart(productList[adapterPosition].id, object :
                            CartTransactionListener {
                            override fun onTransactionSuccess() {
                                cartItem.itemCount++
                                holder.quantityIndicator.text = cartItem.itemCount.toString()
                                cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].disscounted_price.toInt())
                                cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].price.toInt())
                                holder.insertCartItem(cartItem)
                                if (!isExpanded){
                                    holder.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                                    Animations.expand(holder.removeLl)
                                    isExpanded = true
                                }
                            }

                            override fun onTransactionFailed() {

                            }

                        }, context)
                    } else {
                        withContext(Dispatchers.Main){
                            cartItem.itemCount++
                            holder.quantityIndicator.text = cartItem.itemCount.toString()
                            cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].disscounted_price.toInt())
                            cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].price.toInt())
                            holder.insertCartItem(cartItem)
                            if (!isExpanded){
                                holder.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                                Animations.expand(holder.removeLl)
                                isExpanded = true
                            }
                        }
                    }
                }
            }

            holder.removeButton.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO){
                    if (Pref(context).getPrefBoolean(Constants.LOGIN_STATUS)){
                        CartTransactionRepository.removeFromCart(productList[adapterPosition].id, object :
                            CartTransactionListener {
                            override fun onTransactionSuccess() {
                                cartItem.itemCount--
                                holder.quantityIndicator.text = cartItem.itemCount.toString()
                                cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].disscounted_price.toInt())
                                cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].price.toInt())
                                if (cartItem.itemCount == 0){
                                    holder.deleteItemFromCart(cartItem)
                                    holder.addButton.setImageResource(R.drawable.circle_button_add_bg)
                                    Animations.collapse(holder.removeLl)
                                    isExpanded = false
                                } else {
                                    holder.updateCartItem(cartItem)
                                }
                            }

                            override fun onTransactionFailed() {

                            }

                        }, context)
                    } else {
                        withContext(Dispatchers.Main){
                            cartItem.itemCount--
                            holder.quantityIndicator.text = cartItem.itemCount.toString()
                            cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].disscounted_price.toInt())
                            cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,productList[adapterPosition].price.toInt())
                            if (cartItem.itemCount == 0){
                                holder.deleteItemFromCart(cartItem)
                                holder.addButton.setImageResource(R.drawable.circle_button_add_bg)
                                Animations.collapse(holder.removeLl)
                                isExpanded = false
                            } else {
                                holder.updateCartItem(cartItem)
                            }
                        }
                    }
                }
            }

        }

        @DelicateCoroutinesApi
        fun updateCartItem(cartItem: CartItem){
            GlobalScope.launch(Dispatchers.IO){
                CartDB.getInstance(context)
                    .dao()
                    .updateSingleCartItem(cartItem)
            }
        }

        @DelicateCoroutinesApi
        fun insertCartItem(cartItem: CartItem){
            GlobalScope.launch(Dispatchers.IO){
                CartDB.getInstance(context)
                    .dao()
                    .addItemToCart(cartItem)
            }
        }

        @DelicateCoroutinesApi
        fun deleteItemFromCart(cartItem: CartItem){
            GlobalScope.launch(Dispatchers.IO){
                CartDB.getInstance(context)
                    .dao()
                    .removeItemFromCart(cartItem)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailsViewHolder {
        return CategoryDetailsViewHolder(CategoryDetailsItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CategoryDetailsViewHolder, position: Int) {
        holder.titleTextView.text = productList[position].title

        Glide.with(holder.imageView.context)
            .load(productList[position].image)
            .placeholder(R.mipmap.placeholder_portrait)
            .into(holder.imageView)

        holder.quantityTextView.text = productList[position].quantity

        holder.offerTextView.text = productList[position].disscount + "% off"

//        if (productList[position].status == "0")

//        if (productList[position].disscount == "0"){
//            holder.offerBgPart1.visibility = View.GONE
//            holder.offersBgPart2.visibility = View.GONE
//        }

        holder.discountedPriceTextView.text = "\u20B9" +" " + productList[position].disscounted_price
        holder.priceTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        holder.priceTextView.text = "\u20B9" +" " + productList[position].price

//        holder.savingsTextView.text = "Save " +  "\u20B9" + " " + (productList[position].price.toInt() - productList[position].disscounted_price.toInt())

        // Begin
        val productId = productList[position].id

        GlobalScope.launch(Dispatchers.IO){
            val list = CartDB.getInstance(context)
                .dao()
                .getAllCartItems()

            var foundItem: CartItem? = null

            for (item in list){
                if (item.id == productId){
                    foundItem = item
                }
            }

            if (foundItem?.id == productId){
                val cartItem: CartItem = CartDB.getInstance(context).dao().getCartItemById(productId)
                withContext(Dispatchers.Main){

                    // Show expanded view
                    holder.quantityIndicator.text = cartItem.itemCount.toString()
                    holder.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                    toggleLayout(true, holder.removeLl)

//                    holder.totalPriceTv.text = "\u20B9" +" " + cartItem.totalPrice
                    holder.bind(cartItem,holder)
                }
            } else {
                withContext(Dispatchers.Main){

                    toggleLayout(false,holder.removeLl)

                    val newCartItem = CartItem(
                        productList[position].banner,
                        productList[position].best,
                        productList[position].cat_id,
                        productList[position].created,
                        productList[position].description,
                        productList[position].disscount,
                        productList[position].disscounted_price,
                        productList[position].featured,
                        productList[position].id,
                        productList[position].image,
                        productList[position].order_index,
                        productList[position].price,
                        productList[position].quantity,
                        0,
                        productList[position].status,
                        productList[position].title,
                        0,
                        0,
                        0
                    )

                    holder.bind(newCartItem,holder)

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

    override fun getItemCount(): Int = productList.size

}