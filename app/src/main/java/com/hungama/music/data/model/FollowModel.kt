package com.hungama.music.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
data class FollowModel(

    @field:SerializedName("data")
    val data: FollowData? = null,
) : Parcelable

@Parcelize
data class FollowData(
    @field:SerializedName("head")
    val head: FollowHead? = null,

    @field:SerializedName("body")
    val body: FollowBody? = null
) : Parcelable

@Parcelize
data class FollowHead(
    @SerializedName("playcount")
    var playcount : Int? = null
) : Parcelable

@Parcelize
data class FollowBody(

    @field:SerializedName("rows")
    var rows: ArrayList<FollowRowsItem?>? = null
) : Parcelable


@Parcelize
data class FollowRowsItem(
    @field:SerializedName("data")
    val data: FData? = null
) : Parcelable



@Keep
@Parcelize
data class FData(
    @SerializedName("id") var id: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("type") var type: Int? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("playble_image") var playbleImage: String? = null,
    @SerializedName("genre") var genre: ArrayList<String> = arrayListOf(),
    @SerializedName("subtitle") var subtitle: String? = null,
    @SerializedName("duration") var duration: Int? = null,
    @SerializedName("misc") var misc: Misc? = Misc()

) : Parcelable


