package com.hungama.music.ui.main.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.LayoutParams
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.BucketSwipedEvent
import com.hungama.music.eventanalytic.eventreporter.Hero_card_viewed
import com.hungama.music.eventanalytic.eventreporter.ProgressiveSurveyTappedEvent
import com.hungama.music.eventanalytic.util.callbacks.inapp.InAppCallback
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.fragment.DiscoverTabFragment
import com.hungama.music.ui.main.viewmodel.RecentlyPlayViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setClickAnimation
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.blurview.CustomBlurView
import com.hungama.music.utils.customview.scrollingpagerindicator.ScrollingPagerIndicator
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import com.moengage.inapp.MoEInAppHelper
import kotlinx.android.synthetic.main.row_bucket.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class BucketParentAdapter(
    private var parents: ArrayList<RowsItem?>,
    val context: Context,
    val onParentItemClick: OnParentItemClickListener?,
    val onMoreItemClick: OnMoreItemClick,
    var bottomTabID: Int,
    val headItemsItem: HeadItemsItem?,
    val varient: Int = Constant.ORIENTATION_VERTICAL,
/*    val isMore:Boolean = false,
    var moreId:String = "",*/
    val bannerItemClick: BannerItemClick? = null
) : RecyclerView.Adapter<BucketParentAdapter.ViewHolder>(), BaseActivity.PlayItemChangeListener {
    var isStoryUpdate = false
    var continueWatchIndex = -1
    var commonSpaceBetweenBuckets = 0
    var artImageUrl: String? = null
    var pageCount = 0
    var isTimerCancelled = false
    var pagerAdapter : Itype50PagerAdapter? = null
    var auto_scroll = false
    var runnable: java.lang.Runnable? = null

    companion object {
        var isKeywordWatchCall = 999
        var lastHolderItem: ViewHolder? = null
        var bucketChildAdapterRecently: BucketChildAdapter? = null
        var isVisible = true
        var type_of_Scroll=false
        var handler: Handler? = null
        var itype47AdapterLiveData: MutableLiveData<String> = MutableLiveData<String>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        isKeywordWatchCall = 999
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_bucket, parent, false)

        (context as MainActivity).addPlayItemChangeListener(this)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return parents.size
    }

    fun addData(list: MutableList<RowsItem?>) {

        parents.forEach {
            if (it?.itype == null && it?.items != null && it?.items?.isNotEmpty() == true) {
                it?.itype = it?.items?.get(0)?.itype
                if (it?.type == 57){
                    it?.itype = 57
                    val item = BodyRowsItemsItem()
                    item.data?.image = ""
                    item.itype = 57
                    it?.items?.add(0, item)
                }
            }
        }


        if (continueWatchIndex >= 0) {
            try {

                if (lastHolderItem?.rvChildItem?.adapter != null) {
                    setLog("continue-watching", "continue-watching 555")

                    bucketChildAdapterRecently?.refreshData(
                        parents.get(
                            continueWatchIndex
                        )?.items!!
                    )
//                    lastHolderItem?.rvChildItem?.invalidate()

                } else {
                    setLog("continue-watching", "lastHolderItem?.rvChildItem?.adapter is null")
                }

                setLog(
                    "continue-watching", "continue-watching 4 :${
                        parents.get(
                            continueWatchIndex
                        )?.items?.size
                    }"
                )
                notifyItemChanged(continueWatchIndex)
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
    }

    private var lastClickedTime: Long = 0

    fun isCalledMultipalTime(): Boolean {
        /*
          Prevents the Launch of the component multiple times
          on clicks encountered in quick succession.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Constant.MAX_CLICK_INTERVAL) {
            return false
        }
        lastClickedTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        setLog("continue-watching", "parent->keywords" + parents[position]?.keywords)
        setLog("continue-watching", "isKeywordWatchCall" + isKeywordWatchCall)
//        setLog("continue-watching","parent?.items?.size"+parents[position]?.items?.size)
        setLog("continue-watching", "position::" + position)
        holder.blurView.visibility = View.GONE

        if (position < parents.size) {
            val parent = parents[position]
            setLog("lhfkasggdf", parent?.itype.toString() +" " + parent?.heading.toString()  +" " + parent?.id.toString())
            commonSpaceBetweenBuckets = context.resources.getDimensionPixelSize(R.dimen.common_two_bucket_space_listing_page)
/*            if (isMore && moreId.isNotEmpty() && moreId == parent?.id && !parent?.heading.isNullOrEmpty() && parent?.more == 1 && parent?.items?.size!! > 0){
                onMoreItemClick.onMoreClick(parent, position)
            }*/
            if (parent?.keywords != null && parent.keywords?.contains("continue-watching")!! && isKeywordWatchCall == 999 && isCalledMultipalTime()) {


               val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {

                        pagerAdapter?.notifyDataSetChanged();
                    }
                }

                val lbm = LocalBroadcastManager.getInstance(context)
                lbm.registerReceiver(receiver as BroadcastReceiver, IntentFilter(Constant.HeroSection))


                setLog("continue-watching", "continue-watching 0 called")
                continueWatchIndex = holder.adapterPosition
                lastHolderItem = holder

                setLog(
                    "continue-watching",
                    "continue-watching continueWatchIndex:${continueWatchIndex}"
                )
                setUpContinueWhereLeftListViewModel()
                Utils.setMargins(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                )
            } else {
                setLog("continue-watching", "continue-watching 0 not called")
                Utils.setMarginsTop(
                    holder.llHeaderTitle,
                    context.resources.getDimensionPixelSize(R.dimen.dimen_0)
                )
            }

            if (parent?.heading != null && !TextUtils.isEmpty(parent.heading!!) && ((parent.items != null && parent.items?.size!! > 0) || (parent.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                if (!TextUtils.isEmpty(parent.identifier.toString()) && parent.identifier == 1) {
                    var displayName = ""
                    if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserFirstname())) {
                        displayName += SharedPrefHelper.getInstance().getUserFirstname()!! + " "
                    }

                    if (!TextUtils.isEmpty(SharedPrefHelper.getInstance().getUserLastname())) {
                        displayName += SharedPrefHelper.getInstance().getUserLastname()!!
                    }

                    if (TextUtils.isEmpty(displayName)) {
                        CommonUtils.getDayGreetings(context)
                        holder.tvTitle.text = CommonUtils.greeting.value
                    } else {
                        CommonUtils.getDayGreetings(context)
                        holder.tvTitle.text = CommonUtils.greeting.value+" "+displayName
                    }

                    holder.tvTitle.visibility = View.VISIBLE
                } else {
                    holder.tvTitle.text = parent.heading
                    holder.tvTitle.visibility = View.VISIBLE
                }
            }
            else {
                holder.tvTitle.visibility = View.GONE
            }

            if (parent?.more != null && parent.more == 1 && ((parent.items != null && parent.items?.size!! > 0) || (parent.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                if (varient == Constant.ORIENTATION_HORIZONTAL) {
                    holder.ivMore.visibility = View.VISIBLE
                    holder.ivMore.setOnClickListener {
                        if (onMoreItemClick != null) {
                            if (parent.heading != null && !TextUtils.isEmpty(parent.heading!!)) {
                                onMoreItemClick.onMoreClick(parent, position)
                            }

                        }
                    }
                    holder.llHeaderTitle.setOnClickListener {
                        if (onMoreItemClick != null) {
                            if (parent.heading != null && !TextUtils.isEmpty(parent.heading!!)) {
                                onMoreItemClick.onMoreClick(parent, position)
                            }

                        }
                    }
                } else {
                    holder.ivMore.visibility = View.GONE
                }
            } else {
                holder.ivMore.visibility = View.GONE
            }

            if (parent?.subhead != null && !TextUtils.isEmpty(parent.subhead) && ((parent.items != null && parent.items?.size!! > 0) || (parent.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                holder.tvSubTitle.text = parent.subhead
                holder.tvSubTitle.visibility = View.VISIBLE
            } else {
                holder.tvSubTitle.visibility = View.GONE
            }

            if (parent?.public != null) {
                holder.switchPublic.isChecked = parent.public == 1
                holder.switchPublic.visibility = View.VISIBLE
            }

            if (parent?.itype == 47) {
                holder.tvTitle.visibility = View.GONE
                holder.tvSubTitle.visibility = View.GONE
                holder.ivMore.visibility = View.GONE
                holder.switchPublic.visibility = View.GONE
            }

            if (((parent?.items != null && parent.items?.size!! > 0) || (parent?.orignalItems != null && parent.orignalItems?.size!! > 0))) {
                holder.rvChildItem.visibility = View.VISIBLE
                holder.dotedView.visibility = View.GONE

                var layoutManager: GridLayoutManager? = null
                setLog(
                    "onBindViewHolder",
                    "heading:${parent.heading} varient:${varient} numrow:${parent.numrow}"
                )
                if (varient == Constant.ORIENTATION_VERTICAL) {
                    layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                    setLog("onBindViewHolder", "heading:${parent.heading} ORIENTATION_VERTICAL")

                } else {
                    if (parent.numrow != null && parent.numrow!! > 0) {
                        layoutManager = GridLayoutManager(
                            context,
                            parent.numrow!!,
                            GridLayoutManager.HORIZONTAL,
                            false
                        )
                    } else {
                        layoutManager =
                            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                    }

                    setLog("onBindViewHolder", "heading:${parent.heading} numrow:${parent.numrow}")
                }

                if (parent.itype == null || parent.items?.isEmpty() == true){
                    holder.rvChildItem.visibility = View.GONE
                    holder.dotedView.visibility = View.GONE
                }
                when (parent.itype) {
                    1 -> {
                        var layoutManager: GridLayoutManager? = null
                        if (parent.numrow != null && parent.numrow!! > 0) {
                            layoutManager = GridLayoutManager(
                                context,
                                parent.numrow!!,
                                GridLayoutManager.HORIZONTAL,
                                false
                            )
                        } else {
                            layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
                        }
                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )

                    }
                    2, 3,4,5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 22, 25, 41, 42, 43, 44,45,46,48,1000, 9999,52, 56, 57 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )

                        if (parent.itype == 57){
                            holder.rvChildItem.viewTreeObserver.addOnPreDrawListener(object :
                                ViewTreeObserver.OnPreDrawListener {
                                override fun onPreDraw(): Boolean {
                                    holder.rvChildItem.viewTreeObserver.removeOnPreDrawListener(this)
                                    holder.blurView.visibility = View.VISIBLE
                                    val height: Int = holder.rvChildItem.measuredHeight
                                    val layoutParams = holder.blurView.layoutParams
                                    layoutParams.height = height + context.resources.getDimensionPixelSize(R.dimen.dimen_16).toInt()
                                    holder.blurView.layoutParams = layoutParams
                                    return true
                                }
                            })
                        }



                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    18 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_25)
                        )
                        Utils.setMarginsBottom(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_20)
                        )
                    }
                    19 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        Utils.setMargins(holder.llMain, 0)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    50 -> {
                        handler?.removeCallbacksAndMessages(null)
                        holder.rvChildItem.visibility = View.GONE
                        holder.llHeaderTitle.visibility = View.GONE
                        holder.dotedView.visibility = View.VISIBLE

                        val displayMetrics = DisplayMetrics()

                       (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
                        val heightOfPager = ((62 * displayMetrics.heightPixels)/100).toFloat()
                        holder.dotedView.layoutParams.height = heightOfPager.toInt()

                        val param = holder.pagerIndicator.layoutParams as ViewGroup.MarginLayoutParams
                        param.setMargins(0,0,0,context.resources.getDimensionPixelSize(R.dimen.dimen_10))
                        holder.pagerIndicator.layoutParams = param
                        holder.pagerIndicator.bringToFront()


                        if (pagerAdapter == null) {
                            pagerAdapter = Itype50PagerAdapter(
                                parent,
                                context,
                                heightOfPager.toInt(),
                                object : Itype50PagerAdapter.OnChildItemClick {
                                    override fun onUserClick(childPosition: Int) {
                                        if (onParentItemClick != null) {
                                            onParentItemClick.onParentItemClick(
                                                parent,
                                                position,
                                                childPosition
                                            )
                                        }
                                    }

                                    override fun onIconClick(
                                        position: Int, bodyData: BodyRowsItemsItem?
                                    ) {
                                        bannerItemClick?.bannerItemClick(true, position, bodyData)
                                    }

                                    override fun onCheckSatusplaylist(
                                        position: Int,
                                        bodyData: BodyRowsItemsItem?
                                    ) {
                                        bannerItemClick?.onCheckSatusplaylist(true, position, bodyData)
                                    }
                                })
                        }
                        holder.pager.adapter = pagerAdapter
                        holder.pager.offscreenPageLimit = 1

                        if (parent.items?.size!! > 0 && isVisible && first){
                            first  = false
                            mixpanelEventView(parent.items?.get(position)?.data, position)
                        }

                        holder.pagerIndicator.attachToPager(holder.pager)
                        Utils.setMargins(holder.llMain, 0)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
                        Utils.setMargins(holder.llHeaderTitle, context.resources.getDimensionPixelSize(R.dimen.dimen_12))

                        val pageSize = parent.items?.size

                        var isExoPlaying = false
                        var count = 0
                        var auto_scroll_time = 3500L
                        val autoScrollTimeFirebase = CommonUtils.getFirebaseConfigAdsData().hero_section_control.auto_scroll_time

                        if (CommonUtils.getFirebaseConfigAdsData().hero_section_control.auto_scroll_time.isNotEmpty()) {
                            if (autoScrollTimeFirebase.contains("."))
                                auto_scroll_time = autoScrollTimeFirebase.replace(".","").toLong() * 100
                            else
                                auto_scroll_time = autoScrollTimeFirebase.toLong() * 1000;

                        }
                        if(CommonUtils.getFirebaseConfigAdsData().hero_section_control.auto_scroll.isNotEmpty())
                            auto_scroll = CommonUtils.getFirebaseConfigAdsData().hero_section_control.auto_scroll.toBoolean()
                        handler = Handler(Looper.getMainLooper())
                        runnable = Runnable {
                            CoroutineScope(Dispatchers.Main).launch {
                                if (isVisible) {
                                    isExoPlaying = Itype50PagerAdapter.callPlayerList()?.isPlaying == true
                                    if (parent.items?.size!! > holder.pager.currentItem && !parent.items?.get(holder.pager.currentItem)?.data?.type.equals("video")) {
                                        isExoPlaying = false
                                    } else if (parent.items?.size!! > holder.pager.currentItem && parent.items?.get(holder.pager.currentItem)?.data?.type.equals("video") && parent.items?.get(position)?.data?.trailer?.isNotEmpty() == true && !isExoPlaying) {
                                        count += 1
                                        if (count > 2) {
                                            count = 0
                                            isExoPlaying = false
                                        }
                                    }



//                                    setLog("alhgdfa", "$isExoPlaying " + parent.items?.get(holder.pager.currentItem)?.data?.type)

                                    if (pageSize != null && !isExoPlaying) {
                                        if (pageSize == pageCount) {
                                            pageCount = 0
                                            Itype50PagerAdapter.muteIcons.removeAll(Itype50PagerAdapter.muteIcons)
                                            Itype50PagerAdapter.playerlist.removeAll(Itype50PagerAdapter.playerlist)
                                        }
                                        pageCount += 1
                                        if (pageSize > pageCount) {
                                            holder.pager.currentItem = pageCount
                                        }

                                    }
                                    type_of_Scroll = true
                                }
/*                                else
                                {
//                                    Itype50PagerAdapter.itype50AdapterLiveData?.value = Constant.PAUSE
                                }*/
                            }
                            if (auto_scroll)
                                handler?.postDelayed(runnable!!, auto_scroll_time)
                        }
                        if (auto_scroll)
                            handler?.postDelayed(runnable!!, auto_scroll_time)

                        holder.pager.addOnPageChangeListener(object : OnPageChangeListener {
                            override fun onPageScrolled(
                                position: Int,
                                positionOffset: Float,
                                positionOffsetPixels: Int) {
                                pageCount = position

                                CoroutineScope(Dispatchers.Main).launch {
                                    Itype50PagerAdapter.mutePlayer()
                                    Itype50PagerAdapter.muteIconChange()
                                    Itype50PagerAdapter.isMute = true
                                }

                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                if (parent?.items?.size!! > position && parent.items?.get(position)?.data?.type.equals("video") && parent.items?.get(position)?.data?.trailer?.isNotEmpty() == true && parent.items?.get(position)?.data?.playTrailer == true && MainActivity.headerItemPosition==0) {
                                    Itype50PagerAdapter.callPlayerList()?.play()
                                    Itype50PagerAdapter.muteIconChange()
       /*                             if(Itype50PagerAdapter.isMute)
                                        Itype50PagerAdapter.callPlayerList()?.volume = 0F
                                    else
                                        Itype50PagerAdapter.callPlayerList()?.volume = Itype50PagerAdapter.currentVolume*/

                                } else {
                                    Itype50PagerAdapter.callPlayerList()?.pause()
                                }
                                }
                                handler?.removeCallbacksAndMessages(null)
                                if (auto_scroll){
                                    handler = Handler(Looper.getMainLooper())
                                    handler?.postDelayed(runnable!!, auto_scroll_time)
                                }
                            }

                            override fun onPageSelected(position: Int) {

                                if(isVisible && position != parent.items?.size && nonRepeat != position) {
                                    mixpanelEventView(parent.items?.get(position)?.data, position)
                                    nonRepeat = position
                                }

                                if(DiscoverTabFragment.updateBanner<=2) {
                                    if (position == 0 || position == 1) {

                                        val handler = Handler()
                                        handler.postDelayed({

                                            notifyItemChanged(position)
                                            DiscoverTabFragment.updateBanner = DiscoverTabFragment.updateBanner+1

                                        }, 100)
                                    }
                                }else DiscoverTabFragment.updateBanner = DiscoverTabFragment.updateBanner++
                                }

                            override fun onPageScrollStateChanged(state: Int) {

                                if (state == ViewPager.SCROLL_STATE_IDLE) {
                                    if (pageCount == (parent.items?.size?.minus(1))) {
                                        Itype50PagerAdapter.muteIcons.removeAll(Itype50PagerAdapter.muteIcons)
                                        Itype50PagerAdapter.playerlist.removeAll(Itype50PagerAdapter.playerlist)
                                        holder.pager.setCurrentItem(0, false)
                                        pageCount = 0
                                    }
                                }
                                type_of_Scroll = false
                            }
                        })


                    }
                    21 -> {
                        holder.rvChildItem.visibility = View.GONE
                        holder.dotedView.visibility = View.VISIBLE
                        holder.dotedView.layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.dimen_225)
                        //setChildRecyclerView(holder, layoutManager, parent, position)

                        setLog("TAG", "onBindViewHolder Itype23PagerAdapter:111 ")
                        val pagerAdapter = Itype21PagerAdapter(parent, context,
                            object : Itype21PagerAdapter.OnChildItemClick {
                                override fun onUserClick(childPosition: Int) {
                                    if (onParentItemClick != null) {
                                        onParentItemClick.onParentItemClick(
                                            parent,
                                            position,
                                            childPosition
                                        )
                                    }
                                }

                            })
                        holder.pager.adapter = pagerAdapter
                        holder.pagerIndicator.attachToPager(holder.pager)

                        Utils.setMargins(holder.llMain, 0)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    23 -> {
                        holder.rvChildItem.visibility = View.GONE
                        holder.dotedView.layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.dimen_225)
                        holder.dotedView.visibility = View.VISIBLE
                        //setChildRecyclerView(holder, layoutManager, parent, position)

                        val pagerAdapter =
                            Itype23PagerAdapter(
                                parent,
                                context
                            ) { childPosition ->
                                if (onParentItemClick != null) {
                                    onParentItemClick.onParentItemClick(
                                        parent,
                                        position,
                                        childPosition
                                    )
                                }
                            }

                        holder.pager.adapter = pagerAdapter
                        holder.pagerIndicator.attachToPager(holder.pager)

                        Utils.setMargins(holder.llMain, 0)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    47 -> {

                        val model = BodyRowsItemsItem()
                        model.itype = parent.itype
                        model.type = parent.type.toString()
                        model.id = parent.id.toString()
                        parent.orignalItems?.get(0)?.image = parent.image!!
                        parent.orignalItems?.get(0)?.videoUrl = parent.videoUrl!!
                        parent.orignalItems?.get(0)?.description = parent.description!!

                        model.orignalItems = parent.orignalItems

                        parent.items = ArrayList<BodyRowsItemsItem?>()
                        parent.items?.add(0, model)

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(0, 0, 0, 0)
//                    Utils.setMargins(holder.llHeaderTitle, context.resources.getDimensionPixelSize(R.dimen.dimen_12))
//                    Utils.setMarginsTop(
//                        holder.llHeaderTitle,
//                        commonSpaceBetweenBuckets
//                    )
                    }
                    51 -> {

                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_25)
                        )
                        Utils.setMarginsBottom(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_20)
                        )
                    }
                    BucketChildAdapter.ROW_ITYPE_101,
                    BucketChildAdapter.ROW_ITYPE_102,
                    BucketChildAdapter.ROW_ITYPE_103,
                    BucketChildAdapter.ROW_ITYPE_104 -> {

                        holder.llHeaderTitle.visibility = View.INVISIBLE
                        setChildRecyclerView(holder, layoutManager, parent, position)
                        holder.rvChildItem.setPadding(
                            context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                            0,
                            0,
                            0
                        )
                        Utils.setMargins(
                            holder.llHeaderTitle,
                            context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                        )
                        Utils.setMarginsTop(
                            holder.llHeaderTitle,
                            commonSpaceBetweenBuckets
                        )
                    }
                    BucketChildAdapter.ROW_ITYPE_201 -> {
                        setLog("TAG", "onBindViewHolder datamodel: ${parent.items?.get(0)?.data}")
                        if (!parent.items.isNullOrEmpty()
                            && parent.items?.get(0)?.data != null && parent.items?.get(0)?.data?.isVisible!!
                        ) {

                            holder.llHeaderTitle.visibility = View.GONE
                            setChildRecyclerView(holder, layoutManager, parent, position)
                            holder.rvChildItem.setPadding(
                                context.resources.getDimensionPixelSize(R.dimen.dimen_18),
                                0,
                                0,
                                0
                            )
                            Utils.setMargins(
                                holder.llHeaderTitle,
                                context.resources.getDimensionPixelSize(R.dimen.dimen_12)
                            )
                            Utils.setMarginsTop(
                                holder.llHeaderTitle,
                                commonSpaceBetweenBuckets
                            )
                            holder.rvChildItem.visibility = View.VISIBLE
                        }
                        else {
                            holder.rvChildItem.visibility = View.GONE
                        }

                    }
                    else -> {
                        setLog("NoView", "NoVIew 2")
                        holder.rvChildItem.visibility = View.GONE
                        holder.pager.visibility = View.GONE
                    }
                }

                if (position == 0) {
                    Utils.setMarginsTop(
                        holder.llHeaderTitle,
                        context.resources.getDimensionPixelSize(R.dimen.dimen_8)
                    )

                } else if (position == 1 && parents.get(0)?.items?.isEmpty() == true) {
                    Utils.setMarginsTop(
                        holder.llHeaderTitle,
                        context.resources.getDimensionPixelSize(R.dimen.dimen_28)
                    )
                }
            } else {
                setLog("NoView", "NoVIew 1")
                holder.rvChildItem.visibility = View.GONE
                holder.dotedView.visibility = View.GONE
            }
        }

    }

    var first = true
    var nonRepeat = 0


    fun mixpanelEventView(data: BodyDataItem?, position: Int) {
        val hashMapPageView = HashMap<String, String>()

        hashMapPageView[EventConstant.banner_title] = data?.title.toString()
        hashMapPageView[EventConstant.banner_type] = data?.type.toString()
        hashMapPageView[EventConstant.hero_card_swiped] =
            if (BucketParentAdapter.type_of_Scroll) "Auto" else "Manual"
        hashMapPageView[EventConstant.hero_card_position] = position.toString()
        hashMapPageView[EventConstant.CONTENTTYPE_EPROPERTY] = data?.contentType.toString()
        hashMapPageView[EventConstant.CONTENTID_EPROPERTY] = data?.id.toString()
        hashMapPageView[EventConstant.deeplink] = data?.deeplink_url.toString()
        hashMapPageView[EventConstant.Primary_cta] = data?.primaryCta?.id.toString()
        hashMapPageView[EventConstant.secondary_cta] = data?.secondaryCta.toString()
        hashMapPageView[EventConstant.trailer_played] = data?.trailer.toString()
        hashMapPageView[EventConstant.ismuted] = data?.playWithSound.toString()

        EventManager.getInstance().sendEvent(Hero_card_viewed(hashMapPageView))
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvChildItem: RecyclerView = itemView.rvBucketItem
        val blurView: RelativeLayout = itemView.blurView
        val tvTitle: TextView = itemView.tvTitle
        val tvSubTitle: TextView = itemView.tvSubTitle
        val switchPublic: SwitchCompat = itemView.switchPublic
        val ivMore: ImageView = itemView.ivMore
        val llMain: LinearLayoutCompat = itemView.llMain
        val llHeaderTitle: LinearLayoutCompat = itemView.llHeaderTitle
        val dotedView = itemView.dotedView
        val pager: ViewPager = itemView.pager
        val pagerIndicator: ScrollingPagerIndicator = itemView.pager_indicator

    }


    private fun setChildRecyclerView(
        holder: ViewHolder,
        layoutManager2: GridLayoutManager,
        parent: RowsItem,
        position: Int
    ) {
        val bucketChildAdapter = BucketChildAdapter(context, parent.items!!, varient,
            object : BucketChildAdapter.OnChildItemClick {
                override fun onUserClick(childPosition: Int, view: View?) {
                    if (onParentItemClick != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            setClickAnimation(context, view)
                            delay(150)
                            onParentItemClick.onParentItemClick(parent, position, childPosition)
                        }
                    }
                }

                override fun onInAppSubmitClick(
                    inAppModel: InAppSelfHandledModel?,
                    childPosition: Int
                ) {
                    setLog(
                        "setMoengageData",
                        "onInAppSubmitClick: called title:${parent.heading} position:${position} childPosition:${childPosition}length:${parents.size}"
                    )
                    if (parents != null && position < parents.size) {
                        parents.removeAt(position)
                        // call whenever in-app is dismissed
                        MoEInAppHelper.getInstance()
                            .selfHandledDismissed(context, inAppModel?.inAppCampaign!!)
                        notifyItemRemoved(position)
                        InAppCallback.mInAppCampaignList.values.remove(inAppModel)

                        notifyItemRangeChanged(position - 1, 2)

                        setLog("setMoengageData", "onInAppSubmitClick: anser called")
                        setLog("setMoengageData", "onInAppSubmitClick: selfHandledDismissed called")
                        setLog(
                            "setMoengageData",
                            "onInAppSubmitClick: called length:${parents.size}"
                        )


                        if (inAppModel.inAppCampaign.campaign != null && childPosition <= inAppModel?.options!!.size) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val dataMap = java.util.HashMap<String, String>()
                                inAppModel?.campaign_id?.let {
                                    dataMap.put(
                                        EventConstant.CAMPAIGN_ID_EPROPERTY,
                                        it
                                    )
                                }
                                inAppModel?.templateId?.let {
                                    dataMap.put(
                                        EventConstant.TEMPLATE_ID_EPROPERTY,
                                        it
                                    )
                                }
                                inAppModel?.title?.let {
                                    dataMap.put(EventConstant.TITLE_EPROPERTY,
                                        it
                                    )
                                }
                                inAppModel?.subTitle?.let {
                                    dataMap.put(
                                        EventConstant.SUBTITLE_EPROPERTY,
                                        it
                                    )
                                }
                                dataMap.put(
                                    EventConstant.OPTION_EPROPERTY,
                                    "" + inAppModel?.options
                                )
                                dataMap.put(
                                    EventConstant.OPTION_TAPPED_EPROPERTY,
                                    "" + inAppModel?.userAnswer
                                )
                                dataMap.put(
                                    EventConstant.BOTTOM_NAV_POSITION_EPROPERTY,
                                    "" + inAppModel?.bottom_nav_position
                                )
                                dataMap.put(
                                    EventConstant.TOP_NAV_POSITION_EPROPERTY,
                                    "" + inAppModel?.top_nav_position
                                )
                                dataMap.put(
                                    EventConstant.POSITION_EPROPERTY,
                                    "" + inAppModel?.position
                                )
                                EventManager.getInstance()
                                    .sendEvent(ProgressiveSurveyTappedEvent(dataMap))
                            }

                        }
                    }
                }

            },
        onMoreItemClick, parent)

        if (parent.keywords != null && parent.keywords?.contains("continue-watching")!!) {
            bucketChildAdapterRecently = bucketChildAdapter
        }

        holder.rvChildItem.apply {
            layoutManager = layoutManager2
            adapter = bucketChildAdapter
//            setRecycledViewPool(viewPool)
            //setHasFixedSize(true)
        }

        //holder.rvChildItem.setItemViewCacheSize(parent.items?.size!!)

        itype47AdapterLiveData.observe(context as LifecycleOwner, Observer {
            setLog("alhgsgdfldash", " $it")
            if (parent.itype == 47 && !parent.items.isNullOrEmpty() && parent?.items?.size!! > 0 && it == Constant.NOTIFY){
                CoroutineScope(Dispatchers.Main).launch {
                    bucketChildAdapter.notifyItemChanged(0)
                }
            }
        })

        holder.rvChildItem.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                setLog("SwipedEventCall", " " + "Swiped called")

                val manager: GridLayoutManager =
                    (holder.rvChildItem.layoutManager as GridLayoutManager)
                val visiblePosition: Int = manager.findLastCompletelyVisibleItemPosition()

                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String, String>()
                    hashMap.put(EventConstant.BUCKETSWIPED_EPROPERTY, MainActivity.lastItemClicked)
                    //hashMap.put(EventConstant.SOURCE_EPROPERTY,""+Utils.getContentTypeDetailName(""+parent?.type))
                    hashMap.put(
                        EventConstant.SOURCE_EPROPERTY,
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + parent.heading
                    )
                    hashMap.put(EventConstant.BUCKETNAME_EPROPERTY, parent.heading!!)
                    Constant.title_heading = parent.heading!!
                    hashMap.put(EventConstant.LASTPOSITIONOFBUCKET_EPROPERTY, "" + visiblePosition)

                    if (BaseActivity.eventManagerStreamName != EventConstant.BUCKETSWIPED_ENAME) {
                        BaseActivity.eventManagerStreamName = EventConstant.BUCKETSWIPED_ENAME
                        EventManager.getInstance().sendEvent(BucketSwipedEvent(hashMap))
                    }

                }

            }
        })
        if (parent.itype == BucketChildAdapter.ROW_ITYPE_47) {
            setOriginalBucketAdapter(bucketChildAdapter)
            registerReceiver()
        }
    }

    fun updateList(data: ArrayList<BodyDataItem>?, parentPosition: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!data.isNullOrEmpty()) {
                data.forEachIndexed { index, bodyDataItem ->
                    //this.parents.get(parentPosition)?.items?.get(index)?.data?.misc?.post = bodyDataItem?.misc?.post
                    this@BucketParentAdapter.parents.get(parentPosition)?.items?.get(index)?.data =
                        bodyDataItem
                }
                isStoryUpdate = true
                withContext(Dispatchers.Main) {
                    notifyItemChanged(parentPosition)
                }
            }
        }
    }

    fun getListData(): ArrayList<RowsItem?> {
        return parents
    }

    interface OnMoreItemClick {
        fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    var continueWhereLeftModel: ContinueWhereLeftModel? = null

    private fun setUpContinueWhereLeftListViewModel() {
        if (ConnectionUtil(context).isOnline) {
            val continueWhereLeftViewModel = ViewModelProvider(context as AppCompatActivity).get(RecentlyPlayViewModel::class.java)

            continueWhereLeftViewModel.getContinueWhereLeftList(
                context,
                bottomTabID,
                headItemsItem
            )?.observe(context,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setLog("continue-watching", "continue-watching 1")
                            if (it != null) {
                                continueWhereLeftModel = it.data
                                HungamaMusicApp.getInstance().setContinueWhereLeftData(continueWhereLeftModel!!)
                                parents.get(continueWatchIndex)?.items = ArrayList()

                                setLog(
                                    "continue-watching",
                                    "continue-watching 11:${continueWhereLeftModel?.size}"
                                )
                                if (continueWhereLeftModel?.size!! > Constant.MIN_RECENTPLAY_SIZE) {
                                    isKeywordWatchCall = 1000
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 11:${parents.get(continueWatchIndex)?.items?.size}"
                                    )
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 111:${continueWhereLeftModel?.get(0)?.data?.title}"
                                    )
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 1111:${
                                            continueWhereLeftModel?.get(continueWhereLeftModel?.size!! - 1)?.data?.title
                                        }"
                                    )

                                    parents.get(continueWatchIndex)?.items = continueWhereLeftModel
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 2:${parents.get(continueWatchIndex)?.items?.size}"
                                    )
                                } else {
                                    setLog(
                                        "continue-watching",
                                        "continue-watching 3:${parents.get(continueWatchIndex)?.items?.size}"
                                    )
                                    parents.get(continueWatchIndex)?.items = ArrayList()
                                }
                                addData(parents as MutableList<RowsItem?>)

                            }
                        }

                        Status.LOADING -> {
                        }

                        Status.ERROR -> {
                            setLog(
                                "continue-watching",
                                "setUpContinueWhereLeftListViewModel: ${it.message!!}"
                            )

                        }
                    }
                })
        }
    }


    override fun playItemChange() {
        setLog("TAG", "continue playItemChange: ")
        isKeywordWatchCall = 999
        notifyItemChanged(continueWatchIndex)
    }

    var originalBucketChildAdapter: BucketChildAdapter? = null
    fun setOriginalBucketAdapter(adapter: BucketChildAdapter) {
        originalBucketChildAdapter = adapter
    }

    var screenOnOffReceiver: BroadcastReceiver? = null
    fun registerReceiver() {
        unRegisterOriginalReceiver()
        val theFilter = IntentFilter()
        /** System Defined Broadcast  */
        theFilter.addAction(Intent.ACTION_SCREEN_ON)
        theFilter.addAction(Intent.ACTION_SCREEN_OFF)
        theFilter.addAction(Intent.ACTION_USER_PRESENT)
        screenOnOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val strAction = intent.action
                val myKM: KeyguardManager =
                    context.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
                if (strAction == Intent.ACTION_USER_PRESENT || strAction == Intent.ACTION_SCREEN_OFF || strAction == Intent.ACTION_SCREEN_ON) if (myKM.inKeyguardRestrictedInputMode()) {
                    setLog("isDeviceLocked", "LOCKED")
                    pauseOriginalPlayer()
                } else {
                    setLog("isDeviceLocked", "UNLOCKED")
                    playOriginalPlayer()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        context.registerReceiver(screenOnOffReceiver, theFilter,Context.RECEIVER_NOT_EXPORTED)
        else
        context.registerReceiver(screenOnOffReceiver, theFilter)
    }

    private fun unRegisterOriginalReceiver() {
        if (screenOnOffReceiver != null) {
            context.unregisterReceiver(screenOnOffReceiver)
            screenOnOffReceiver = null
        }
    }

    fun playOriginalPlayer() {
        if (originalBucketChildAdapter != null) {
            originalBucketChildAdapter?.playPlayer()
        }
    }

    fun pauseOriginalPlayer() {
        if (originalBucketChildAdapter != null) {
            originalBucketChildAdapter?.pausePlayer()
        }
    }

    fun stopOriginalPlayer() {
        try {
            unRegisterOriginalReceiver()
            if (originalBucketChildAdapter != null) {
                originalBucketChildAdapter?.releasePlayer()
            }
        } catch (e: Exception) {

        }

    }
}
