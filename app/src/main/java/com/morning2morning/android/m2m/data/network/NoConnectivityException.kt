package com.morning2morning.android.m2m.data.network

import java.io.IOException

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class NoConnectivityException : IOException() {

    override fun getLocalizedMessage(): String? {
        return NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE
    }

}