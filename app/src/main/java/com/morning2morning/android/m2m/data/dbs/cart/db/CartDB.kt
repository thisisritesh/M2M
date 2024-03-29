package com.morning2morning.android.m2m.data.dbs.cart.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.morning2morning.android.m2m.data.dbs.cart.dao.CommonDao
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */

@Database(entities = [CartItem::class], version = 4, exportSchema = false)
abstract class CartDB : RoomDatabase() {

    abstract fun dao() : CommonDao

    companion object {
        private var INSTANCE: CartDB? = null

        @Synchronized
        public fun getInstance(context: Context) : CartDB {
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                    CartDB::class.java,
                    "CartDB")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }

    }

}