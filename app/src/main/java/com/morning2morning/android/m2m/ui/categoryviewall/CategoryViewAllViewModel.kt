package com.morning2morning.android.m2m.ui.categoryviewall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.models.CategoriesResponse
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.CategoryRepository
import kotlinx.coroutines.launch


/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class CategoryViewAllViewModel(val repo: CategoryRepository) : ViewModel() {

    private val _categories: MutableLiveData<NetworkState<List<CategoriesResponse.Result>>> = MutableLiveData()
    val categories: LiveData<NetworkState<List<CategoriesResponse.Result>>>
        get() = _categories


    fun getCategoryList() = viewModelScope.launch {
        repo.getCategoriesData().collect{
            _categories.value = it
        }
    }



}