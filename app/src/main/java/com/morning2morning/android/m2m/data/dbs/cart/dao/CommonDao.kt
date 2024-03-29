package com.morning2morning.android.m2m.data.dbs.cart.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Dao
interface CommonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addItemToCart(product: CartItem)

    @Delete
    fun removeItemFromCart(product: CartItem)

    @Query("SELECT * FROM product_table")
    fun getAllCartItemsLiveData() : LiveData<List<CartItem>>

    @Query("SELECT * FROM product_table")
    fun getAllCartItems() : List<CartItem>

    @Query("SELECT * FROM product_table WHERE id LIKE :productId")
    fun getCartItemById(productId: String) : CartItem

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSingleCartItem(product: CartItem)


}