package com.hungama.music.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ErrorPropertiesModel(
    @field:SerializedName("error_code")
    val error_code: String? = "",

    @field:SerializedName("response_code")
    val response_code: String? = "",

    @field:SerializedName("response_time")
    val response_time: String? = "",

    @field:SerializedName("url")
    val url: String? = ""): Parcelable
