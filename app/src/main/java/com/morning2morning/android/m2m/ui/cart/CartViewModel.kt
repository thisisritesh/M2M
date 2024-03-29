package com.morning2morning.android.m2m.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.CartRepository
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class CartViewModel(val repo: CartRepository) : ViewModel() {

    private val _cartData: MutableLiveData<NetworkState<List<CartItem>>> = MutableLiveData()
    val cartData: LiveData<NetworkState<List<CartItem>>>
        get() = _cartData




    fun getCartDataList() = viewModelScope.launch {
        repo.getCartData().collect{
            _cartData.value = it
        }
    }



}