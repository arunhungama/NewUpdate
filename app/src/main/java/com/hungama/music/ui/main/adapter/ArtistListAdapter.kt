package com.hungama.music.ui.main.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hungama.myplay.activity.R
import com.hungama.myplay.activity.databinding.RowItype16Binding
import com.hungama.music.data.model.ArtistModel
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.data.model.PaytmInsiderDetailModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.ImageLoader
import com.hungama.myplay.activity.databinding.RowItemArtistListBinding
import kotlinx.android.synthetic.main.fragment_artist_details.*
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.*

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class ArtistListAdapter(
    val context: Context,
    var artistList: List<PaytmInsiderDetailModel.Data.Head.Artist?>,
    val onitemclick: OnItemClick?) :
    RecyclerView.Adapter<ArtistListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowItemArtistListBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowItemArtistListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(artistList[position]){
                if (!TextUtils.isEmpty(this?.image)) {
                    this?.image?.let {
                        ImageLoader.loadImage(context, binding.ivArtistImage, it, R.drawable.bg_gradient_placeholder)
                    }
                  }
                binding.tvArtistName.text = this?.name
                binding.tvSubTitle.text = this?.type
                if (this?.isFollowed == true){
                    binding.tvFollow.text = context.getString(R.string.profile_str_5)
                    binding.ivFollow.setImageDrawable(context.faDrawable(R.string.icon_following, R.color.colorWhite))
                }
                else{
                    binding.tvFollow.text = context.getString(R.string.profile_str_2)
                    binding.ivFollow.setImageDrawable(context.faDrawable(R.string.icon_follow, R.color.colorWhite))
                }
                binding.llFollow.setOnClickListener{
                    if (this?.isFollowed == true){
                        this.isFollowed = false
                        binding.tvFollow.text = context.getString(R.string.profile_str_2)
                        binding.ivFollow.setImageDrawable(context.faDrawable(R.string.icon_follow, R.color.colorWhite))
                    }
                    else{
                        this?.isFollowed = true
                        binding.tvFollow.text = context.getString(R.string.profile_str_5)
                        binding.ivFollow.setImageDrawable(context.faDrawable(R.string.icon_following, R.color.colorWhite))
                    }
                    this?.let { it1 -> onitemclick?.onUserClick(it1) }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return artistList.size
    }

    interface OnItemClick {
        fun onUserClick(data: PaytmInsiderDetailModel.Data.Head.Artist)
    }
}