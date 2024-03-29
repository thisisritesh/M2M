package com.morning2morning.android.m2m.data.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class NetworkHelper {

    companion object{

        private var INSTANCE: Retrofit? = null

        @Synchronized
        fun getInstance(context: Context) : Retrofit? {
            if (INSTANCE == null){
                INSTANCE = Retrofit.Builder()
                    .baseUrl(NetworkConstants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getHttpClient(context))
                    .build()
            }
            return INSTANCE
        }

        private fun getHttpClient(context: Context) : OkHttpClient {
            val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            return OkHttpClient.Builder()
                .addInterceptor(ConnectivityInterceptor(context))
                .readTimeout(15,TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .connectTimeout(30,TimeUnit.SECONDS)
                .build()
        }
    }


}