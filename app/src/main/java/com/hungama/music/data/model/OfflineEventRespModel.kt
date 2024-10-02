package com.hungama.music.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class OfflineEventRespModel(
    @SerializedName("data")
    var `data`: Data = Data(),
    @SerializedName("success")
    var success: Boolean = false
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("offlineEvent")
        var offlineEvent: ArrayList<OfflineEvent> = ArrayList()
    ) : Parcelable {
        @Keep
        @Parcelize
        data class OfflineEvent(
            @SerializedName("_id")
            var Id: String? = "",
            @SerializedName("event_id")
            var eventId: String? = "",
            @SerializedName("event_name")
            var eventName: String? = "",
            @SerializedName("organiser_name")
            var organiserName: String? = "",
            @SerializedName("organiser_id")
            var organiserId: String? = "",
            @SerializedName("transaction_id")
            var transactionId: String? = "",
            @SerializedName("ticket_details")
            var ticketDetails: ArrayList<TicketDetails> = ArrayList(),
            @SerializedName("hungama_id")
            var hungamaId: String? = "",
            @SerializedName("__v")
            var _v: Int? = 0,
            @SerializedName("image")
            var image: String? = "",
            @SerializedName("detailImage")
            var detailImage: String? = "",
            @SerializedName("eventDate")
            var eventDate: String? = "",
            @SerializedName("date")
            var date: String? = "",
            @SerializedName("endDate")
            var endDate: String? = "",
            @SerializedName("country")
            var country: String? = ""
        ) : Parcelable {
            @Keep
            @Parcelize
            data class TicketDetails(
                @SerializedName("event_id")
                var eventId: String? = "",
                @SerializedName("event_name")
                var eventName: String? = "",
                @SerializedName("event_slug")
                var eventSlug: String? = "",
                @SerializedName("organiser_id")
                var organiserId: String? = "",
                @SerializedName("organiser_name")
                var organiserName: String? = "",
                @SerializedName("venue_name")
                var venueName: String? = "",
                @SerializedName("buyer_email")
                var buyerEmail: String? = "",
                @SerializedName("buyer_name")
                var buyerName: String? = "",
                @SerializedName("original_cost")
                var originalCost: String? = "",
                @SerializedName("transaction_id")
                var transactionId: String? = "",
                @SerializedName("transaction_datetime")
                var transactionDatetime: String? = "",
                @SerializedName("shortcode")
                var shortcode: String? = "",
                @SerializedName("amount")
                var amount: String? = "",
                @SerializedName("buyer_phone")
                var buyerPhone: String? = "",
                @SerializedName("hungama_id")
                var hungamaId: String? = "",
                @SerializedName("ticket_details")
                var ticketDetails: ArrayList<TicketDetails2> = ArrayList(),
                @SerializedName("buyer_mobile")
                var buyerMobile: String? = "",
                @SerializedName("webview_url")
                var webview_url: String? = ""
            ) : Parcelable {
                @Keep
                @Parcelize
                data class TicketDetails2(
                    @SerializedName("ticket_name")
                    var ticketName: String? = "",
                    @SerializedName("ticket_id")
                    var ticketId: String? = "",
                    @SerializedName("ticket_bought_id")
                    var ticketBoughtId: String? = "",
                    @SerializedName("ticket_bought")
                    var ticketBought: String? = "",
                    @SerializedName("is_ticket_redeemed")
                    var isTicketRedeemed: Boolean? = false,
                    @SerializedName("ticket_status")
                    var ticketStatus: String? = "",
                    @SerializedName("ticket_item_user_params")
                    var ticketItemUserParams: ArrayList<String> = ArrayList(),
                    @SerializedName("ticket_inventory_user_params")
                    var ticketInventoryUserParams: ArrayList<String> = ArrayList()
                ) : Parcelable
            }
        }
    }
}