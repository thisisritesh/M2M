package com.morning2morning.android.m2m.ui.m2mproducts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.models.M2mProductsResponse
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.M2mProductsRepository
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class M2mProductsViewModel(val repo: M2mProductsRepository) : ViewModel() {

    private val _m2mProducts: MutableLiveData<NetworkState<List<M2mProductsResponse.Product.ProductX>>> = MutableLiveData()
    val m2mProducts: LiveData<NetworkState<List<M2mProductsResponse.Product.ProductX>>>
        get() = _m2mProducts


    fun getM2mProductsList(offset: Int) = viewModelScope.launch {
        repo.getM2mProductsData(offset).collect{
            _m2mProducts.value = it
        }
    }


}