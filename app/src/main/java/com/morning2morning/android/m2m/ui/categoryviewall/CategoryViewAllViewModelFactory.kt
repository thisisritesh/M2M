package com.morning2morning.android.m2m.ui.categoryviewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morning2morning.android.m2m.data.repositories.CategoryRepository

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class CategoryViewAllViewModelFactory(private val repo: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewAllViewModel::class.java)) {
            return CategoryViewAllViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}