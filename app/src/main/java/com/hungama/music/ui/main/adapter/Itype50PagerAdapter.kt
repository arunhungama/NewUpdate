package com.hungama.music.ui.main.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.*
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.viewpager.widget.PagerAdapter
import com.hungama.fetch2.Status
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.Hero_card_viewed
import com.hungama.music.eventanalytic.eventreporter.LiveConcertReminderEvent
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.download.DemoUtil
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ImageLoader.loadImage
import com.hungama.music.utils.customview.fontview.FontAwesomeImageView
import com.hungama.music.utils.fontmanger.FontDrawable
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class Itype50PagerAdapter(
    private val rowsItem: RowsItem,
    private val ctx: Context,
    bannerHeight1 :Int = 0,
    onChildItemClick: OnChildItemClick?
) : PagerAdapter(), OnUserSubscriptionUpdate {
    companion object {
        var playerlist: ArrayList<ExoPlayer?> = ArrayList()
        var muteIcons: ArrayList<FontAwesomeImageView?> = ArrayList()
        var isMute = true
        var isVisible = true
        var currentVolume = 0.0f

        fun callPlayerList(): ExoPlayer?{
            if (playerlist.isEmpty())
                return null
            else if (playerlist.size == 1)
                return playerlist[0]
            else if (playerlist.size >= 1)
            return playerlist[1]

            return null
        }

        fun muteIconChange(){
            if (muteIcons.isNotEmpty()){
                for (item in muteIcons){
                    item?.setImageDrawable(HungamaMusicApp.getInstance().applicationContext.faDrawable(R.string.icon_mute, R.color.colorWhite, HungamaMusicApp.getInstance().applicationContext.resources.getDimensionPixelSize(R.dimen.font_16).toFloat()))
                }
            }
        }

        fun mutePlayer(){
            if (playerlist.isNotEmpty()){
                for (player in playerlist){
                    player?.volume = 0F
                }
            }
        }

        fun pausePlayer(){
            if (playerlist.isNotEmpty()){
                for (player in playerlist){
                    player?.pause()
                }
            }
        }

        fun addRemovePlayer(player:ExoPlayer?){
            if (playerlist.isEmpty()){
                playerlist.add(player)
                playerlist.add(player)
            }
            else{
                playerlist.add(0, player)
                playerlist.removeAt((playerlist.size-1))
            }
        }

        fun addRemoveMuteIcon(icon:FontAwesomeImageView){
//            if (muteIcons.isEmpty()){
                muteIcons.add(icon)
     /*           muteIcons.add(icon)
            }
            else{
                muteIcons.add(0, icon)
                muteIcons.removeAt((muteIcons.size-1))
            }*/

        }

        var itype50AdapterLiveData: MutableLiveData<String> = MutableLiveData<String>()

    }

    private var pageCount: Int
    var onChildItemClick: OnChildItemClick?
    var simpleExoplayerr: ExoPlayer? = null
    val bannerHeight = bannerHeight1


    interface OnChildItemClick {
        fun onUserClick(childPosition: Int)
        fun onIconClick(position: Int, bodyData: BodyRowsItemsItem?)
        fun onCheckSatusplaylist(position: Int, bodyData: BodyRowsItemsItem?)

    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(collection.context).inflate(R.layout.row_itype_50, collection, false) as ViewGroup
        val title = layout.findViewById<TextView>(R.id.tvTitle)
        val subTitle = layout.findViewById<TextView>(R.id.tvSubTitle)
        val ivUserImage = layout.findViewById<ImageView>(R.id.ivUserImage)
        val ivMuteUnmute = layout.findViewById<FontAwesomeImageView>(R.id.ivMuteUnmute)
        val vTopBottom = layout.findViewById<View>(R.id.vTopBottom)
        val episode_player_view = layout.findViewById<PlayerView>(R.id.episode_player_view)
        val preViewProgressBar = layout.findViewById<ProgressBar>(R.id.preViewProgressBar)
        val llMute = layout.findViewById<LinearLayoutCompat>(R.id.llMute)
        val ivAction = layout.findViewById<FontAwesomeImageView>(R.id.ivAction)
        val llMain: ConstraintLayout = layout.findViewById(R.id.llMain)
        val txtRent = layout.findViewById<TextView>(R.id.txtRent)
        val ivRent = layout.findViewById<ImageView>(R.id.ivRent)
        val llRentBanner: LinearLayoutCompat = layout.findViewById(R.id.llRentBanner)
        val rlAction: RelativeLayout = layout.findViewById(R.id.rlAction)

        if (!rowsItem.items.isNullOrEmpty() && rowsItem.items?.size!! > position) {

            itype50AdapterLiveData.observe(ctx as LifecycleOwner, Observer {
                setLog("alhgldash", " $it")

                if (it == Constant.PAUSE)
                simpleExoplayerr?.pause()
            })
            val displayMetrics = DisplayMetrics()
            (ctx as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            val aspectRadio = bannerHeight.toFloat()/displayMetrics.widthPixels.toFloat()
            val width = aspectRadio.toFloat() * displayMetrics.widthPixels.toFloat()
            if (bannerHeight > 0){
                val height = (bannerHeight * 55)/100
                episode_player_view.layoutParams.height = height
                vTopBottom.layoutParams.height = height
                if (width > displayMetrics.widthPixels) {
                    episode_player_view.layoutParams.width = width.toInt()
                    vTopBottom.layoutParams.width = width.toInt()
                }
            }
            if (CommonUtils.isUserHasGoldSubscription() && rowsItem.items?.get(position)?.data?.primaryCta?.id == Constant.Subscribe_Now){
                rowsItem.items?.get(position)?.data?.primaryCta?.id = Constant.WATCH_NOW
            }

            if (!TextUtils.isEmpty(rowsItem.items?.get(position)?.data?.title)) {
                title.text = rowsItem.items?.get(position)?.data?.title.toString()
                title.visibility = View.VISIBLE
            } else {
                title.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(rowsItem.items?.get(position)?.data?.secondaryCta) || rowsItem.items?.get(
                    position
                )?.data?.secondaryCta != null
            ) {
                subTitle.text = rowsItem.items?.get(position)?.data?.secondaryCta
                subTitle.visibility = View.VISIBLE
            } else {
                subTitle.visibility = View.GONE
            }
            title.visibility = View.GONE
            subTitle.visibility = View.GONE

            val ctaId = rowsItem.items?.get(position)?.data?.primaryCta?.id
            if (ctaId == "Play Now" || ctaId == "Watch Now" || ctaId == "Explore Now") {
                ivRent.setImageDrawable(
                    ctx.faDrawable(
                        R.string.icon_play_2,
                        R.color.colorBlack
                    )
                )
            } else if (ctaId == "Subscribe Now" || ctaId == "Buy Now")
                ivRent.setImageDrawable(ctx.faDrawable(R.string.icon_crown, R.color.colorBlack))
            else if (ctaId == "Remind Me") {
                var share_link=""
                 if(rowsItem.items?.get(position)?.data?.deepLink?.isNotEmpty() == true)
                     share_link = rowsItem.items?.get(position)?.data?.deepLink?.split("/live/")?.get(1)?.split("/")?.get(0)!!

                val keyName= SharedPrefHelper.getInstance().getUserId()+"_"+ share_link
             setLog("alhgasdh", "list $keyName")
                if(!keyName.isNullOrEmpty())
                    if(SharedPrefHelper.getInstance().has(keyName)){
                        ivRent?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_baseline_done_24))
                        DrawableCompat.setTint(DrawableCompat.wrap(ivRent.drawable), ContextCompat.getColor(ctx, R.color.colorBlack))
                    }else{
                        ivRent?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_clock))
                        DrawableCompat.setTint(DrawableCompat.wrap(ivRent.drawable), ContextCompat.getColor(ctx, R.color.colorBlack))
                    }
            }
            else if (ctaId == "Rent Now") {
                ivRent.setImageDrawable(
                    ctx.faDrawable(
                        R.string.icon_rent,
                        R.color.colorBlack
                    )
                )
            }

            if (rowsItem.items?.get(position)?.data?.primaryCta != null && !rowsItem.items?.get(
                    position
                )?.data?.primaryCta?.title.isNullOrEmpty()
            ) {

                val data = rowsItem.items?.get(position)?.data?.primaryCta?.id.toString()
                when (data) {
                    "Play Now" -> txtRent.text = ctx.getString(R.string.hero_banner_cta_str_1)
                    "Explore Now" -> txtRent.text = ctx.getString(R.string.hero_banner_cta_str_2)
                    "Watch Now" -> txtRent.text = ctx.getString(R.string.hero_banner_cta_str_3)
                    "Subscribe Now" -> txtRent.text = ctx.getString(R.string.hero_banner_cta_str_4)
                    "Remind Me" -> txtRent.text = ctx.getString(R.string.hero_banner_cta_str_5)
                    "Buy Now" -> txtRent.text = ctx.getString(R.string.hero_banner_cta_str_6)
                    "Rent Now" -> txtRent.text = ctx.getString(R.string.hero_banner_cta_str_7)
                }
            } else {
                txtRent.visibility = View.GONE
            }
            if (rowsItem.items?.get(position)?.data?.image != null) {
                loadImage(
                    ctx,
                    ivUserImage,
                    rowsItem.items?.get(position)?.data?.image.toString(),
                    R.drawable.bg_gradient_placeholder
                )
            }

            onClickEvent(position, ivAction)


            if (rowsItem.items?.get(position)?.data?.contentTypeId == "55555" && rowsItem.items?.get(
                    position
                )?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId == "96" && rowsItem.items?.get(
                    position
                )?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId == "1" && rowsItem.items?.get(
                    position
                )?.data?.secondaryCta.equals(Constant.Download)

                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "97",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)

                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "102",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "107",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "109",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
            ) {
                rowsItem.items?.get(position)?.data?.isDownloading?.let {

                    if(it.equals("")) {
                        onChildItemClick?.onCheckSatusplaylist(
                            position,
                            rowsItem.items?.get(position)
                        )

                    }else {

                        downloadIconStates(
                            it.toInt(),
                            ivAction,
                            rowsItem.items?.get(position)?.data
                        )
                    }
                }

            } else donwloadIconChanges(rowsItem.items?.get(position)?.data, ivAction)


             CheckDownloading(ivAction, position)
/*            rowsItem.items?.get(position)?.data?.playTrailer = false
            rowsItem.items?.get(position)?.data?.image = ""*/

            if (rowsItem.items?.get(position)?.data?.type.equals("video") && rowsItem.items?.get(position)?.data?.trailer?.isNotEmpty() == true && rowsItem.items?.get(position)?.data?.playTrailer == true) {
                episode_player_view.visibility = View.VISIBLE
                preViewProgressBar.visibility = View.VISIBLE
                llMute.visibility = View.VISIBLE
                vTopBottom.visibility = View.VISIBLE
                ivUserImage.visibility = View.GONE

                ivUserImage.Invisiable()
                preViewProgressBar.show()
                val renderersFactory = DefaultRenderersFactory(ctx)

                var mediaSource: MediaSourceFactory? = null
                val track = Track()
//                    track.id = list.get(position)?.orignalItems?.get(0)?.data?.id?.trim()?.toLong()!!
                track.title = rowsItem.items?.get(position)?.data?.title
                track.url = rowsItem.items?.get(position)?.data?.trailer
                mediaSource = DefaultMediaSourceFactory(DemoUtil.getDataSourceFactory(ctx))

                val simpleExoplayer = ExoPlayer.Builder(ctx, renderersFactory)
                    .setMediaSourceFactory(mediaSource)
                    .setHandleAudioBecomingNoisy(true).build()

                simpleExoplayer?.setMediaItem(CommonUtils.setMediaItem(track))
                simpleExoplayer?.prepare()
                episode_player_view.player = simpleExoplayer
//                simpleExoplayer?.playWhenReady = rowsItem.items?.get(position)?.data?.playTrailer == true
                currentVolume = simpleExoplayer?.volume!!
                isMute = rowsItem.items?.get(position)?.data?.playWithSound != true
                if (isMute) {
                    simpleExoplayer?.volume = 0.0f
                    ivMuteUnmute?.setImageDrawable(
                        ctx.faDrawable(
                            R.string.icon_mute,
                            R.color.colorWhite,
                            ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                .toFloat()
                        )
                    )
                }
                else {
                    simpleExoplayer?.volume = currentVolume
                    ivMuteUnmute?.setImageDrawable(
                        ctx.faDrawable(
                            R.string.icon_unmute,
                            R.color.colorWhite,
                            ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                .toFloat()
                        )
                    )
                }

                simpleExoplayerr = simpleExoplayer

                simpleExoplayer?.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
//                            super.onPlaybackStateChanged(playbackState)
                        if (playbackState == Player.STATE_READY) {
                            BaseFragment.isPlayBannerVPlaying = true
                        } else if (playbackState == Player.STATE_ENDED) {
                            if (rowsItem.items?.get(position)?.data?.loopTrailer == false) {
                                simpleExoplayer?.play()
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        ivUserImage.show()
                        episode_player_view.Invisiable()
                        preViewProgressBar.hide()
                        vTopBottom.hide()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        if (isPlaying) {
                            ivUserImage.hide()
                            episode_player_view.show()
                            preViewProgressBar.hide()
                            llMute.show()
                            vTopBottom.show()
                            llMute.setOnClickListener {
                                if (isMute) {
                                    simpleExoplayer?.volume = currentVolume
                                    ivMuteUnmute?.setImageDrawable(
                                        ctx.faDrawable(
                                            R.string.icon_unmute,
                                            R.color.colorWhite,
                                            ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                                .toFloat()
                                        )
                                    )
                                    if ((ctx as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                        (ctx as MainActivity).currentPlayer?.pause()
                                    }
                                } else {
                                    simpleExoplayer?.volume = 0.0f
                                    ivMuteUnmute?.setImageDrawable(
                                        ctx.faDrawable(
                                            R.string.icon_mute,
                                            R.color.colorWhite,
                                            ctx.resources.getDimensionPixelSize(R.dimen.font_16)
                                                .toFloat()
                                        )
                                    )
                                }

                                isMute = !isMute
                            }
                        } else {
                            ivUserImage.show()
                            episode_player_view.Invisiable()
                            llMute.hide()
                            vTopBottom.hide()
                            preViewProgressBar.hide()
                            llMute.setOnClickListener(null)
                            BaseFragment.isPlayBannerVPlaying = false
                        }
                    }
                })
                episode_player_view.addOnAttachStateChangeListener(object :
                    View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        setLog("TAG", "onViewRecycled-onViewAttachedToWindow-$v")
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        setLog("TAG", "onViewRecycled-onViewDetachedFromWindow-$v")
                        episode_player_view.player?.stop()
                        episode_player_view.player?.release()
                        preViewProgressBar.hide()
                    }

                })
            } else {
                ivUserImage.visibility = View.VISIBLE
                episode_player_view.visibility = View.GONE
                preViewProgressBar.visibility = View.GONE
                llMute.visibility = View.GONE
                vTopBottom.visibility = View.GONE
            }

            llMain.setOnClickListener { v: View? ->
                if (onChildItemClick != null) {
                    val ctaId = rowsItem.items?.get(position)?.data?.primaryCta?.id
                    if (ctaId == "Play Now" ||
                        ctaId == "Watch Now"
                    ) {
                        BaseFragment.isPlayFromBanner = true
                        onChildItemClick?.onUserClick(position)
                    } else if (ctaId == "Subscribe Now") {

                        Constant.screen_name = "Home Screen"
                        CommonUtils.openSubscriptionDialogPopup(
                            ctx,
                            PlanNames.SVOD.name,
                            "",
                            true,
                            null,
                            "",
                            null,
                            Constant.nudge_home_header_banner,
                            "banner"
                        )

                    } else {
                        BaseFragment.isPlayFromBanner = false
                        onChildItemClick?.onUserClick(position)
                    }
                }
            }
            llRentBanner.setOnClickListener { v: View? ->
                if (onChildItemClick != null) {
                    val ctaId = rowsItem.items?.get(position)?.data?.primaryCta?.id
                    if (ctaId == "Play Now" ||
                        ctaId == "Watch Now" || ctaId == "Subscribe Now"
                    ) {
                        BaseFragment.isPlayFromBanner = true
                        onChildItemClick?.onUserClick(position)
                    }
                    else if (ctaId == "Remind Me") {
                        var share_link =""
                        if(rowsItem.items?.get(position)?.data?.deepLink?.isNotEmpty() == true)
                            share_link = rowsItem.items?.get(position)?.data?.deepLink?.split("/live/")?.get(1)?.split("/")?.get(0)!!

                        val keyName= SharedPrefHelper.getInstance().getUserId()+"_"+ share_link

                        setLog("alhgasdh", "click $keyName")
                        if(!keyName.isNullOrEmpty())
                            if(!SharedPrefHelper.getInstance().has(keyName)){

                                ivRent?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_baseline_done_24))
                                DrawableCompat.setTint(DrawableCompat.wrap(ivRent.drawable), ContextCompat.getColor(ctx, R.color.colorBlack))
                                //llRemindMe?.isEnabled=false

                                SharedPrefHelper.getInstance().save(keyName,true)
                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = java.util.HashMap<String, String>()
                                    hashMap.put(EventConstant.CONCERT_NAME, rowsItem.items?.get(position)?.data?.title.toString())
                                    hashMap.put(EventConstant.CONCERT_ID, rowsItem.items?.get(position)?.data?.primaryCta?.id.toString())
             /*                       hashMap.put(EventConstant.DATE_OF_CONCERT, tvDate.text?.toString()!!)
                                    hashMap.put(EventConstant.TIME_OF_CONCERT, tvTime.text?.toString()!!)*/
                                    setLog("TAG","login${hashMap}")
                                    EventManager.getInstance().sendEvent(LiveConcertReminderEvent(hashMap))
                                }

                            }else{
                                SharedPrefHelper.getInstance().delete(keyName)
                                ivRent?.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.ic_clock))
                                DrawableCompat.setTint(DrawableCompat.wrap(ivRent.drawable), ContextCompat.getColor(ctx, R.color.colorBlack))

                                //llRemindMe?.isEnabled=true
                            }
                    }
                    else {
                        BaseFragment.isPlayFromBanner = false
                        onChildItemClick?.onUserClick(position)
                    }
                }
            }
            ivUserImage.setOnClickListener {
                if (onChildItemClick != null) {
                    val ctaId = rowsItem.items?.get(position)?.data?.primaryCta?.id
                    if (ctaId == "Play Now" ||
                        ctaId == "Watch Now" || ctaId == "Subscribe Now"
                    ) {
                        BaseFragment.isPlayFromBanner = true
                        onChildItemClick?.onUserClick(position)
                    } else {
                        BaseFragment.isPlayFromBanner = false
                        onChildItemClick?.onUserClick(position)
                    }
                }
            }

            vTopBottom.setOnClickListener {
                if (onChildItemClick != null) {
                    val ctaId = rowsItem.items?.get(position)?.data?.primaryCta?.id
                    if (ctaId == "Play Now" ||
                        ctaId == "Watch Now" || ctaId == "Subscribe Now"
                    ) {
                        BaseFragment.isPlayFromBanner = true
                        onChildItemClick?.onUserClick(position)
                    } else {
                        BaseFragment.isPlayFromBanner = false
                        onChildItemClick?.onUserClick(position)
                    }
                }
            }

            rlAction.setOnClickListener {
                val secondaryCta = rowsItem.items?.get(position)?.data?.secondaryCta.toString()
                onChildItemClick?.onIconClick(position, rowsItem.items?.get(position))
                if (rowsItem.items?.get(position)?.data?.contentTypeId == "55555" && rowsItem.items?.get(
                        position
                    )?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId == "109" && rowsItem.items?.get(
                        position)?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId == "1" && rowsItem.items?.get(
                        position
                    )?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId == "96" && rowsItem.items?.get(
                        position
                    )?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                        "97",
                        true
                    ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                        "22",
                        true
                    ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                        "98",
                        true
                    ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                        "102",
                        true
                    ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                        "107",
                        true
                    ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                    || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                        "109",
                        true
                    ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                ) {

                    if (rowsItem.items?.get(position)?.data?.isFollow == false) {
                        if (CommonUtils.isUserHasGoldSubscription()) {
                            rowsItem.items?.get(position)?.data?.isDownloading = "2"
                            downloadIconStates(2, ivAction, rowsItem.items?.get(position)?.data)
                        }
                    } else donwloadIconChanges(rowsItem.items?.get(position)?.data, ivAction)

                    if (CommonUtils.isUserHasGoldSubscription()) rowsItem.items?.get(position)?.data?.isFollow =
                        true

                }else if(rowsItem.items?.get(position)?.data?.contentTypeId.equals("4", true)
                        && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download))
                 {

                }else if (rowsItem.items?.get(position)?.data?.secondaryCta.toString().equals(Constant.Download)
                    && rowsItem.items?.get(position)?.data?.isFollow == false
                ) {

                    if (CommonUtils.isUserHasGoldSubscription()) {
                        rowsItem.items?.get(position)?.data?.isFollow = true
                        ivAction.setImageDrawable(
                            ctx.faDrawable(
                                R.string.icon_downloading,
                                R.color.colorWhite
                            )
                        )
                    }

                } else if (rowsItem.items?.get(position)?.data?.secondaryCta.toString().equals(Constant.Download)
                    && rowsItem.items?.get(position)?.data?.isFollow == true) {
                    ivAction.setImageDrawable(
                        ctx.faDrawable(
                            R.string.icon_downloaded2,
                            R.color.colorWhite
                        )
                    )

                } else if (rowsItem.items?.get(position)?.data?.isFollow == false) {
                    setActionImage(secondaryCta, ivAction, rowsItem.items?.get(position)?.data)
                } else {
                    setActionImageAfterClick(
                        secondaryCta,
                        ivAction,
                        rowsItem.items?.get(position)?.data
                    )
                }
            }
        }

        addRemovePlayer(simpleExoplayerr)
        addRemoveMuteIcon(ivMuteUnmute)
        collection.addView(layout)
/*        if(BucketParentAdapter.isVisible) {
            mixpanelEventView(rowsItem.items?.get(position)?.data, position)
        }*/
        return layout
    }


    fun donwloadIconChanges(data: BodyDataItem?, ivAction: FontAwesomeImageView) {

        val downloadedAudio =
            AppDatabase.getInstance()?.downloadedAudio()?.findByContentId(data?.id!!)
        val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByContentId(data?.id!!)

        if (downloadQueue != null) {

            downloadIconStates(downloadQueue.downloadStatus, ivAction, data)

        }

        if (downloadedAudio != null) {
            if (downloadedAudio.downloadStatus == Status.COMPLETED.value && !TextUtils.isEmpty(
                    downloadedAudio.downloadedFilePath
                )
            ) {
                try {
                    val fileName = downloadedAudio.downloadedFilePath
                    val file = File(fileName)
                    if (file.exists()) {
                        downloadIconStates(downloadedAudio.downloadStatus, ivAction, data)
                    } else {
                        AppDatabase.getInstance()?.downloadedAudio()
                            ?.deleteDownloadQueueItemByContentId(
                                downloadedAudio.contentId!!
                            )
                        downloadIconStates(0, ivAction, data)
                    }
                } catch (e: Exception) {

                } catch (fileNotFound: FileNotFoundException) {
                    downloadIconStates(0, ivAction, data)
                }


            } else if (downloadedAudio.downloadStatus == Status.COMPLETED.value) {
                downloadIconStates(4, ivAction, data)
            }
        }
    }

    fun onClickEvent(position: Int, ivAction: FontAwesomeImageView) {
        if (rowsItem.items?.get(position)?.data?.secondaryCta.toString()
                .equals(Constant.Download)
        ) {
            ivAction.setImageDrawable(ctx.faDrawable(R.string.icon_download, R.color.colorWhite))
        } else if (rowsItem.items?.get(position)?.data?.secondaryCta.toString()
                .equals(Constant.Download)
            && rowsItem.items?.get(position)?.data?.isFollow == true
        ) {
            ivAction.setImageDrawable(ctx.faDrawable(R.string.icon_downloading, R.color.colorWhite))
        } else if (rowsItem.items?.get(position)?.data?.isFollow == false) {
            setActionImage(
                rowsItem.items?.get(position)?.data?.secondaryCta.toString(),
                ivAction,
                rowsItem.items?.get(position)?.data
            )  // icon without click
        } else {
            setActionImageAfterClick(
                rowsItem.items?.get(position)?.data?.secondaryCta.toString(),
                ivAction,
                rowsItem.items?.get(position)?.data
            ) // icon with click
        }
    }

    private fun downloadIconStates(status: Int, ivDownload: ImageView, data: BodyDataItem?) {
        when (status) {
            Status.NONE.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_download)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }

            Status.QUEUED.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_download_queue)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }

            Status.DOWNLOADING.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_downloading)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
            }

            Status.COMPLETED.value -> {
                val drawable = FontDrawable(ctx, R.string.icon_downloaded2)
                drawable.setTextColor(ContextCompat.getColor(ctx, R.color.colorWhite))
                ivDownload.setImageDrawable(drawable)
                data?.isFollow = true
            }
        }
    }


    fun setActionImage(value: String, ivAction: FontAwesomeImageView, data: BodyDataItem? = null) {
        donwloadIconChanges(data, ivAction)
        when (value) {

            Constant.Favorited -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_like,
                    R.color.colorWhite
                )
            )

            Constant.Follow_Artist -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_follow,
                    R.color.colorWhite
                )
            )

            Constant.Follow -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_follow,
                    R.color.colorWhite
                )
            )

            Constant.Watchlist -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_watchlist,
                    R.color.colorWhite
                )
            )

            Constant.Share -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_share,
                    R.color.colorWhite
                )
            )

            Constant.View_Plans -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_watchlist,
                    R.color.colorWhite
                )
            )
        }

    }

    fun setActionImageAfterClick(
        value: String,
        ivAction: FontAwesomeImageView,
        data: BodyDataItem? = null
    ) {
        donwloadIconChanges(data, ivAction)

        when (value) {
            Constant.Favorited -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_liked,
                    R.color.colorWhite
                )
            )

            Constant.Follow_Artist -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_tick,
                    R.color.colorWhite
                )
            )

            Constant.Follow -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_tick,
                    R.color.colorWhite
                )
            )

            Constant.Watchlist -> ivAction.setImageDrawable(
                ctx.faDrawable(
                    R.string.icon_tick,
                    R.color.colorWhite
                )
            )

        }
    }


    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
        isVisible = false
    }

    override fun getCount(): Int {
        return pageCount
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence {
        return position.toString()
    }

    init {
        pageCount = rowsItem.items!!.size
        this.onChildItemClick = onChildItemClick
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }


    private suspend fun checkAllContentDWOrNot(
        icon: FontAwesomeImageView,
        playlistSongList: ArrayList<PlaylistModel.Data.Body.Row>?,data: BodyDataItem?): Boolean {
        var contentTypes = ContentTypes.AUDIO.value
        try {
            if(data?.contentTypeId.equals("110") || data?.contentTypeId.equals("109")){
                contentTypes = ContentTypes.PODCAST.value
            }else if(data?.type?.contains("video") == true){
                contentTypes = ContentTypes.VIDEO.value
            }else if (data?.contentTypeId.equals("22") || data?.contentTypeId.equals("55555")){
                contentTypes = ContentTypes.AUDIO.value
            }

            var isAllDownloaded = false
            var isAllDW = true
            var isAllDownloadInQueue = false
            var isCurrentContentPlayingFromThis = false

            if(data?.contentTypeId.equals("109")){
                  if (!playlistSongList.isNullOrEmpty()) {
                      try {
                          playlistSongList.get(0).data.misc.tracks.forEachIndexed { index, it ->
                              if (it != null) {
                                  if (!isAllDownloadInQueue) {
                                      val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                          ?.getDownloadQueueItemsByContentIdAndContentType(it.data.id,
                                              contentTypes
                                          )

                                      if (downloadQueue?.contentId.equals(it.data.id)) {
                                          isAllDownloadInQueue = downloadQueue?.downloadAll == 1
                                      }
                                  }

                                  if (isAllDW) {
                                      val downloadedAudio =
                                          AppDatabase.getInstance()?.downloadedAudio()
                                              ?.getDownloadedAudioItemsByContentIdAndContentType(
                                                  it.data.id, contentTypes
                                              )
                                      if (downloadedAudio != null && downloadedAudio.contentId.equals(
                                              it.data.id
                                          )
                                      ) {
                                          isAllDownloaded = true
                                      } else {
                                          isAllDW = false
                                          isAllDownloaded = false
                                      }
                                  }

                              }

                          }
                      } catch (e: Exception) {

                      }
                  }

            }else if (!playlistSongList.isNullOrEmpty()) {
                try {
                    playlistSongList.forEachIndexed { index, it ->
                        if (it != null) {
                            if (!isAllDownloadInQueue) {
                                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                    ?.getDownloadQueueItemsByContentIdAndContentType(it.data.id,contentTypes)

                                if (downloadQueue?.contentId.equals(it.data.id)) {
                                    isAllDownloadInQueue = downloadQueue?.downloadAll == 1
                                }
                            }

                            if (isAllDW) {
                                val downloadedAudio =
                                    AppDatabase.getInstance()?.downloadedAudio()
                                        ?.getDownloadedAudioItemsByContentIdAndContentType(
                                            it.data.id,contentTypes
                                        )
                                if (downloadedAudio != null && downloadedAudio.contentId.equals(
                                        it.data.id
                                    )
                                ) {
                                    isAllDownloaded = true
                                } else {
                                    isAllDW = false
                                    isAllDownloaded = false
                                }
                            }

                        }

                    }
                } catch (e: Exception) {

                }
            }

            setIsAllDownloadImage(icon, isAllDownloadInQueue, isAllDownloaded, data)
            return isCurrentContentPlayingFromThis
        } catch (e: Exception) {

        }
        return false
    }

    suspend fun setIsAllDownloadImage(
        icon: FontAwesomeImageView,
        isAllDownloadInQueue: Boolean,
        isAllDownloaded: Boolean,
        data: BodyDataItem?
    ) {
        CoroutineScope(Dispatchers.IO).launch {

            if (isAllDownloadInQueue) {
             //       icon.setImageDrawable(ctx.faDrawable(R.string.icon_downloading, R.color.colorWhite))
                    data?.isDownloading = "2"



            } else {
                if (isAllDownloaded) {
                        data?.isDownloading = "4"

                    if(Constant.notify) {
                        val intent = Intent(Constant.HeroSection)
                        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent)

                         Constant.notify = false
                    }


                } else {
                    data?.isDownloading = "0"
                }
            }
        }
    }

    fun CheckDownloading(ivAction: FontAwesomeImageView, position: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            if (rowsItem.items?.get(position)?.data?.contentTypeId == "55555" && rowsItem.items?.get(
                    position
                )?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId == "96" && rowsItem.items?.get(
                    position
                )?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId == "1" && rowsItem.items?.get(
                    position
                )?.data?.secondaryCta.equals(Constant.Download)

                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "97",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)

                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "102",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "107",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
                || rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "109",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download)
            ) {

                if(rowsItem.items?.get(position)?.data?.contentTypeId.equals(
                    "109",
                    true
                ) && rowsItem.items?.get(position)?.data?.secondaryCta.equals(Constant.Download))
                {
                    delay(400)
                    checkAllContentDWOrNot(
                        ivAction,
                        rowsItem.items?.get(position)?.data?.playlistSongList,
                        rowsItem.items?.get(position)?.data
                    )
                }else {
                    checkAllContentDWOrNot(
                        ivAction,
                        rowsItem.items?.get(position)?.data?.playlistSongList,
                        rowsItem.items?.get(position)?.data
                    )
                }
            }
        }
    }

}