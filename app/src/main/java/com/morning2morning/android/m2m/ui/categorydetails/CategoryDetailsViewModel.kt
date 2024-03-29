package com.morning2morning.android.m2m.ui.categorydetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.models.ProductsListByCatIdResponse
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.CategoryDetailsRepository
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

class CategoryDetailsViewModel(val repo: CategoryDetailsRepository) : ViewModel() {

    private val _categoryDetailsData: MutableLiveData<NetworkState<List<ProductsListByCatIdResponse.Product.ProductX>>> = MutableLiveData()
    val categoryDetailsData: LiveData<NetworkState<List<ProductsListByCatIdResponse.Product.ProductX>>>
        get() = _categoryDetailsData


    fun getCategoriesData(offset: Int) = viewModelScope.launch {
        repo.getCategoriesData(offset).collect{
            _categoryDetailsData.value = it
        }
    }

}