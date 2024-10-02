package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.OfflineEventRespModel
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.hide
import com.hungama.music.utils.show
import com.hungama.myplay.activity.R
import java.text.SimpleDateFormat
import java.util.Date

class PurchasesEventsAdapter(var context:Context, val onMovieItemClick: OnMovieItemClick?):RecyclerView.Adapter<PurchasesEventsAdapter.ViewHolder>() {

    var eventList = ArrayList<OfflineEventRespModel.Data.OfflineEvent>()
    internal fun setEventList(eventData: ArrayList<OfflineEventRespModel.Data.OfflineEvent>) {
        eventList = eventData
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var eventTitle = itemView.findViewById(R.id.tvEventTitle) as TextView
        var eventTime = itemView.findViewById(R.id.tvEventTime) as TextView
        var tvDay = itemView.findViewById(R.id.tvDay) as TextView
        var tvMonth = itemView.findViewById(R.id.tvMonth) as TextView
        var eventAmountPaid = itemView.findViewById(R.id.tvAmountPaid) as TextView
        var eventImage = itemView.findViewById(R.id.ivEvent) as ImageView
        var ticketStatus = itemView.findViewById(R.id.ticketStatus) as TextView
        var tvViewTicket = itemView.findViewById(R.id.tvViewTicket) as TextView
        var cvBgView = itemView.findViewById(R.id.cvBgView) as ConstraintLayout
        var cvBgViewForeground = itemView.findViewById(R.id.cvBgViewForeground) as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //  var view = LayoutInflater.from(context).inflate(R.layout.ticket_view,parent,false)

        var view = LayoutInflater.from(context).inflate(R.layout.row_offline_events, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.eventTitle.text = eventList.get(position).eventName

        if (eventList.get(position).ticketDetails.get(0).amount?.isNotBlank() == true) {
            if (eventList.get(position)?.country.equals("IN")) {
                holder.eventAmountPaid.text =
                    "₹ " + eventList.get(position).ticketDetails.get(0).amount
            } else holder.eventAmountPaid.text =
                "$ " + eventList.get(position).ticketDetails.get(0).amount
        }

        holder.ticketStatus.text = eventList.get(position).ticketDetails.get(0).ticketDetails.get(0).ticketStatus

        if (!TextUtils.isEmpty(eventList.get(position)?.eventDate)) {
            holder.eventTime.text = eventList.get(position)?.eventDate
        }
        if (!TextUtils.isEmpty(eventList.get(position)?.date)) {
            holder.tvDay.text = com.hungama.music.utils.DateUtils.convertDate(
                com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
                com.hungama.music.utils.DateUtils.DATE_FORMAT_DD, eventList.get(position)?.date
            )
            holder.tvMonth.text = com.hungama.music.utils.DateUtils.convertDate(
                com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T,
                com.hungama.music.utils.DateUtils.DATE_FORMAT_MMM,
                eventList.get(position)?.date
            )
        }
        if (!TextUtils.isEmpty(eventList.get(position)?.date)) {

            if (!isWithinRange(
                    eventList.get(position)?.date.toString(),
                    eventList.get(position)?.endDate.toString()
                )
            ) {
                holder.cvBgViewForeground.show()
            }else holder.cvBgViewForeground.hide()
        }


        eventList.get(position).image?.let {
            ImageLoader.loadImage(
                context,
                holder.eventImage,
                it,
                R.drawable.bg_gradient_placeholder
            )
        }

        holder.tvViewTicket.setOnClickListener {
            if (onMovieItemClick != null) {
                eventList.get(position).ticketDetails.get(0).webview_url?.let { it1 ->
                    onMovieItemClick.onItemClick(
                        it1,eventList.get(position).ticketDetails.get(0).eventId.toString(),
                        eventList.get(position).ticketDetails.get(0).eventName.toString()
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    interface OnMovieItemClick {
        fun onItemClick(data: String,event_id: String,event_name: String)
    }
}
    fun isWithinRange(startDate: String,endDate: String): Boolean {

        val dateFormat2 = SimpleDateFormat("yyyy-MM-dd")

        val date1: Date = dateFormat2.parse(startDate)
        val date2: Date = dateFormat2.parse(endDate)

        return !(Date().before(date1) || Date().after(date2))
    }
