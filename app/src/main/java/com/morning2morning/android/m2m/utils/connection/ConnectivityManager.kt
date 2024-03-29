package com.morning2morning.android.m2m.utils.connection

import android.content.Context
import androidx.lifecycle.LifecycleOwner

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
object ConnectivityManager {

    fun init(context: Context, viewLifecycleOwner: LifecycleOwner, onConnectionChanged: OnConnectionChanged){
        val connectionLiveData = ConnectionLiveData(context.applicationContext)
        connectionLiveData.observe(viewLifecycleOwner
        ) { connection -> onConnectionChanged.onInternetConnectionChanged(connection?.isConnected!!) }
    }

}