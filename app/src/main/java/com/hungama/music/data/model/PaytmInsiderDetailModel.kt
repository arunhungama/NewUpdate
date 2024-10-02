package com.hungama.music.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PaytmInsiderDetailModel(
    @SerializedName("data")
    var `data`: Data = Data()) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("itype")
        var itype: Int = 0,
        @SerializedName("body")
        var body: Body = Body(),
        @SerializedName("head")
        var head: Head = Head()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Head(
            @SerializedName("itype")
            val itype: Int = 0,
            @SerializedName("artist")
            var artist: List<Artist> = listOf(),
            @SerializedName("event")
            val event: Event = Event()):Parcelable{
            @Keep
            @Parcelize
            data class Event(
                @SerializedName("id")
                var id : String = "",
                @SerializedName("paytmInsiderId")
                var paytmInsiderId : String = "",
                @SerializedName("type")
                var type : Int = 0,
                @SerializedName("eventName")
                var eventName : String = "",
                @SerializedName("country")
                var country : String = "",
                @SerializedName("eventTag")
                var eventTag : String = "",
                @SerializedName("venue")
                var venue : String = "",
                @SerializedName("venueMapLink")
                var venueMapLink : String = "",
                @SerializedName("lowestTicketPrice")
                var lowestTicketPrice : String = "",
                @SerializedName("webviewUrlIos")
                var webviewUrlIos : String = "",
                @SerializedName("webviewUrlAndroid")
                var webviewUrlAndroid : String = "",
                @SerializedName("age")
                var age : String = "",
                @SerializedName("language")
                var language : String = "",
                @SerializedName("duration")
                var duration : String = "",
                @SerializedName("aboutEventTitle")
                var aboutEventTitle : String = "",
                @SerializedName("aboutEventDesc")
                var aboutEventDesc : String = "",
                @SerializedName("date")
                var date : String = "",
                @SerializedName("share")
                var share : String = "",
                @SerializedName("eventDate")
                var eventDate : String = "",
                @SerializedName("endDate")
                var endDate : String = "",
                @SerializedName("ticketPrice")
                var ticketPrice : List<TicketPrice> = listOf(),
                @SerializedName("image")
                var image: List<String> = listOf(),
                @SerializedName("keywords")
                var keywords: List<String> = listOf()
            ):Parcelable{
                @Keep
                @Parcelize
                    data class TicketPrice(
                        @SerializedName("type")
                        var type: String = "",
                        @SerializedName("value")
                        var value: String = ""):Parcelable{

                    }
            }

            @Keep
            @Parcelize
            data class Artist(
                @SerializedName("id")
                var id:String = "",
                @SerializedName("name")
                var name:String = "",
                @SerializedName("sname")
                var sname:String = "",
                @SerializedName("typeid")
                var typeid:Int = 0,
                @SerializedName("type")
                var type:String = "",
                @SerializedName("image")
                var image:String = "",
                @SerializedName("isFollowed")
                var isFollowed:Boolean = false,
            ):Parcelable
        }
        @Keep
        @Parcelize
        data class Body(
            @SerializedName("albums")
            var album: Album = Album(),
            @SerializedName("songs")
            var songs: Songs = Songs()):Parcelable{
            @Keep
            @Parcelize
            data class Album(
                @SerializedName("item")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()):Parcelable

            @Keep
            @Parcelize
            data class Songs(
                @SerializedName("item")
                var items: ArrayList<BodyRowsItemsItem?>? = arrayListOf()
            ) : Parcelable
        }
    }
}