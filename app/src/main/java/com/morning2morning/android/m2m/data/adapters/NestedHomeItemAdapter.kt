package com.morning2morning.android.m2m.data.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
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
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.data.repositories.CartTransactionRepository
import com.morning2morning.android.m2m.databinding.NestedRvItemLayoutBinding
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.ui.Utils
import com.morning2morning.android.m2m.utils.ui.animations.Animations
import com.morning2morning.android.m2m.utils.ui.animations.Animations.collapse
import com.morning2morning.android.m2m.utils.ui.animations.Animations.expand
import kotlinx.coroutines.*

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class NestedHomeItemAdapter(
    val productList: List<HomeCategoryResponse.Categories.Category.Products.Product>, val context: Context, val recyclerView: RecyclerView, val
    adapterCallbacks: AdapterCallbacks) :
    RecyclerView.Adapter<NestedHomeItemAdapter.NestedHomeItemViewHolder>() {
    
    companion object{
        private const val TAG = "NestedHomeItemAdapter"
    }



    interface AdapterCallbacks{
        fun onNestedRecyclerViewItemClicked(productId: String)
    }


    inner class NestedHomeItemViewHolder(binding: NestedRvItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.titleTextView
        val imageView = binding.productImageView
        val quantityTextView = binding.quantityTextView
        val discountedPriceTextView = binding.discountedPriceTextView
        val originalPriceTextView = binding.originalPriceTextView
        val savingsTextView = binding.savingsTextView
        val addToCartCustomView = binding.addToCartCustomView
        val addButton = binding.addButton
        val removeLl = binding.removeLl
        val removeBtn = binding.removeButton
        val quantityIndicatorTextView = binding.quantityIndicatorTextView

        init {
            binding.root.setOnClickListener {
                adapterCallbacks.onNestedRecyclerViewItemClicked(productList[adapterPosition].id)
            }
        }

        fun bind(cartItem: CartItem, holder: NestedHomeItemViewHolder) {

            var isExpanded = false

            holder.quantityIndicatorTextView.text = cartItem.itemCount.toString()

            holder.addButton.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO){
                    if (Pref(context).getPrefBoolean(Constants.LOGIN_STATUS)){
                        CartTransactionRepository.addToCart(productList[adapterPosition].id, object :
                            CartTransactionListener {
                            override fun onTransactionSuccess() {
                                cartItem.itemCount++
                                holder.quantityIndicatorTextView.text = cartItem.itemCount.toString()
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
                            holder.quantityIndicatorTextView.text = cartItem.itemCount.toString()
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

            holder.removeBtn.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO){
                    if (Pref(context).getPrefBoolean(Constants.LOGIN_STATUS)){
                        CartTransactionRepository.removeFromCart(productList[adapterPosition].id, object :
                            CartTransactionListener {
                            override fun onTransactionSuccess() {
                                cartItem.itemCount--
                                holder.quantityIndicatorTextView.text = cartItem.itemCount.toString()
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
                            holder.quantityIndicatorTextView.text = cartItem.itemCount.toString()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedHomeItemViewHolder {
        return NestedHomeItemViewHolder(NestedRvItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NestedHomeItemViewHolder, position: Int) {
        holder.title.text = productList[position].title

        Glide.with(holder.imageView.context)
            .load(productList[position].image)
            .placeholder(R.mipmap.placeholder_portrait)
            .into(holder.imageView)

        holder.quantityTextView.text = productList[position].quantity

        holder.discountedPriceTextView.text = "\u20B9" +" " + productList[position].disscounted_price
        holder.originalPriceTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        holder.originalPriceTextView.text = "\u20B9" +" " + productList[position].price

        holder.savingsTextView.text = "Save " +  "\u20B9" + " " + (productList[position].price.toInt() - productList[position].disscounted_price.toInt())

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
                    holder.quantityIndicatorTextView.text = cartItem.itemCount.toString()
                    holder.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                    toggleLayout(true, holder.removeLl)

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

        // End

    }

    private fun toggleLayout(isExpanded: Boolean, layoutExpand: LinearLayout): Boolean {
        if (isExpanded) {
            expand(layoutExpand)
        } else {
            collapse(layoutExpand)
        }
        return isExpanded
    }


    override fun getItemCount(): Int = productList.size

}