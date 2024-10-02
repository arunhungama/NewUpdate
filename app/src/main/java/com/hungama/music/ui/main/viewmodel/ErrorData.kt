package com.hungama.music.ui.main.viewmodel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ErrorData (
        @field:SerializedName("errorCode")
        val errorCode: String,

        @field:SerializedName("errorMessage")
        val errorMessage: String
): Parcelable