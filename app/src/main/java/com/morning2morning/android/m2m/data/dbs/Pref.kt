package com.morning2morning.android.m2m.data.dbs

import android.content.Context
import android.content.SharedPreferences
import com.morning2morning.android.m2m.utils.Constants

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class Pref(val context: Context) {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.M2M_SHARED_PREF, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    public fun getPrefString(key: String) : String {
        return sharedPreferences.getString(key,"")!!
    }

    public fun putPref(key: String, value: String) {
        editor.putString(key,value)
        editor.commit()
    }

    public fun getPrefBoolean(key: String) : Boolean {
        return sharedPreferences.getBoolean(key,false)
    }

    public fun putPref(key: String, value: Boolean) {
        editor.putBoolean(key,value)
        editor.commit()
    }

    public fun getPrefInt(key: String) : Int {
        return sharedPreferences.getInt(key,0)
    }

    public fun putPref(key: String, value: Int) {
        editor.putInt(key,value)
        editor.commit()
    }

}
