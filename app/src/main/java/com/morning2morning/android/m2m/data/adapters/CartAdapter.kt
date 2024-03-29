package com.morning2morning.android.m2m.data.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.databinding.CartItemLayoutBinding
import com.morning2morning.android.m2m.utils.ui.Utils
import com.morning2morning.android.m2m.utils.ui.animations.Animations
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class CartAdapter(private var list: List<CartItem>, private val activity: Activity, val adapterCallbacks: AdapterCallbacks) : RecyclerView.Adapter<CartAdapter.CartItemViewHolder>() {

    interface AdapterCallbacks{
        fun onItemClicked(productId: String)
    }

    @DelicateCoroutinesApi
    inner class CartItemViewHolder(binding: CartItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleTextView = binding.titleTextViewProduct
        val imageView = binding.imageViewSearchResult
        val quantityIndicator = binding.quantityIndicatorTextView
        val quantityTextView = binding.productQuantityTextView
        val priceTextView = binding.productPriceTextView
        val removeButton = binding.removeButton
        val discountedPriceTextView = binding.discountedPriceTextView
        val addToCartCustomView = binding.addToCartCustomView
        val addButton = binding.addButton
        val removeLl = binding.removeLl

        init {

            binding.root.setOnClickListener {
                adapterCallbacks.onItemClicked(list[adapterPosition].id)
            }

            removeButton.setOnClickListener {
                removeItemFromCartDatabase(list[adapterPosition])
            }
        }

        private fun removeItemFromCartDatabase(product: CartItem){
            GlobalScope.launch(Dispatchers.IO) {
                CartDB.getInstance(activity)
                    .dao()
                    .removeItemFromCart(product)
            }
        }

        fun bind(cartItem: CartItem, holder: CartAdapter.CartItemViewHolder,position: Int){

            holder.quantityIndicator.text = cartItem.itemCount.toString()

            holder.addButton.setOnClickListener {
                if (cartItem.itemCount == 0){
                    cartItem.itemCount++
                    holder.insertCartItem(cartItem)
                    holder.quantityIndicator.text = cartItem.itemCount.toString()
                    cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].disscounted_price.toInt())
                    cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].price.toInt())
                    holder.addButton.setImageResource(R.drawable.circle_button_add_white_bg)
                    Animations.expand(holder.removeLl)
                } else {
                    cartItem.itemCount++
                    holder.quantityIndicator.text = cartItem.itemCount.toString()
                    cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].disscounted_price.toInt())
                    cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].price.toInt())
                    holder.updateCartItem(cartItem)
                }
            }

            holder.removeButton.setOnClickListener {
                if (cartItem.itemCount == 1){
                    cartItem.itemCount--
                    holder.deleteItemFromCart(cartItem)
                    holder.quantityIndicator.text = "0"
                    cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].disscounted_price.toInt())
                    cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].price.toInt())
                    Animations.collapse(holder.removeLl)
                } else {
                    cartItem.itemCount--
                    holder.quantityIndicator.text = cartItem.itemCount.toString()
                    cartItem.totalDiscountedPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].disscounted_price.toInt())
                    cartItem.totalPrice = Utils.calculateTotalPrice(cartItem.itemCount,list[position].price.toInt())
                    holder.updateCartItem(cartItem)
                }
            }

        }


        @DelicateCoroutinesApi
        fun updateCartItem(cartItem: CartItem){
            GlobalScope.launch(Dispatchers.IO){
                CartDB.getInstance(activity)
                    .dao()
                    .updateSingleCartItem(cartItem)
            }
        }

        @DelicateCoroutinesApi
        fun insertCartItem(cartItem: CartItem){
            GlobalScope.launch(Dispatchers.IO){
                CartDB.getInstance(activity)
                    .dao()
                    .addItemToCart(cartItem)
            }
        }

        @DelicateCoroutinesApi
        fun deleteItemFromCart(cartItem: CartItem){
            GlobalScope.launch(Dispatchers.IO){
                CartDB.getInstance(activity)
                    .dao()
                    .removeItemFromCart(cartItem)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        return CartItemViewHolder(CartItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {

        holder.bind(list[position],holder,position)

        holder.titleTextView.text = list[position].title


        Glide.with(holder.imageView.context)
            .load(list[position].image)
            .placeholder(R.mipmap.m2m_placeholder)
            .into(holder.imageView)
        holder.quantityIndicator.text = list[position].itemCount.toString()
        holder.quantityTextView.text = list[position].quantity

        holder.discountedPriceTextView.text = "\u20B9" +" " + list[position].disscounted_price
        holder.priceTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        holder.priceTextView.text = "\u20B9" +" " + list[position].price

//        holder.addButton.setImageResource(R.drawable.)

    }


    fun setData(list: List<CartItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

}