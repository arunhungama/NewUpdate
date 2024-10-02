package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.myplay.activity.R
import com.hungama.myplay.activity.databinding.RowItype8Binding
import com.hungama.music.data.model.LiveEventDetailModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.hide
import com.hungama.music.utils.show
import com.hungama.myplay.activity.databinding.EventTopSongBinding
import kotlinx.coroutines.delay

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class PaytmEventTopSongAdapter(
    val context: Context,
    var arrayList: List<BodyRowsItemsItem?>,
    val onitemclick: OnItemClick?
) :
    RecyclerView.Adapter<PaytmEventTopSongAdapter.ItemViewHolder>() {

    private val ROW_TYPE_1 = 1
    private val ROW_TYPE_2 = 2


    fun addData(list: List<BodyRowsItemsItem?>) {
        arrayList = list
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: EventTopSongBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.event_top_song,
            parent,
            false
        )
        return ItemViewHolder(binding)

    }


    class ItemViewHolder(val binding: EventTopSongBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int {
        return arrayList?.size!!
        //return 5
    }

    override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
//        holder.bind(holidayList.get(pos))
        if (arrayList?.get(holder.adapterPosition)?.data?.title != null) {
            holder.binding.tvTitle.text = arrayList?.get(holder.adapterPosition)?.data?.title
            holder.binding.tvTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvTitle.visibility = View.GONE
        }

        if (arrayList?.get(holder.adapterPosition)?.data?.subTitle != null) {
            holder.binding.tvSubTitle.text =
                arrayList?.get(holder.adapterPosition)?.data?.subTitle
            holder.binding.tvSubTitle.visibility = View.VISIBLE
        } else {
            holder.binding.tvSubTitle.visibility = View.GONE
        }

        if (arrayList?.get(holder.adapterPosition)?.data?.image != null) {
            ImageLoader.loadImage(
                context,
                holder.binding.ivUserImage,
                arrayList?.get(holder.adapterPosition)?.data?.image!!,
                R.drawable.bg_gradient_placeholder
            )
        }
        arrayList?.get(holder.adapterPosition)?.data?.isCurrentPlaying = false
        if (BaseActivity.songDataList.size>BaseActivity.nowPlayingCurrentIndex())
        {
            val currentPlayingContentId = BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
            arrayList?.get(holder.adapterPosition)?.data?.isCurrentPlaying =
                currentPlayingContentId.toString().equals(arrayList?.get(holder.adapterPosition)?.data?.id)
        }



        if (arrayList?.get(holder.adapterPosition)?.data?.isCurrentPlaying == true){
            holder.binding.vNowPlaying.show()
            holder.binding.ivEqualizer.show()
            holder.binding.ivEqualizerAnim.show()
            holder.binding.ivEqualizerAnim.playAnimation()
            holder.binding.ivEqualizerAnim.progress = 0f
        }else{
            holder.binding.vNowPlaying.hide()
            holder.binding.ivEqualizer.hide()
            holder.binding.ivEqualizerAnim.hide()
            holder.binding.ivEqualizerAnim.cancelAnimation()
            holder.binding.ivEqualizerAnim.progress = 0f
        }

        holder.binding.llMain.setOnClickListener {
            if (onitemclick != null) {
                onitemclick.onUserClick(holder.adapterPosition)
            }
        }
    }//onBind

    override fun getItemViewType(position: Int): Int {
        return ROW_TYPE_1

    }

    interface OnItemClick {
        fun onUserClick(position: Int)
    }
}