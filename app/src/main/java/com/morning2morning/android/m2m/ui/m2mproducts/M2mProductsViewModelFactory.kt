package com.morning2morning.android.m2m.ui.m2mproducts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morning2morning.android.m2m.data.repositories.M2mProductsRepository

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class M2mProductsViewModelFactory(private val repo: M2mProductsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(M2mProductsViewModel::class.java)) {
            return M2mProductsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}