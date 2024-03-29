package com.morning2morning.android.m2m.data.dbs.cart.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.morning2morning.android.m2m.data.dbs.cart.dao.UserDetailsDao
import com.morning2morning.android.m2m.data.dbs.cart.entity.UserDetails

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Database(entities = [UserDetails::class], version = 1, exportSchema = false)
abstract class UserDetailsDB : RoomDatabase() {

    abstract fun dao() : UserDetailsDao

    companion object {
        private var INSTANCE: UserDetailsDB? = null

        @Synchronized
        public fun getInstance(context: Context) : UserDetailsDB {
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                    UserDetailsDB::class.java,
                    "UserDetailsDB")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }

    }

}