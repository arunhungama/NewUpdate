package com.hungama.music.ui.main.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hungama.music.data.model.PaytmInsiderDetailModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import com.hungama.music.utils.hide
import com.hungama.music.utils.show
import com.hungama.myplay.activity.R
import com.hungama.myplay.activity.databinding.RowItemPlansBinding

/**
 * Created by Chetan(chetan.patel@saeculumsolutions.com)
 * Copyright (c) by saeculumsolutions(www.saeculumsolutions.com)
 * Purpose: set user notification data
 */
class PlanListAdapter(
    val context: Context,
    var ticketPriceList: List<PaytmInsiderDetailModel.Data.Head.Event.TicketPrice?>,
    val maxScreenWidth : Double,
    val onitemclick: OnItemClick?) :
    RecyclerView.Adapter<PlanListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowItemPlansBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowItemPlansBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){

            val marginStart = context.resources.getDimensionPixelSize(R.dimen.dimen_2).toDouble()
            val marginEnd = context.resources.getDimensionPixelSize(R.dimen.dimen_2).toDouble()
            val marginTop = context.resources.getDimensionPixelSize(R.dimen.dimen_15).toDouble()
            val marginBottom = context.resources.getDimensionPixelSize(R.dimen.dimen_10).toDouble()

            val noOfColums :Double= if (ticketPriceList.size>3) 3.25 else 3.0

            val parentStartSpacing = context.resources.getDimensionPixelSize(R.dimen.dimen_2_1).toDouble() * (noOfColums-1.5)
            val itemWidth = (maxScreenWidth / noOfColums)-parentStartSpacing
            val lineOne = context.resources.getDimensionPixelSize(R.dimen.font_9).toDouble() + context.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            val lineTwo = context.resources.getDimensionPixelSize(R.dimen.font_14).toDouble() + context.resources.getDimensionPixelSize(R.dimen.dimen_13).toDouble()
            val lineThree = context.resources.getDimensionPixelSize(R.dimen.font_12).toDouble() + context.resources.getDimensionPixelSize(R.dimen.dimen_4).toDouble()
            val textSize = lineOne + lineTwo + lineThree
            val imageHeightByAspectRatio = itemWidth * 9 / 16
            val itemHeight = imageHeightByAspectRatio + textSize + marginTop + marginBottom + marginStart + marginEnd
            CommonUtils.setLog("algafs", " $itemWidth $noOfColums")
//            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.transparentColor));

            binding.cnMain.layoutParams.width = itemWidth.toInt()
//            binding.cnMain.layoutParams.height = itemHeight.toInt()
            with(ticketPriceList[position]){
                val priceStr = this?.value?.split(",")
                val price = priceStr?.get(0)
                if (!TextUtils.isEmpty(price))
                {
                    binding.tvPrice.text = price
                    binding.tvTitle.show()
                    binding.tvPrice.show()
                }
                else
                {
                    binding.tvPrice.hide()
                    binding.tvTitle.hide()
                }

                if (!priceStr.isNullOrEmpty() && priceStr.size>1)
                {
                    val planTime = priceStr?.get(1)
                    if (!TextUtils.isEmpty(planTime))
                    {
                    binding.tvPlanTime.text = planTime
                    binding.tvPlanTime.show()
                }
                else
                    binding.tvPlanTime.hide()
                }
                else
                    binding.tvPlanTime.hide()

                if (position == 0)
                {
                    val param = binding.cardView.layoutParams as ViewGroup.MarginLayoutParams
                    param.setMargins(context.resources.getDimensionPixelSize(R.dimen.dimen_10_), 0, context.resources.getDimensionPixelSize(R.dimen.dimen_2), 0)
                    binding.cardView.layoutParams = param
                }
                else if(position == (ticketPriceList.size-1)){
                    val param = binding.cardView.layoutParams as ViewGroup.MarginLayoutParams
                    param.setMargins(context.resources.getDimensionPixelSize(R.dimen.dimen_2), 0, context.resources.getDimensionPixelSize(R.dimen.dimen_10_) , 0)
                    binding.cardView.layoutParams = param
                }
                else{
                    val param = binding.cardView.layoutParams as ViewGroup.MarginLayoutParams
                    param.setMargins(context.resources.getDimensionPixelSize(R.dimen.dimen_3) , 0, context.resources.getDimensionPixelSize(R.dimen.dimen_3),0)
                    binding.cardView.layoutParams = param
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return ticketPriceList.size
    }

    interface OnItemClick {
        fun onUserClick(data: PaytmInsiderDetailModel.Data.Head.Artist)
    }
}