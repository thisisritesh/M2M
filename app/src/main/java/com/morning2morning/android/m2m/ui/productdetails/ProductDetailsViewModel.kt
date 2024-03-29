package com.morning2morning.android.m2m.ui.productdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.models.ProductDetailsResponse
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.ProductDetailsRepository
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class ProductDetailsViewModel(val repo: ProductDetailsRepository) : ViewModel() {

    private val _productDetailsData: MutableLiveData<NetworkState<ProductDetailsResponse>> = MutableLiveData()
    val productDetailsData: LiveData<NetworkState<ProductDetailsResponse>>
        get() = _productDetailsData


    fun getProductDetailsData() = viewModelScope.launch {
        repo.getProductDetailsData().collect{
            _productDetailsData.value = it
        }
    }

}