package com.morning2morning.android.m2m.ui.searchresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morning2morning.android.m2m.data.repositories.SearchResultRepository

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class SearchResultViewModelFactory(private val repo: SearchResultRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            return SearchResultViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}