package com.morning2morning.android.m2m.data.models

import com.google.gson.annotations.SerializedName

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
data class LoginParams(
    @SerializedName("mobile") val mobile: String
    )
