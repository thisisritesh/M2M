package com.morning2morning.android.m2m.ui.offers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.models.HomeBannerResponse
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.OffersRepository
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class OffersViewModel(val repo: OffersRepository) : ViewModel() {

    private val _homeBannerData: MutableLiveData<NetworkState<List<HomeBannerResponse.Result>>> = MutableLiveData()
    val homeBannerData: LiveData<NetworkState<List<HomeBannerResponse.Result>>>
        get() = _homeBannerData

    private val _categoryData: MutableLiveData<NetworkState<List<HomeCategoryResponse.Categories.Category>>> = MutableLiveData()
    val categoryData: LiveData<NetworkState<List<HomeCategoryResponse.Categories.Category>>>
        get() = _categoryData



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

}



