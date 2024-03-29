package com.morning2morning.android.m2m.data.dbs.cart.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.morning2morning.android.m2m.data.dbs.cart.entity.UserDetails

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Dao
interface UserDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: UserDetails)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateItem(item: UserDetails)

    @Query("SELECT * FROM user_details_table")
    fun getAllRecentSearchesLiveData() : LiveData<List<UserDetails>>


}