package com.morning2morning.android.m2m.ui.categorydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morning2morning.android.m2m.data.repositories.CategoryDetailsRepository


/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class CategoryDetailsViewModelFactory(private val repo: CategoryDetailsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryDetailsViewModel::class.java)) {
            return CategoryDetailsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}