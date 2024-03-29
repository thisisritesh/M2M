package com.morning2morning.android.m2m.data.dbs.cart.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.morning2morning.android.m2m.data.dbs.cart.entity.RecentSearch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Dao
interface RecentSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecentSearch(recentSearch: RecentSearch)

    @Delete
    fun deleteRecentSearch(recentSearch: RecentSearch)

    @Query("SELECT * FROM recent_search_table")
    fun getAllRecentSearchesLiveData() : LiveData<List<RecentSearch>>

    @Query("SELECT * FROM recent_search_table")
    fun getAllRecentSearches() : List<RecentSearch>

}