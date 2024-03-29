package com.morning2morning.android.m2m.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.models.HomeBannerResponse
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.HomeRepository
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class HomeViewModel(val repo: HomeRepository) : ViewModel() {

    private val _homeBannerData: MutableLiveData<NetworkState<List<HomeBannerResponse.Result>>> = MutableLiveData()
    val homeBannerData: LiveData<NetworkState<List<HomeBannerResponse.Result>>>
        get() = _homeBannerData

    private val _categoryData: MutableLiveData<NetworkState<List<HomeCategoryResponse.Categories.Category>>> = MutableLiveData()
    val categoryData: LiveData<NetworkState<List<HomeCategoryResponse.Categories.Category>>>
        get() = _categoryData

    private val _categoryGridData: MutableLiveData<NetworkState<List<HomeCategoryResponse.Categories.Category>>> = MutableLiveData()
    val categoryGridData: LiveData<NetworkState<List<HomeCategoryResponse.Categories.Category>>>
        get() = _categoryGridData

    fun getHomeBannerList() = viewModelScope.launch {
        repo.getHomeBannerData().collect{
            _homeBannerData.value = it
        }
    }

    fun getCategoryList(catOffSet: Int) = viewModelScope.launch {
        repo.getCategoryList(catOffSet).collect{
            _categoryData.value = it
        }
    }

    fun getCategoryGridList() = viewModelScope.launch {
        repo.getCategoryGridList().collect{
            _categoryGridData.value = it
        }
    }



}