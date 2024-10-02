package com.hungama.music.ui.main.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.myplay.activity.R
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.customview.circleimageview.CircleImageView
import com.hungama.music.utils.hide

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class DetailTVCastShowAdapter(
    context: Context,
    val slist: List<PlaylistModel.Data.Body.Row.Info.Cast>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context

    private inner class IType1000ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val ivUserImage: CircleImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootParent)

        fun bind(position: Int) {
            if (!slist.isNullOrEmpty() && slist.size > position) {
                val list = slist.get(position)?.data

                if (list != null) {
                    tvTitle.text = list.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.GONE
                }


                if (list?.image != null) {
                 //   CommonUtils.setArtImageBg(true, list?.image!!, rootLayout)
                    val params: ViewGroup.LayoutParams = llMain.layoutParams

                    ImageLoader.loadImage(
                        ctx, ivUserImage, list?.image!!, R.color.colorPlaceholder
                    )

                }

                llMain.setOnClickListener {
                    if (onChildItemClick != null) {
                        onChildItemClick.onUserClick(position, llMain)
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return IType1000ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_tv_cast, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return slist?.size!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType1000ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return slist?.get(position)?.itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int, isMenuClick:LinearLayoutCompat)
    }
}