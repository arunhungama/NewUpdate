package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.myplay.activity.R
import com.hungama.music.data.model.BodyRowsItemsItem
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.customview.circleimageview.CircleImageView

class ArtistForYouAdapter(
    context: Context,
    list: ArrayList<BodyRowsItemsItem?>?,
    val onChildItemClick: OnChildItemClick?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ctx: Context = context
    var list: ArrayList<BodyRowsItemsItem?>? = list

    private inner class IType12ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        //var tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val ivUserImage: CircleImageView = itemView.findViewById(R.id.ivUserImage)
        val llMain: LinearLayoutCompat = itemView.findViewById(R.id.llMain)
        fun bind(position: Int) {
            val list = list!![position]!!.data

            if (list!!.title != null) {
                tvTitle.text = list.title
                tvTitle.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.GONE
            }

            /*if (list!!.subtitle != null) {
                tvSubTitle.text = list.subtitle
                tvSubTitle.visibility = View.VISIBLE
            } else {
                tvSubTitle.visibility = View.GONE
            }*/


            if (!TextUtils.isEmpty(list.image)) {
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    list.image!!,
                    R.drawable.bg_gradient_placeholder
                )
            }else{
                ImageLoader.loadImage(
                    ctx,
                    ivUserImage,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }
            llMain.setOnClickListener {
                if (onChildItemClick != null) {
                    onChildItemClick.onUserClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return IType12ViewHolder(
                LayoutInflater.from(ctx).inflate(R.layout.row_itype_4, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IType12ViewHolder).bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return list!![position]?.itype!!
    }

    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
    }
}