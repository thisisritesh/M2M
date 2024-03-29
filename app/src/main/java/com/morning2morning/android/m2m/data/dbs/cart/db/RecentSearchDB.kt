package com.morning2morning.android.m2m.data.dbs.cart.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.morning2morning.android.m2m.data.dbs.cart.dao.RecentSearchDao
import com.morning2morning.android.m2m.data.dbs.cart.entity.RecentSearch

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Database(entities = [RecentSearch::class], version = 1, exportSchema = false)
abstract class RecentSearchDB : RoomDatabase() {

    abstract fun dao() : RecentSearchDao

    companion object {
        private var INSTANCE: RecentSearchDB? = null

        @Synchronized
        public fun getInstance(context: Context) : RecentSearchDB {
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                    RecentSearchDB::class.java,
                    "RecentSearchDB")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }

    }

}