package com.morning2morning.android.m2m.ui.searchresult

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morning2morning.android.m2m.data.models.SearchResponse
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.SearchResultRepository
import kotlinx.coroutines.launch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class SearchResultViewModel(val repo: SearchResultRepository) : ViewModel() {

    private val _searchResultData: MutableLiveData<NetworkState<SearchResponse.Product>> = MutableLiveData()
    val searchResultData: LiveData<NetworkState<SearchResponse.Product>>
        get() = _searchResultData

    fun getSearchResultList(offset: Int) = viewModelScope.launch {
        repo.getSearchResultData(offset).collect{
            _searchResultData.value = it
        }
    }


}