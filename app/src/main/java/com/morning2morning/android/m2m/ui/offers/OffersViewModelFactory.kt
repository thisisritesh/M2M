package com.morning2morning.android.m2m.ui.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morning2morning.android.m2m.data.repositories.OffersRepository

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class OffersViewModelFactory(private val repo: OffersRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OffersViewModel::class.java)) {
            return OffersViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}