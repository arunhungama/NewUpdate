package com.hungama.music.ui.main.view.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSourceFactory
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hungama.music.HungamaMusicApp
import com.hungama.music.auto.DemoUtil
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.repositories.RecentlyPlayRepos
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.AddedToWatchlist
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.eventanalytic.eventreporter.RemovedFromWatchListEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketChildAdapter
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.TabAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.RecentlyPlayViewModel
import com.hungama.music.ui.main.viewmodel.TVShowViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.MODULE_WATCHLIST
import com.hungama.music.utils.Constant.TVSHOW_DETAIL_PAGE
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.customview.fontview.FontAwesomeImageView
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fr_tv_show_details_v1.*
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivDownloadFullList
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivDownloadFullListActionBar
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit


// Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [TvShowDetailsFragment] factory method to
 * create an instance of this fragment.
 */
class TvShowDetailsFragment(val varient: Int) : BaseFragment(),
    TabLayout.OnTabSelectedListener,
    ViewTreeObserver.OnScrollChangedListener, BaseFragment.OnUserContentOrderStatus,
    OnUserSubscriptionUpdate, BaseActivity.OnLocalBroadcastEventCallBack, TracksContract.View,
    BaseActivity.OnDownloadVideoQueueItemChanged, OnParentItemClickListener,
    BucketParentAdapter.OnMoreItemClick {
    var artImageUrl = ""
    var selectedContentId: String? = null
    var playerType: String? = null
    private var chartDetailBgArtImageDrawable: LayerDrawable? = null
    var requestTime = DateUtils.getCurrentDateTime()
    var muteIcons: ArrayList<FontAwesomeImageView?> = ArrayList()
    var continueWhereLeftModel: ContinueWhereLeftModel? = null
   // var isMute = true
    var currentVolume = 0.0f
    private var isScreenLandscape: Boolean? = false
    var screenHeightInPotratitMode = 0
    var data = PlaylistDynamicModel()

    var tabAdapter: TabAdapter? = null

    var tvShowViewModel: TVShowViewModel? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()

    var rowList: MutableList<RowsItem?>? = null
    var artworkProminentColor = 0
    var userViewModel: UserViewModel? = null
    var isAddToWatchlist = false
    var bookmarkDataModel: BookmarkDataModel? = null
    var contentOrderStatus = Constant.CONTENT_ORDER_STATUS_NA
    var seasonList = ArrayList<PlaylistModel.Data.Body.Row.Season>()
    var currentSeasonNo = 0
    var currentSeasonEpisodes = 0
    var subTitleFullText = ""
    private lateinit var tracksViewModel: TracksContract.Presenter
    var isDefaultSeasonSelected = false
    var defaultSeasonNumber = 0
    var isEpisode = false
    var episodeName = ""
    var episodeId = ""
    var isDeepLinkDirectPlay = false
    var isDirectPlay = 0
    var recentlyPlayViewModel: RecentlyPlayViewModel? = null
    private var recentlyPlayRepos: RecentlyPlayRepos?=null
    var live_remark: MutableLiveData<String> = MutableLiveData<String>()
    var recent_season="0"
    var recent_episode = 0
    var he = 0
    var simpleExoplayerr: ExoPlayer? = null
    var ivMuteUnmuteMain :ImageView? = null
    var tvShowDetailRespModel: PlaylistDynamicModel? = null
    var pass_remark = ""

    companion object {
        var tvShowDetailRespModel1: PlaylistDynamicModel? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
     //   if (varient == 2) {
            setLocalBroadcast()
            return inflater.inflate(R.layout.fr_tv_show_details_v1, container, false)
      /*  } else {
            return inflater.inflate(R.layout.fr_tv_show_details, container, false)

        }*/

    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            if (context != null) {
                if (activity != null && activity is MainActivity){
                    (activity as MainActivity).closePIPVideoPlayer()
                }
                BucketChildAdapter.originalLiveData?.value = Constant.MUTE
                if (arguments != null) {
                    if (arguments?.containsKey(Constant.EXTRA_IS_SEASON)!!) {
                        isDefaultSeasonSelected =
                            requireArguments().getBoolean(Constant.EXTRA_IS_SEASON)
                    }
                    if (arguments?.containsKey(Constant.isPlay)!!) {
                        isDirectPlay = requireArguments().getInt(Constant.isPlay)
                    }

                    if (arguments?.containsKey(Constant.EXTRA_IS_EPISODE)!!) {
                        isEpisode = requireArguments().getBoolean(Constant.EXTRA_IS_EPISODE)
                    }
                    if (arguments?.containsKey("playerType") == true) {
                        playerType = requireArguments().getString("playerType")
                    }

                    if (arguments?.containsKey(Constant.EXTRA_EPISODE_NAME)!!) {
                        episodeName =
                            requireArguments().getString(Constant.EXTRA_EPISODE_NAME).toString()
                    }

                    if (arguments?.containsKey(Constant.EXTRA_EPISODE_ID)!!) {
                        episodeId =
                            requireArguments().getString(Constant.EXTRA_EPISODE_ID).toString()
                    }
                }
                tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this@TvShowDetailsFragment)
                selectedContentId = requireArguments().getString("id").toString()

                applyButtonTheme(requireContext(), llPlayAllTvShow)
                applyButtonTheme(requireContext(), llPlayMovieActionBar)
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_back_right_arrow)

                Constant.screen_name ="TvShow Details"

                ivBack?.setOnClickListener { backPress() }
                llToolbar?.visibility = View.INVISIBLE
                ivMuteUnmuteMain = ivMuteUnmute

                scrollView?.viewTreeObserver?.addOnScrollChangedListener(this@TvShowDetailsFragment)

                recentlyPlayViewModel = ViewModelProvider(context as AppCompatActivity).get(RecentlyPlayViewModel::class.java)


                tabLayout?.visibility = View.VISIBLE
                tabView?.visibility = View.VISIBLE

                if (!TextUtils.isEmpty(selectedContentId)) {
                    getContentOrderStatus(this@TvShowDetailsFragment, selectedContentId!!)
                }

                shimmerLayout?.visibility = View.VISIBLE
                shimmerLayout?.startShimmer()
                //this call is only for general data
                setUpTVShowDetailListViewModel()
                setLocalBroadcast()
                threeDotMenu?.setOnClickListener(this@TvShowDetailsFragment)
                threeDotMenu2?.setOnClickListener(this@TvShowDetailsFragment)
                threeDotMenu?.hide()
                threeDotMenu2?.hide()
                llWatchlist?.setOnClickListener(this@TvShowDetailsFragment)
                llTrailer?.setOnClickListener(this@TvShowDetailsFragment)
                llDownload?.setOnClickListener(this@TvShowDetailsFragment)
                ivShare?.setOnClickListener(this@TvShowDetailsFragment)


                   live_remark.observe(requireActivity(), Observer {
                    if(!it.isNullOrEmpty()) {
                        SplitEpisodeSeason(it)
                        pass_remark = it
                        tvDetailBtnTitle.text =
                            "${getString(R.string.download_str_9)} ${
                                continueWhereLeftModel?.get(0)?.remark?.replace(".","•")
                            }"
                        ivDetailBtnIcon.hide()
                        ivDetailBtnIconActionBar.hide()

                        val layoutParams = llToolbar.layoutParams
                        layoutParams.width =
                            resources.getDimensionPixelSize(R.dimen.dimen_190)
                        llToolbar.layoutParams = layoutParams

                        tvDetailBtnTitleActionBar.text =
                            "${getString(R.string.download_str_9)} ${
                                continueWhereLeftModel?.get(0)?.remark?.replace(".","•")
                            }"
                    }
                })

                img_full_screen_enter_exit.setOnClickListener(this@TvShowDetailsFragment)


                CommonUtils.setPageBottomSpacing(
                    scrollView,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
            }
        }
    }

    var seek = 0L

    private val broadCastReceiverTV : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            val event = intent?.getStringExtra(Constant.MUSIC_VIDEO_STATUS)
        //    Toast.makeText(requireContext(), "received"+event, Toast.LENGTH_SHORT).show()
            setLog("aljga", "$event")
            var count = 0
            when(event){
                Constant.STOP -> {
                    simpleExoplayerr?.let {
                        seek = it.currentPosition
                        it.stop()
                        it.release()
                    }
                    simpleExoplayerr = null
                }
                Constant.PAUSE -> simpleExoplayerr?.pause()
                Constant.STOP_PLAY ->{
                    if(simpleExoplayerr == null) {
                        if (count == 0)
                            playerData?.let { playerInit(it, seek) }
                        count += 1
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            count = 0
                        }
                    }
                }
                Constant.PLAY -> simpleExoplayerr?.play()
                Constant.MUTE -> {
                    simpleExoplayerr?.volume = 0F
                    ivMuteUnmuteMain?.setImageDrawable(
                        requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                            requireContext()?.faDrawable(
                                R.string.icon_mute,
                                R.color.colorWhite,
                                it.toFloat()
                            )
                        }
                    )
                }
                Constant.Redirection -> {
                    var str_remark = intent?.getStringExtra(Constant.Redirection)
                    baseMainScope.launch {
                        //      live_remark.postValue(str_remark)
                        if (!str_remark.isNullOrEmpty()) {
                            pass_remark = str_remark
                            tvDetailBtnTitle.text =
                                "${getString(R.string.download_str_9)} $pass_remark"
                        }
                        ivDetailBtnIcon.hide()
                        ivDetailBtnIconActionBar.hide()

                        val layoutParams = llToolbar.layoutParams
                        layoutParams.width =
                            resources.getDimensionPixelSize(R.dimen.dimen_190)
                        llToolbar.layoutParams = layoutParams

                        tvDetailBtnTitleActionBar.text =
                            "${getString(R.string.download_str_9)} $pass_remark"
                    }
                }
                Constant.UNMUTE -> {
                    simpleExoplayerr?.volume = currentVolume
                    ivMuteUnmuteMain?.setImageDrawable(
                        requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                            requireContext()?.faDrawable(
                                R.string.icon_unmute,
                                R.color.colorWhite,
                                it.toFloat()
                            )
                        }
                    )
                }
                Constant.SEEK -> {
                   var lon = intent?.getLongExtra(Constant.SEEK, 0L)
                    lon?.let { simpleExoplayerr?.seekTo(it) }
                }
                Constant.Download -> {
                    if (isAdded && context != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (data.data.body.rows.isNotEmpty())
                                checkAllContentDWOrNot(data.data.body.rows.get(0))
                        }
                    }
                }
            }
        }
    }

    private fun SplitEpisodeSeason(str1:String) {
        val str = str1.replace(".","•")

        val splited: List<String> = str.split("S")
        val splited1: List<String> = splited.get(1).split("EP")

        recent_season = splited1.get(0).replace(" ","").replace("•","")
        defaultSeasonNumber = recent_season.toInt().minus(1)
        val seasonListData = data.data.body.rows.get(0).seasons
        val seasonSize = seasonListData.size
        val splited2: List<String> = str.split("•")
        val strS = splited2.get(0)

        if (seasonSize>1){
            for(season in seasonListData.indices){
                for(tracks in seasonListData[season].data.misc.tracks){
                    if (tracks.data.subTitle.contains(strS)){
                      defaultSeasonNumber = season
                    }
                }
            }
        }
        else
            defaultSeasonNumber = 0


        tabLayout?.getTabAt(defaultSeasonNumber)?.select()
        var temp = splited1.get(1).replace(" ","").replace("•","").trimStart('0')
        recent_episode = temp.toInt().minus(1)

    }

    private fun playAllTvShow(selectedContentId: String, position: Int) {
        baseMainScope.launch {
            try{
                if (isAdded && context != null && !seasonList.isEmpty() && seasonList.get(0).data.misc.tracks.size > position) {
                    /*val songsList = CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
            val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
            val serviceBundle = Bundle()
            serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
            serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
            serviceBundle.putString(Constant.SELECTED_CONTENT_ID,selectedContentId)
            serviceBundle.putInt(Constant.CONTENT_TYPE, Constant.CONTENT_TV_SHOW)
            intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
            intent.putExtra("thumbnailImg", artImageUrl)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)*/
                    val dpm = DownloadPlayCheckModel()
                    dpm.contentId = seasonList.get(0).data.misc.tracks.get(position).data.id
                    dpm.contentTitle =
                        seasonList.get(0).data.misc.tracks.get(position).data.name
                    dpm.planName =
                        seasonList.get(0).data.misc.tracks.get(position).data.misc.movierights.toString()
                    dpm.isAudio = false
                    dpm.isDownloadAction = false
                    dpm.isDirectPaymentAction = false
                    dpm.queryParam = ""
                    dpm.isShowSubscriptionPopup = true
                    dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                    dpm.restrictedDownload =
                        RestrictedDownload.valueOf(seasonList.get(0).data.misc.tracks.get(position).data.misc.restricted_download!!)



                    if (CommonUtils.userCanDownloadContent(
                            requireContext(),
                            null,
                            dpm,
                            this@TvShowDetailsFragment,Constant.drawer_svod_tvshow_episode,
                            CommonUtils.checkEnableFlag(CommonUtils.getFirebaseConfigAdsData().drawerSvodTvshowEpisode,dpm.planName)
                        )
                    ) {
                        BaseActivity.tvshowDetail =
                            seasonList.get(0).data.misc.tracks.get(position)
                        val songsList =
                            CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
                        val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                        val serviceBundle = Bundle()
                        serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
                        serviceBundle.putParcelableArrayList(Constant.SEASON_LIST, seasonList)
                        serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                        serviceBundle.putInt(Constant.recent_episode, recent_episode.toInt())
                        serviceBundle.putString(Constant.recent_season,pass_remark)
                        serviceBundle.putString(
                            Constant.SELECTED_CONTENT_ID,
                            seasonList.get(0).data.misc.tracks.get(recent_episode.toInt()).data.id
                        )
                        serviceBundle.putInt(Constant.CONTENT_TYPE, Constant.CONTENT_TV_SHOW)
                        serviceBundle.putInt(
                            Constant.TYPE_ID,
                            seasonList.get(0).data.misc.tracks.get(recent_episode.toInt()).data.type
                        )
                        intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                        intent.putExtra(
                            "thumbnailImg",
                            seasonList.get(0).data.misc.tracks.get(recent_episode.toInt()).data.image
                        )
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        serviceBundle.putLong(
                            Constant.VIDEO_START_POSITION, TimeUnit.SECONDS.toMillis(
                                HungamaMusicApp.getInstance().getContentDuration(
                                    seasonList.get(0).data.misc.tracks.get(recent_episode.toInt()).data.id
                                )!!
                            )
                        )
                        (requireActivity() as MainActivity).setLocalBroadcastEventCall(
                            this@TvShowDetailsFragment,
                            Constant.VIDEO_PLAYER_EVENT
                        )
                        if (activity != null) {
                            val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                            if (status == Constant.pause) {
                                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                            } else {
                                SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                            }
                            (activity as MainActivity).pausePlayer()
                        }
                        startActivity(intent)
                        /*startActivityForResult(
                            intent,
                            Constant.VIDEO_ACTIVITY_RESULT_CODE
                        )*/


                    }
                }
            }catch (e:Exception){}
        }
    }

    var playerData: PlaylistDynamicModel? =null

    fun playerInit(data: PlaylistDynamicModel, seek:Long = 0){
        playerData  = data
        val renderersFactory = DefaultRenderersFactory(requireContext())

        var mediaSource: MediaSourceFactory? = null
        val track = Track()
//                    track.id = list.get(position)?.orignalItems?.get(0)?.data?.id?.trim()?.toLong()!!
        track.title = data?.data?.head?.title
        track.url = data?.data?.head?.trailer_url

        mediaSource = DefaultMediaSourceFactory(DemoUtil.getDataSourceFactory(requireContext()))

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        val heightOfPager = ((62 * displayMetrics.heightPixels)/100).toFloat()

        val aspectRadio = heightOfPager/displayMetrics.widthPixels.toFloat()
        val width = aspectRadio.toFloat() * displayMetrics.widthPixels.toFloat()

            val height = (heightOfPager * 55)/100
            episode_player_view?.layoutParams?.height = height.toInt()
               vTopBottom.layoutParams.height = height.toInt()
            if (width > displayMetrics.widthPixels) {
                episode_player_view.layoutParams.width = width.toInt()
                vTopBottom.layoutParams.width = width.toInt()
            }

        val simpleExoplayer = ExoPlayer.Builder(requireContext(), renderersFactory)
            .setMediaSourceFactory(mediaSource)
            .setHandleAudioBecomingNoisy(true).build()

        /*screenHeightInPotratitMode = getPlayerHeight()
        if (episode_player_view != null) {
            val params: ViewGroup.LayoutParams = episode_player_view.getLayoutParams()
            params.height = screenHeightInPotratitMode
            episode_player_view?.requestLayout()
        }*/

        simpleExoplayer.pause()
        simpleExoplayer?.setMediaItem(CommonUtils.setMediaItem(track))
        simpleExoplayer?.prepare()
        episode_player_view.player = simpleExoplayer
        simpleExoplayer?.playWhenReady = true

        currentVolume = simpleExoplayer.volume
        Constant.isMute = if (!Constant.isMute) false else data?.data?.head?.playWithSound != true
        checkMiniVideoSound()

        if (Constant.isMute) {
            simpleExoplayer?.volume = 0.0f
            ivMuteUnmuteMain?.setImageDrawable(
                requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                    requireContext()?.faDrawable(
                        R.string.icon_mute,
                        R.color.colorWhite,
                        it.toFloat()
                    )
                }
            )
        } else {
            simpleExoplayer?.volume = currentVolume
            ivMuteUnmuteMain?.setImageDrawable(
                requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                    requireContext()?.faDrawable(
                        R.string.icon_unmute,
                        R.color.colorWhite,
                        it.toFloat()
                    )
                }
            )
        }
        simpleExoplayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        simpleExoplayer.seekTo(seek)

        simpleExoplayerr = simpleExoplayer

        simpleExoplayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
//                            super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY) {
                    val responseTime = DateUtils.getCurrentDateTime()
                    val diffInMillies: Long =
                        Math.abs(requestTime.getTime() - responseTime.getTime())

                    val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)

                    if (diff<3000)
                    {
                        simpleExoplayer.pause()
                        val newTime = 3000 - diff
                        CoroutineScope(Dispatchers.Main).launch {
                            if (seek<=0)
                            {
                                delay(newTime)
                            }
                            simpleExoplayer.play()
                        }

                    }
                    tvShowAlbumArtImageView?.hide()
                    llMain?.show()
                    img_full_screen_enter_exit?.show()

                    ivShare?.hide()

            } else if (playbackState == Player.STATE_ENDED) {
                   /* BaseFragment.isPlayBannerVPlaying = false

                   tvShowAlbumArtImageView?.show()
                   ivShare?.show()*/
                    simpleExoplayerr?.let {
                    it.playWhenReady}
                       //ending .......
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                tvShowAlbumArtImageView?.show()
                ivShare?.show()
                episode_player_view?.Invisiable()
                preViewProgressBar?.hide()
                vTopBottom?.hide()

            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    checkMiniVideoSound()
                    tvShowAlbumArtImageView?.hide()
                    ivShare?.hide()
                    episode_player_view?.show()
                    preViewProgressBar?.hide()
                    vTopBottom?.hide()
                    llMute?.show()
                    llMute?.setOnClickListener {
                        if (Constant.isMute) {
                            VideoPlayerActivity?.mPlayer?.pause()
                            simpleExoplayer?.volume = currentVolume
                            ivMuteUnmuteMain?.setImageDrawable(
                                requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)
                                    ?.let { it1 ->
                                        requireContext()?.faDrawable(
                                            R.string.icon_unmute,
                                            R.color.colorWhite,
                                            it1.toFloat()
                                        )
                                    }
                            )
                            if ((requireContext() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                (requireContext() as MainActivity).currentPlayer?.pause()
                            }
                        } else {
                            simpleExoplayer?.volume = 0.0f
                            ivMuteUnmuteMain?.setImageDrawable(
                                requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)
                                    ?.let { it1 ->
                                        requireContext()?.faDrawable(
                                            R.string.icon_mute,
                                            R.color.colorWhite,
                                            it1.toFloat()
                                        )
                                    }
                            )
                        }
                        setLog("lagnasf", "$currentVolume $Constant.isMute " +simpleExoplayer?.volume.toString())

                        Constant.isMute = !Constant.isMute
                    }
                } else {
                    tvShowAlbumArtImageView?.show()
                    ivShare?.show()
                    episode_player_view?.hide()
                    llMute?.hide()
                    preViewProgressBar?.hide()
                    llMute?.setOnClickListener(null)
                    BaseFragment.isPlayBannerVPlaying = false
                }
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //(activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
        return onCreateOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //(activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_download_video -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        /*if (isDownloaded){
            menu!!.findItem(R.id.action_download_video).title = resources.getString(R.string.downloaded)
        }else{
            menu!!.findItem(R.id.action_download_video).title = resources.getString(R.string.download)
        }*/

        return onPrepareOptionsMenu(menu)
    }

    fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }
    }

    fun setArtImageBg(status: Boolean) {
        try {
            baseIOScope.launch {
                if (activity != null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                        artImageUrl
                    ) && tvShowDetailroot != null
                ) {
                    val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
                    val gradient: Drawable? = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.audio_player_gradient_drawable
                    )
                    val result: Deferred<Bitmap?> = baseIOScope.async {
                        val urlImage = URL(artImageUrl)
                        urlImage.toBitmap()
                    }

                    baseIOScope.launch {
                        try {
                            // get the downloaded bitmap
                            val bitmap: Bitmap? = result.await()
                            val artImage = BitmapDrawable(resources, bitmap)
                            if (status) {
                                if (bitmap != null) {
                                    Palette.from(bitmap!!).generate { palette ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            artworkProminentColor =
                                                CommonUtils.calculateAverageColor(bitmap, 1)
                                            baseMainScope.launch {
                                                if (context != null) {
                                                    CommonUtils.setLog(
                                                        "ChartLifecycle",
                                                        "setArtImageBg--$artworkProminentColor"
                                                    )
                                                    changeStatusbarcolor(artworkProminentColor)
                                                }
                                            }

                                        }
                                        chartDetailBgArtImageDrawable =
                                            LayerDrawable(
                                                arrayOf<Drawable>(
                                                    bgColor,
                                                    artImage,
                                                    gradient!!
                                                )
                                            )
                                        tvShowDetailroot?.background = chartDetailBgArtImageDrawable
                                    }

                                }

                            }
                        } catch (exp: Exception) {
                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }


                    }
                }
            }

        } catch (exp: Exception) {
            exp.printStackTrace()
        }


    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            var last_pos = tabAdapter?.tabCount?.minus(1)
            if(position == last_pos){
                val params: ViewGroup.LayoutParams = viewPager.getLayoutParams()
                var h = resources.getDimensionPixelSize(R.dimen.dimen_57) * tvShowDetailRespModel?.data?.body?.rows?.get(0)?.info?.cast?.size?.toInt()!!
                params.height= h
                viewPager.layoutParams = params
                viewPager.top = resources.getDimensionPixelSize(R.dimen.dimen_200)
            }
            val size = tvShowDetailRespModel?.data?.body?.rows?.get(0)?.seasons?.size
            if(position==size){

            }else {
                setLog("onPageSelected", "Selected position:" + position)
                currentSeasonNo = position
                seasonList = ArrayList()
                seasonList.add(
                    tvShowDetailRespModel?.data?.body?.rows?.get(0)?.seasons?.get(
                        currentSeasonNo
                    )!!
                )
                if (!seasonList.isNullOrEmpty()) {
                    currentSeasonEpisodes = seasonList.get(0)?.data?.misc?.tracks?.size!!
                }

                val params: ViewGroup.LayoutParams = viewPager.getLayoutParams()
                var h = resources.getDimensionPixelSize(R.dimen.dimen_190) * seasonList.get(0).data.misc.tracks.size!!
                params.height= h
                viewPager.layoutParams = params
                viewPager.top = resources.getDimensionPixelSize(R.dimen.dimen_200)

                val text =
                    subTitleFullText + " • " + (currentSeasonNo + 1) + " " + getString(R.string.tvshow_str_11) + " • " + currentSeasonEpisodes + " " + getString(
                        R.string.podcast_str_9
                    )
                tvSubTitle2.text = text

                CommonUtils.setLog(
                    "deepLinkUrl",
                    "TVSHOWFragment-setDetails--isEpisode=$isEpisode && episodeId=$episodeId"
                )
                if (isEpisode && !TextUtils.isEmpty(episodeId) && !isDeepLinkDirectPlay) {
                    if (tvShowDetailRespModel?.data?.body?.rows?.get(0) != null && !tvShowDetailRespModel?.data?.body?.rows?.get(
                            0
                        )?.seasons.isNullOrEmpty() && tvShowDetailRespModel?.data?.body?.rows?.get(0)?.seasons?.size!! > 0
                    ) {
                        tvShowDetailRespModel?.data?.body?.rows?.get(0)?.seasons?.get(
                            0
                        )?.data?.misc?.tracks?.forEachIndexed { index, season ->
                            CommonUtils.setLog(
                                "deepLinkUrl",
                                "TVSHOWFragment-setDetails--episodeId2=${season?.data?.id}"
                            )
                            if (episodeId.equals(season?.data?.id.toString())) {
                                CommonUtils.setLog(
                                    "deepLinkUrl",
                                    "TVSHOWFragment-setDetails--episodeId1=$episodeId && episodeId2=${season?.data?.id}"
                                )
                                playAllTvShow(episodeId, index)
                                isDeepLinkDirectPlay = true
                            }
                        }
                    }

                }
            }

        }
    }


    private fun setUpTVShowDetailListViewModel() {
        try {
            if (isAdded && context != null) {
                tvShowViewModel = ViewModelProvider(
                    this
                ).get(TVShowViewModel::class.java)


                if (ConnectionUtil(context).isOnline) {
                    tvShowViewModel?.getTVShowDetailList(
                        requireContext(),selectedContentId.toString(),
                        playerType
                    )?.observe(this,
                        Observer {
                            when (it.status) {
                                Status.SUCCESS -> {
                                    fillTVShowDetail(it?.data!!)
                                    data = it?.data
                                    if(data?.data?.head?.trailer_url.isNullOrBlank()){
                                        tv_trailer.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_scale));
                                        DrawableCompat.setTint(DrawableCompat.wrap(ivTrailer.getDrawable()),
                                            ContextCompat.getColor(requireContext(), R.color.gray_scale))
                                    }else{
                                        tv_trailer.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite));
                                        DrawableCompat.setTint(DrawableCompat.wrap(ivTrailer.getDrawable()),
                                            ContextCompat.getColor(requireContext(), R.color.colorWhite))
                                    }

                                    CoroutineScope(Dispatchers.Main).launch {

                                        if (isAdded)
                                        {
                                            requestTime = DateUtils.getCurrentDateTime()

                                            if(!data?.data?.head?.trailer_url.isNullOrBlank()) {
                                                playerInit(it?.data)
                                            }else{
                                                if(simpleExoplayerr!=null){
                                                    simpleExoplayerr?.release()
                                                }
                                            }
                                        }
                                    }

                                }
                                Status.LOADING -> {
                                    setProgressBarVisible(false)
                                }

                                Status.ERROR -> {
                                    setEmptyVisible(false)
                                    setProgressBarVisible(false)
                                    Utils.showSnakbar(
                                        requireContext(),
                                        requireView(),
                                        true,
                                        it.message!!
                                    )
                                }
                            }
                        })
                } else {
                    val messageModel = MessageModel(
                        getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                        MessageType.NEGATIVE, true
                    )
                    CommonUtils.showToast(requireContext(), messageModel,"TvShowDetailsFragment","setUpTVShowDetailListViewModel")
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun fillTVShowDetail(model: PlaylistDynamicModel) {
        setProgressBarVisible(false)
        if (model != null) {
            //setLog(TAG, "isViewLoading $it")
            tvShowDetailroot.visibility = View.VISIBLE
            setDetails(model)

            CoroutineScope(Dispatchers.IO).launch {
                checkAllContentDWOrNot(model.data.body.rows.get(0))
            }

            if (CommonUtils.isUserHasGoldSubscription())
            showlastplayepisode(model.data.body.rows.get(0))

        }
    }


    private fun setDetails(it: PlaylistDynamicModel) {
        baseMainScope.launch {
            if (isAdded && context != null) {
                if (it?.data?.head != null) {
                    tvShowDetailRespModel = it
                    tvShowDetailRespModel1= it

                    artImageUrl = "" + tvShowDetailRespModel?.data?.head?.tv_show_image
                    playerType = "" + tvShowDetailRespModel?.data?.head?.type

                    if (!TextUtils.isEmpty(artImageUrl)) {
                        setArtImageBg(true)
                       tvShowAlbumArtImageView?.visibility = View.VISIBLE
                        ImageLoader.loadImage(
                            requireContext(),
                            tvShowAlbumArtImageView,
                            artImageUrl,
                            R.drawable.bg_gradient_placeholder
                        )
                    } else {
                        tvShowAlbumArtImageView?.visibility = View.GONE
                    }

                    setupUserViewModel()
                    llButton?.visibility = View.VISIBLE
                    llRating?.visibility = View.VISIBLE
                    if (it?.data?.head!!.title != null && !TextUtils.isEmpty(it?.data?.head!!.title)) {
                        tvTitle?.text = it?.data?.head?.title
                        setLog(TAG, "setDetails: it?.data?.head?.title ${it?.data?.head?.title}")
                        tvTitle?.isSelected = true
                    } else {
                        //tvTitle.text = ""
                        tvTitle?.text = ""
                    }

                    var subTitleText = ""
                    if (it?.data?.head?.releasedate != null) {
                        var year = DateUtils.convertDate(
                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                            DateUtils.DATE_YYYY,
                            "" + it?.data?.head?.releasedate
                        )
                        subTitleText += year
                    }

                    if (it?.data?.head?.genre != null && it?.data?.head?.genre?.size!! > 0) {
                        val genre = TextUtils.join("/", it?.data?.head?.genre!!)
                        subTitleText += " • " + genre
                    }

                    if (it?.data?.head?.duration != null && !TextUtils.isEmpty(it?.data?.head?.duration)) {
                        val totalSecs = it?.data?.head?.duration?.toLong()
                        if (totalSecs != null) {
                            if (totalSecs.toLong() >= 0) {
                                val formatTime =
                                    DateUtils.convertTimeHM(TimeUnit.MINUTES.toMillis(totalSecs))
                                //    subTitleText += " • " + formatTime
                    //                val hours = totalSecs!! / 3600;
                    //                val minutes = (totalSecs!! % 3600) / 60;
                            } else {
                                //subTitleText += ""
                                //        subTitleText += ""
                            }
                        }
                    } else {
                        //subTitleText += ""
                   //     subTitleText += ""
                    }
                    subTitleFullText = subTitleText
                    if (!setContentActionButton(contentOrderStatus)) {
                        if (it?.data?.head?.movierights != null && it?.data?.head?.movierights?.size!! > 0) {
                            val movieRights = Utils.setMovieRightTextForDetail(
                                tvDetailBtnTitle,
                                it?.data?.head?.movierights!!,
                                requireContext(),
                                it?.data?.head?.id.toString()
                            )
                            Utils.setMovieRightTextForDetail(
                                tvDetailBtnTitleActionBar,
                                it?.data?.head?.movierights!!,
                                requireContext(),
                                it?.data?.head?.id.toString()
                            )
                            contentStatusProgress?.visibility = View.GONE
                            tvDetailBtnTitleActionBar?.visibility = View.VISIBLE
                            tvDetailBtnTitle?.visibility = View.VISIBLE
                            llDetails2?.visibility = View.VISIBLE
                            llPlayMovieActionBar?.visibility = View.VISIBLE
                            if (movieRights == 3) {
                                ivDetailBtnIcon?.visibility = View.VISIBLE
                                ivDetailBtnIconActionBar?.visibility = View.VISIBLE
                                ivDetailBtnIcon?.setImageDrawable(
                                    requireContext().faDrawable(
                                        R.string.icon_play_2,
                                        R.color.colorWhite
                                    )
                                )
                                ivDetailBtnIconActionBar?.setImageDrawable(
                                    requireContext().faDrawable(
                                        R.string.icon_play_2,
                                        R.color.colorWhite
                                    )
                                )
                            } else if (movieRights == 1) {
                                ivDetailBtnIcon?.visibility = View.VISIBLE
                                ivDetailBtnIconActionBar?.visibility = View.VISIBLE
                                ivDetailBtnIcon?.setImageDrawable(
                                    requireContext().faDrawable(
                                        R.string.icon_rent,
                                        R.color.colorWhite,
                                        resources.getDimensionPixelSize(R.dimen.dimen_20).toFloat()
                                    )
                                )
                                ivDetailBtnIconActionBar?.setImageDrawable(
                                    requireContext().faDrawable(
                                        R.string.icon_rent,
                                        R.color.colorWhite,
                                        resources.getDimensionPixelSize(R.dimen.dimen_20).toFloat()
                                    )
                                )
                            } else {
                                ivDetailBtnIcon?.visibility = View.VISIBLE
                                ivDetailBtnIconActionBar?.visibility = View.VISIBLE
                                ivDetailBtnIcon?.setImageDrawable(
                                    requireContext().faDrawable(
                                        R.string.icon_crown,
                                        R.color.colorWhite
                                    )
                                )
                                ivDetailBtnIconActionBar?.setImageDrawable(
                                    requireContext().faDrawable(
                                        R.string.icon_crown,
                                        R.color.colorWhite
                                    )
                                )
                            }
                        } else {
                            llDetails2?.visibility = View.INVISIBLE
                            llPlayMovieActionBar?.visibility = View.INVISIBLE
                        }
                    }

                    if (!TextUtils.isEmpty(subTitleText)) {
                        tvSubTitle2?.text = subTitleText
                        tvSubTitle2?.visibility = View.VISIBLE
                    } else {
                        //tvSubTitle2.text = ""
                        tvSubTitle2?.visibility = View.GONE
                    }


                        if (it?.data?.head?.ratingCritic != null && !TextUtils.isEmpty(it?.data?.head?.ratingCritic)) {
                            tvRatingCritic?.text = it?.data?.head?.ratingCritic
                        } else {
                            tvRatingCritic?.text = "0"
                        }

                        var isRatingVisible = true
                        var isCensorRatingVisible = true
                        if (!TextUtils.isEmpty("" + it?.data?.head?.ratingCritic) && !it?.data?.head?.ratingCritic.toString()
                                .equals("0", true) && !it?.data?.head?.ratingCritic.toString()
                                .equals("0.0", true) && !it?.data?.head?.ratingCritic.toString()
                                .equals("NA", true)
                        ) {
                            tvRatingCritic.text = "" + it?.data?.head?.ratingCritic
                            rlRating?.show()
                            isRatingVisible = true
                        } else {
                            rlRating?.hide()
                            isRatingVisible = false
                        }

                        if (!it?.data?.head?.attributeCensorRating.isNullOrEmpty()) {
                            tvUA?.text = it.data.head.attributeCensorRating.get(0)
                            rlUA?.show()
                            isCensorRatingVisible = true
                        } else {
                            rlUA?.hide()
                            isCensorRatingVisible = false
                        }

                        if (!isRatingVisible && !isCensorRatingVisible) {
                            llRating?.hide()
                        } else {
                            llRating?.show()
                        }


                    if (it?.data?.head?.nudity != null && !TextUtils.isEmpty("" + it?.data?.head?.nudity) && it?.data?.head?.nudity.equals(
                            "1"
                        )
                    ) {
                        rlNudity?.visibility = View.VISIBLE
                    } else {
                        rlNudity?.visibility = View.GONE
                    }

                    if (it?.data?.head?.lang != null && it?.data?.head?.lang?.size!! > 0) {
                        val lang = TextUtils.join(",", it?.data?.head?.lang!!)
                        tvLanguage?.text = "Languages: " + lang
                        tvLanguage?.visibility = View.VISIBLE
                    } else {
                        //tvLanguage.text = "Languages: NA"
                        tvLanguage?.visibility = View.GONE
                    }

                    if (!it?.data?.head?.content_advisory?.isNullOrBlank()!!) {
                        tvContentAdvisory.text = "Content Advisory: ${it?.data?.head?.content_advisory}"
                        tvContentAdvisory?.show()
                    } else {
                        tvContentAdvisory?.hide()
                    }
                    if (!TextUtils.isEmpty(it?.data?.head?.description)) {
                        tvDescription?.text = it?.data?.head?.description
                        SaveState.isCollapse = true
                        tvDescription?.setShowingLine(2)
                        tvDescription?.addShowMoreText("read more")
                        tvDescription?.addShowLessText("read less")
                        tvDescription?.setShowMoreColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorWhite
                            )
                        )
                        tvDescription?.setShowLessTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorWhite
                            )
                        )
                        tvDescription?.setShowMoreStyle(Typeface.BOLD)
                        tvDescription?.setShowLessStyle(Typeface.BOLD)
                        //CommonUtils.makeTextViewResizable(tvDescription, 3, "read more", true)
                        tvDescription?.visibility = View.VISIBLE
                    } else {
                        tvDescription?.visibility = View.GONE
                    }
                }

                if (it?.data?.body != null) {
                    if (!it.data.body.rows.isNullOrEmpty()) {

                        setTabData(it.data.body.rows.get(0))



                    }

                    setRecommandedBuckets(it)
                }
                if(requireArguments().getBoolean(Constant.isPlayFromBanner)) {
                    seasonList = ArrayList()
                    if (tvShowDetailRespModel != null) {
                        seasonList.add(
                            tvShowDetailRespModel?.data?.body?.rows?.get(0)?.seasons?.get(
                                currentSeasonNo
                            )!!
                        )
                        callPlayTVShow()
                    }
                }
                shimmerLayout?.stopShimmer()
                shimmerLayout?.visibility = View.GONE
            }
        }
    }

    private fun setRecommandedBuckets(tvshowModel: PlaylistDynamicModel) {
        baseMainScope.launch {
            if (isAdded && context != null) {
                if (tvshowModel.data?.body?.recomendation != null && !tvshowModel.data?.body?.recomendation.isNullOrEmpty()) {
                    rvRecomendation?.visibility = View.VISIBLE

                    val varient = Constant.ORIENTATION_HORIZONTAL

                    val bucketParentAdapter = BucketParentAdapter(
                        tvshowModel.data?.body?.recomendation!!,
                        requireContext(),
                        this@TvShowDetailsFragment,
                        this@TvShowDetailsFragment,
                        Constant.WATCH_TAB,
                        HeadItemsItem(),
                        varient
                    )

                    val mLayoutManager = LinearLayoutManager(
                        activity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    rvRecomendation?.layoutManager = mLayoutManager
                    rvRecomendation?.adapter = bucketParentAdapter


                    bucketParentAdapter?.addData(tvshowModel.data?.body?.recomendation!!)
                    shimmerLayout?.stopShimmer()
                    shimmerLayout?.visibility = View.GONE
                    rvRecomendation?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val firstVisiable: Int =
                                mLayoutManager?.findFirstVisibleItemPosition()!!
                            val lastVisiable: Int =
                                mLayoutManager?.findLastCompletelyVisibleItemPosition()!!

                            setLog(
                                TAG,
                                "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                            )
                            if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                                val fromBucket =
                                    tvshowModel.data?.body?.recomendation?.get(firstVisiable)?.heading
                                val toBucket =
                                    tvshowModel.data?.body?.recomendation?.get(lastVisiable)?.heading
                                val sourcePage =
                                    MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                                if (!fromBucket?.equals(toBucket, true)!!) {
                                    callPageScrolledEvent(
                                        sourcePage,
                                        "" + lastVisiable,
                                        fromBucket,
                                        toBucket!!
                                    )
                                }

                            }
                        }
                    })
                    rvRecomendation?.setPadding(0, 0, 0, 0)
                }
            }
        }
    }

    private fun setTabData(seasonsModel: PlaylistModel.Data.Body.Row?) {
        if (seasonsModel != null && seasonsModel.seasons != null && seasonsModel.seasons?.size!! > 0) {

            for (i in 0 until tabLayout.tabCount) {
                val tab = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(10, 0, 10, 0)
                tab.requestLayout()
            }

            seasonsModel?.seasons?.forEachIndexed { index, season ->
                fragmentList.add(
                    TVShowFragment.addfrag(
                        index,
                        seasonsModel?.seasons,
                        seasonsModel?.seasons?.get(index),
                        tvShowDetailRespModel
                    )
                )
                val pos = index + 1
                //fragmentName.add(getString(R.string.tvshow_str_11) + " " + pos)
               if (index < seasonsModel.seasons.size && index < seasonsModel.seasons.get(index).data.misc.tracks.size)
                fragmentName.add(getString(R.string.tvshow_str_11) + " " + seasonsModel.seasons.get(index).data.misc.tracks.get(index).data.season)
                tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tvshow_str_11) + " " + pos))

               /* tabLayout.addTab(
                    tabLayout.newTab().setText(seasonsModel?.seasons?.get(index).data.misc.tracks.get(index).data.season)
                )*/
                tabLayout.addOnTabSelectedListener(this)
                //onTabSelected(tabLayout.getTabAt(defaultSeasonNumber))
            }



            tabLayout?.getTabAt(defaultSeasonNumber)?.select()
            viewPagerSetUp(seasonsModel)
            tabView.visibility = View.VISIBLE
        } else {
            tabView.visibility = View.GONE
        }
    }

    private fun viewPagerSetUp(items: PlaylistModel.Data.Body.Row?) {
        baseMainScope.launch {

            if (isAdded && context != null && !fragmentName.isEmpty()) {
                /*for (k in 1..5) {
            tabLayout.addTab(tabLayout.newTab().setText("Season " + k))
        }*/
               fragmentList.add(TVCastingShowFragment.addfrag(
                    fragmentList.size,
                      items?.info,
                    tvShowDetailRespModel
                ))

                fragmentName.add("Info")
                tabLayout.addTab(tabLayout.newTab().setText("Info"))


                tabAdapter = TabAdapter(requireActivity(), tabLayout.tabCount, fragmentList, fragmentName)
                viewPager.adapter = tabAdapter
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    if (fragmentName.size>position)
                    tab.text = fragmentName.get(position)
                }.attach()
                viewPager.offscreenPageLimit = 1

                for (i in 0 until tabLayout.tabCount) {
                    val tab = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                    val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                    p.setMargins(10, 0, 10, 0)
                    tab.requestLayout()
                }
//        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
                viewPager.registerOnPageChangeCallback(pageChangeCallback)
                viewPager?.setCurrentItem(defaultSeasonNumber, false)
                viewPager.isUserInputEnabled = false
                //viewPager.addOnPageChangeListener(this)
                CommonUtils.setLog(
                    "deepLinkUrl",
                    "TVSHOWFragment-setDetails--isDirectPlay=$isDirectPlay contentOrderStatus:${contentOrderStatus}"
                )
                if (isDirectPlay == 1) {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(2000)
                        callPlayTVShow()
                    }
                }

            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_bold
        )
        tab?.let {
            setStyleForTab(it, Typeface.BOLD, typeface)
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_medium
        )
        tab?.let {
            setStyleForTab(it, Typeface.NORMAL, typeface)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_bold
        )
        tab?.let {
            setStyleForTab(it, Typeface.BOLD, typeface)
        }
    }

    fun setStyleForTab(tab: TabLayout.Tab, style: Int, typeface: Typeface?) {
        tab.view.children.find { it is TextView }?.let { tv ->
            (tv as TextView).post {
                tv.setTypeface(typeface, style)
            }
        }
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = 0
            if (varient == 1) {
                maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_392)
            } else {
                maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_392)
            }
            /* how much we have scrolled */
            val movement = scrollView.scrollY

            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63)
            if (movement >= maxDistance) {
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())

               simpleExoplayerr?.pause()

                headBlur.visibility = View.INVISIBLE
                llToolbar.visibility = View.VISIBLE
                if (artworkProminentColor == 0) {
                    rlHeading.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                } else {
                    rlHeading.setBackgroundColor(artworkProminentColor)
                }
            } else {
                if(isAdded) {
                    if (simpleExoplayerr?.isPlaying == false && isVisible)

                        simpleExoplayerr?.play()
                }

     /*           val volume = VideoPlayerActivity.mPlayer?.volume
                if (volume != null) {
                    if ( volume > 0F){
                        simpleExoplayerr?.volume = 0.0f
                        Constant.isMute = true
                        ivMuteUnmuteMain?.setImageDrawable(
                            requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)?.let {
                                requireContext()?.faDrawable(
                                    R.string.icon_mute,
                                    R.color.colorWhite,
                                    it.toFloat()
                                )
                          AZ  }
                        )
                    }
                }*/
                //setLog("OnNestedScroll-m--", movement.toString())
                //setLog("OnNestedScroll-d--", maxDistance.toString())
                llToolbar.visibility = View.INVISIBLE
                headBlur.visibility = View.INVISIBLE
                rlHeading.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.transparent
                    )
                )
            }
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v?.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2) {
            commonThreeDotMenuItemSetup(TVSHOW_DETAIL_PAGE)
        } else if (id == llWatchlist.id) {
            if (tvShowDetailRespModel != null && tvShowDetailRespModel?.data != null
                && tvShowDetailRespModel?.data?.head != null
            ) {
                var attributeCensorRating = ""
                if (!tvShowDetailRespModel?.data?.head?.attributeCensorRating.isNullOrEmpty()) {
                    attributeCensorRating =
                        tvShowDetailRespModel?.data?.head?.attributeCensorRating?.get(0).toString()
                }
                if (!CommonUtils.checkUserCensorRating(requireContext(), attributeCensorRating)) {
                    tvShowDetailRespModel?.let { setAddOrRemoveFavourite(it) }
                }
            }
        } else if (id == llTrailer.id) {
            if(!tvShowDetailRespModel?.data?.head?.trailer_url.isNullOrBlank()) {
                simpleExoplayerr?.pause()
                playTrailer(tvShowDetailRespModel?.data?.head?.trailer_url.toString())
            }
        } else if (v == llPlayAllTvShow || v == llPlayMovieActionBar) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireContext(), v,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            } catch (e: Exception) {

            }
              callPlayTVShow()
        } else if (v == llDownload) {
            callDownloadTVShow()
        } else if (v == img_full_screen_enter_exit) {
            simpleExoplayerr?.pause()
            playTrailer(tvShowDetailRespModel?.data?.head?.trailer_url.toString())
        }else if (v == ivShare) {
            val shareurl = getString(R.string.music_player_str_18) + " " + tvShowDetailRespModel?.data?.head?.share
            Utils.shareItem(requireActivity(), shareurl)
        }
    }

    private fun callDownloadTVShow() {
        if (tvShowDetailRespModel != null && tvShowDetailRespModel?.data != null
            && tvShowDetailRespModel?.data?.head != null && !seasonList.isNullOrEmpty()
        ) {

            val dpm = DownloadPlayCheckModel()
            dpm.contentId = selectedContentId?.toString()!!
            dpm.contentTitle = tvShowDetailRespModel?.data?.head?.title.toString()
            dpm.planName = tvShowDetailRespModel?.data?.head?.movierights.toString()
            dpm.isAudio = false
            dpm.isDownloadAction = true
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_ALL_CONTENT
            dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
            var attributeCensorRating = ""
            if (!tvShowDetailRespModel?.data?.head?.attributeCensorRating.isNullOrEmpty()) {
                attributeCensorRating =
                    tvShowDetailRespModel?.data?.head?.attributeCensorRating?.get(0).toString()
            }

            if (CommonUtils.userCanDownloadContent(
                    requireContext(),
                    tvShowDetailroot,
                    dpm,
                    this,Constant.drawer_svod_download,
                    CommonUtils.getFirebaseConfigAdsData()?.drawerSvodDownload?.let { CommonUtils.checkEnableFlag(it,dpm.planName) }.toString())
            ) {
                if (!CommonUtils.checkUserCensorRating(
                        requireContext(),
                        attributeCensorRating
                    )
                ) {
                    val downloadQueueList: ArrayList<DownloadQueue> = ArrayList()
                    var dq = DownloadQueue()
                    for (item in seasonList.iterator()) {
                        if (!item?.data?.misc?.tracks.isNullOrEmpty()) {
                            for (episode in item?.data?.misc?.tracks?.iterator()!!) {
                                dq = DownloadQueue()
                                if (!TextUtils.isEmpty(episode?.data?.id.toString())) {
                                    dq.contentId = episode?.data?.id.toString()
                                }

                                if (!TextUtils.isEmpty(episode?.data?.name)) {
                                    dq.title = episode?.data?.name
                                }

                                if (!TextUtils.isEmpty(episode?.data?.subTitle)) {
                                    dq.subTitle = episode?.data?.subTitle
                                }

                                if (!TextUtils.isEmpty(episode?.data?.image!!)) {
                                    dq.image = episode?.data?.image!!
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.id!!)) {
                                    dq.parentId = tvShowDetailRespModel?.data?.head?.id!!
                                }
                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.title!!)) {
                                    dq.pName = tvShowDetailRespModel?.data?.head?.title
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.subTitle!!)) {
                                    dq.pSubName = tvShowDetailRespModel?.data?.head?.subTitle
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.releasedate!!)) {
                                    dq.pReleaseDate =
                                        tvShowDetailRespModel?.data?.head?.releasedate
                                }

                                if (!TextUtils.isEmpty(tvShowDetailRespModel?.data?.head?.image!!)) {
                                    dq.pImage = tvShowDetailRespModel?.data?.head?.image
                                }

                                if (!TextUtils.isEmpty(episode?.data?.misc?.movierights.toString()!!)) {
                                    dq.planName = episode?.data?.misc?.movierights.toString()
                                    dq.planType = CommonUtils.getContentPlanType(dq.planName)
                                }

                                dq.pType = DetailPages.TVSHOW_DETAIL_PAGE.value
                                dq.contentType = ContentTypes.TV_SHOWS.value
                                val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                                dq.source = eventModel.sourceName

                                val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                    ?.findByContentId(episode?.data?.id!!.toString())
                                val downloadedAudio =
                                    AppDatabase.getInstance()?.downloadedAudio()
                                        ?.findByContentId(episode?.data?.id!!.toString())
                                if ((!downloadQueue?.contentId.equals(episode?.data?.id!!.toString()))
                                    && (!downloadedAudio?.contentId.equals(episode?.data?.id!!.toString()))
                                ) {
                                    downloadQueueList.add(dq)
                                }
                            }
                        }
                    }

/*                    ivDownload.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_downloading,
                            R.color.colorWhite
                        )
                    )
                    tvDownload.text = "Downloading"*/
                    if (isAdded && context != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (data.data.body.rows.isNotEmpty())
                                checkAllContentDWOrNot(data.data.body.rows.get(0))
                        }
                    }
                    //if (downloadQueueList.size > 0){
                    (requireActivity() as MainActivity).addOrUpdateDownloadVideoQueue(
                        downloadQueueList,
                        this,
                        false,
                        true
                    )
                    //}
                }
            }
        }
    }

    private fun handleFullScreenEnterExit() {
        val display =
            (requireContext().getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val orientation = display.orientation
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
   //         centerGradient.visibility = View.VISIBLE
    //        llBottomView.visibility = View.VISIBLE
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
         //   val dpValue = ViewGroup.LayoutParams.MATCH_PARENT
            val dpValue = resources.getDimensionPixelSize(R.dimen.dimen_200)
            val heightOfPager = ((62 * displayMetrics.heightPixels)/100).toFloat()
            val aspectRadio = heightOfPager/displayMetrics.widthPixels.toFloat()
            val width = aspectRadio.toFloat() * displayMetrics.widthPixels.toFloat()

            val height = heightOfPager
            episode_player_view?.layoutParams?.height = height.toInt()
            vTopBottom.layoutParams.height = height.toInt()
            if (width > displayMetrics.widthPixels) {
                episode_player_view.layoutParams.width = width.toInt()
                vTopBottom.layoutParams.width = width.toInt()
            }

            /*val params2: ViewGroup.LayoutParams = videoPlayer.layoutParams
            params2.width = width
            params2.height = dpValue
            videoPlayer.requestLayout()*/
            isScreenLandscape = false
            //fullScreenCall()
            requireActivity().getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requireActivity().currentFocus?.requestLayout()
            show()
        } else {
       //     centerGradient.visibility = View.INVISIBLE
        //    llBottomView.visibility = View.GONE
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            //fullScreenCall()
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requireActivity().getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            requireActivity().currentFocus?.requestLayout()
            val params: ViewGroup.LayoutParams = episode_player_view.layoutParams

            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            episode_player_view.requestLayout()


            isScreenLandscape = true
            hide()
        }
    }

    private fun show() {


        llDetails.show()
        mainView.show()
        topView.show()
        rlHeading.show()
        mainBlack.show()
        vCenterGradient.show()
        vTopBottom.hide()
    //    rlExtraFeature.visibility = View.GONE
       // hideBrightnessAndVolume()
        // full_screen_enter_exit.visibility = View.VISIBLE
        img_full_screen_enter_exit.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_full_screen_video
            )
        )
        if (!isScreenLandscape!!){
            (activity as MainActivity).showBottomNavigationBar()
        }
    }

    private fun hide() {
        try {

            llDetails.hide()
            mainView.hide()
            topView.hide()
            rlHeading.hide()
            mainBlack.hide()
            vTopBottom.hide()
            vCenterGradient.hide()


            (activity as MainActivity).hideMiniPlayer()
            (activity as MainActivity).hideBottomNavigationBar()
         //   rlExtraFeature.visibility = View.VISIBLE
            //full_screen_enter_exit.visibility = View.GONE
            img_full_screen_enter_exit.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_minimize_video
                )
            )

        }catch (e:Exception){

        }

    }

    private fun callPlayTVShow() {
        if (isAdded && context != null && tvShowDetailRespModel != null && tvShowDetailRespModel?.data != null
            && tvShowDetailRespModel?.data?.head != null && !seasonList.isNullOrEmpty()
        ) {
            val dpm = DownloadPlayCheckModel()
            dpm.contentId = selectedContentId?.toString()!!
            dpm.contentTitle = tvShowDetailRespModel?.data?.head?.title.toString()
            dpm.planName = tvShowDetailRespModel?.data?.head?.movierights.toString()
            dpm.isAudio = false
            dpm.isDownloadAction = false
            dpm.isDirectPaymentAction = false
            dpm.queryParam = ""
            dpm.isShowSubscriptionPopup = true
            dpm.clickAction = ClickAction.FOR_ALL_CONTENT
            dpm.restrictedDownload = RestrictedDownload.NONE_DOWNLOAD_CONTENT
            var attributeCensorRating = ""
            if (!tvShowDetailRespModel?.data?.head?.attributeCensorRating.isNullOrEmpty()) {
                attributeCensorRating =
                    tvShowDetailRespModel?.data?.head?.attributeCensorRating?.get(0).toString()
            }

            if (isAdded && context != null && CommonUtils.userCanDownloadContent(
                    requireContext(),
                    tvShowDetailroot,
                    dpm,
                    this,Constant.drawer_svod_purchase,CommonUtils.checkEnableFlag(CommonUtils.getFirebaseConfigAdsData().drawerSvodPurchase,dpm.planName)
                )
            ) {
                if (isAdded && context != null && !CommonUtils.checkUserCensorRating(
                        requireContext(),
                        attributeCensorRating
                    )
                ) {
                    playAllTvShow("0",recent_episode.toInt())
                }
            }
        }
    }


    private fun setupUserViewModel() {
        try {
            if (isAdded && context != null) {
                userViewModel = ViewModelProvider(
                    this
                ).get(UserViewModel::class.java)


                getUserBookmarkedData()
            }
        } catch (e: Exception) {

        }
    }

    private fun setAddOrRemoveFavourite(tvShowDetailRespModel: PlaylistDynamicModel) {
        if (ConnectionUtil(context).isOnline) {

            isAddToWatchlist = !isAddToWatchlist
            val jsonObject = JSONObject()
            jsonObject.put("contentId", tvShowDetailRespModel?.data?.head?.id!!)
            jsonObject.put("typeId", tvShowDetailRespModel?.data?.head?.type!!)
            jsonObject.put("action", isAddToWatchlist)
            jsonObject.put("module", Constant.MODULE_WATCHLIST)
            userViewModel?.callBookmarkApi(requireContext(), jsonObject.toString())
            setFollowingStatus()

            if (isAddToWatchlist) {
                val messageModel = MessageModel(
                    getString(R.string.general_str_9), getString(R.string.general_str_10),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(requireContext(), messageModel,"setSongDetailViewModel","setSongDetailViewModel")
                baseIOScope.launch {
                    if (isAdded && context != null) {
                        val hashMap = HashMap<String, String>()
                        var newContentId=  tvShowDetailRespModel?.data?.head?.id!!
                        var contentIdData=newContentId.replace("playlist-","")
                        hashMap.put(
                            EventConstant.CONTENTID_EPROPERTY, contentIdData
                        )
                        hashMap.put(
                            EventConstant.CONTENTNAME_EPROPERTY,
                            tvShowDetailRespModel?.data?.head?.title!!
                        )
                        hashMap.put(
                            EventConstant.CONTENTTYPE_EPROPERTY,
                            "" + Utils.getContentTypeName("" + tvShowDetailRespModel?.data?.head?.type!!)
                        )
                        hashMap.put(
                            EventConstant.GENRE_EPROPERTY,
                             Utils.arrayToString(tvShowDetailRespModel?.data?.head?.genre!!)
                        )
                        hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.LANGUAGE_EPROPERTY,
                             Utils.arrayToString(tvShowDetailRespModel?.data?.head?.lang!!)
                        )
                        hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.YEAROFRELEASE_EPROPERTY,
                            "" + DateUtils.convertDate(
                                DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                                DateUtils.DATE_YYYY,
                                tvShowDetailRespModel?.data?.head?.releasedate
                            )
                        )
                        val eventModel: EventModel? = HungamaMusicApp?.getInstance()
                            ?.getEventData(tvShowDetailRespModel?.data?.head?.id!!)
                        if (eventModel != null && !TextUtils.isEmpty(eventModel?.bucketName)) {
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + eventModel?.bucketName)
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + eventModel?.bucketName
                            )
                        }
                        EventManager.getInstance().sendEvent(AddedToWatchlist(hashMap))
                    }
                }
            } else {
                val messageModel = MessageModel(
                    getString(R.string.toast_str_14),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(requireContext(), messageModel,"TvShowDetailsFragment","setAddOrRemoveFavourite")

                baseIOScope.launch {
                    if (isAdded && context != null) {
                        val hashMap = HashMap<String, String>()
                        var newContentId=tvShowDetailRespModel?.data?.head?.id!!
                        var contentIdData=newContentId.replace("playlist-","")
                        hashMap.put(
                            EventConstant.CONTENTID_EPROPERTY, contentIdData
                        )
                        hashMap.put(
                            EventConstant.CONTENTNAME_EPROPERTY,
                            tvShowDetailRespModel?.data?.head?.title!!
                        )
                        hashMap.put(
                            EventConstant.CONTENTTYPE_EPROPERTY,
                            "" + Utils.getContentTypeName("" + tvShowDetailRespModel?.data?.head?.type!!)
                        )
                        hashMap.put(
                            EventConstant.GENRE_EPROPERTY,
                             Utils.arrayToString(tvShowDetailRespModel?.data?.head?.genre!!)
                        )
                        hashMap.put(EventConstant.EPISODE_NUMBER_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.LANGUAGE_EPROPERTY,
                             Utils.arrayToString(tvShowDetailRespModel?.data?.head?.lang!!)
                        )
                        hashMap.put(EventConstant.SEASON_NUMBER_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.YEAROFRELEASE_EPROPERTY,
                            "" + DateUtils.convertDate(
                                DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                                DateUtils.DATE_YYYY,
                                tvShowDetailRespModel?.data?.head?.releasedate
                            )
                        )
                        val eventModel: EventModel? = HungamaMusicApp?.getInstance()
                            ?.getEventData(tvShowDetailRespModel?.data?.head?.id!!)
                        if (eventModel != null && !TextUtils.isEmpty(eventModel?.bucketName)) {
                            hashMap.put(EventConstant.SOURCE_EPROPERTY, "" + eventModel?.bucketName)
                            hashMap.put(
                                EventConstant.SOURCE_NAME_EPROPERTY,
                                "" + eventModel?.bucketName
                            )
                        }
                        EventManager.getInstance().sendEvent(RemovedFromWatchListEvent(hashMap))
                    }
                }
            }
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"TvShowDetailsFragment","setAddOrRemoveFavourite")
        }
    }

    private fun getUserBookmarkedData() {
        try {
            if (isAdded && context != null) {
                if (ConnectionUtil(requireContext()).isOnline) {
                    userViewModel?.getUserBookmarkedData(requireContext(), MODULE_WATCHLIST)
                        ?.observe(this,
                            Observer {
                                when (it.status) {
                                    com.hungama.music.data.webservice.utils.Status.SUCCESS -> {
                                        setProgressBarVisible(false)
                                        if (it?.data != null) {
                                            fillUI(it?.data)
                                        }

                                    }

                                    com.hungama.music.data.webservice.utils.Status.LOADING -> {
                                        setProgressBarVisible(true)
                                    }

                                    com.hungama.music.data.webservice.utils.Status.ERROR -> {
                                        setEmptyVisible(false)
                                        setProgressBarVisible(false)
                                        Utils.showSnakbar(
                                            requireContext(),
                                            requireView(),
                                            true,
                                            it.message!!
                                        )
                                    }
                                }
                            })
                } else {
                    val messageModel = MessageModel(
                        getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                        MessageType.NEGATIVE, true
                    )
                    CommonUtils.showToast(requireContext(), messageModel,"TvShowDetailsFragment","getUserBookmarkedData")
                }
            }
        } catch (e: Exception) {

        }
    }


    private fun fillUI(bookmarkDataModel: BookmarkDataModel) {
        this.bookmarkDataModel = bookmarkDataModel
        if (bookmarkDataModel != null && bookmarkDataModel?.data?.body?.rows != null && bookmarkDataModel?.data?.body?.rows?.size!! > 0) {
            for (bookmark in bookmarkDataModel?.data?.body?.rows?.iterator()!!) {
                if (tvShowDetailRespModel?.data?.head?.id?.equals(bookmark?.data?.id)!!) {
                    isAddToWatchlist = true
                }
            }
            setFollowingStatus()
        }
    }

    fun setFollowingStatus() {
        baseMainScope.launch {
            if (isAdded && context != null) {
                if (isAddToWatchlist) {
                    /*val drawable = FontDrawable(requireContext(), R.string.icon_tick)
                    drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    ivWatchlist.setImageDrawable(drawable)*/
                    ivWatchlist.setImageDrawable(
                        requireContext().faDrawable(R.string.icon_tick, R.color.colorWhite)
                    )
                } else {
                    /*val drawable = FontDrawable(requireContext(), R.string.icon_watchlist)
                    drawable.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    ivWatchlist.setImageDrawable(drawable)*/
                    ivWatchlist.setImageDrawable(
                        requireContext().faDrawable(R.string.icon_watchlist, R.color.colorWhite)
                    )
                }
            }
        }

    }

    override fun onDestroy() {
        baseMainScope.launch {
            if (context != null) {
                changeStatusbarcolor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.home_bg_color
                    )
                )
            }
        }
        simpleExoplayerr?.release()
        val display = (requireContext().getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val orientation = display.orientation
        if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            requireActivity().getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requireActivity().currentFocus?.requestLayout()
        }
        removeContentOrderStatusTimerCallback()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadCastReceiverTV)
        super.onDestroy()

        tracksViewModel.onCleanup()
    }

    override fun onUserContentOrderStatusCheck(status: Int) {
        contentOrderStatus = status
        setContentActionButton(contentOrderStatus)
    }

    private fun setContentActionButton(status: Int): Boolean {
        llPlayAllTvShow?.setOnClickListener(null)
        llPlayMovieActionBar?.setOnClickListener(null)
        if (status == Constant.CONTENT_ORDER_STATUS_NA || status == Constant.CONTENT_ORDER_STATUS_FAIL) {
            llPlayAllTvShow?.setOnClickListener(this)
            llPlayMovieActionBar?.setOnClickListener(this)
            return false
        } else if (status == Constant.CONTENT_ORDER_STATUS_IN_PROCESS || status == Constant.CONTENT_ORDER_STATUS_PENDING) {
            contentStatusProgress?.visibility = View.VISIBLE
            tvDetailBtnTitleActionBar?.visibility = View.GONE
            tvDetailBtnTitle?.visibility = View.GONE
            llDetails2.visibility = View.VISIBLE
            llPlayMovieActionBar.visibility = View.VISIBLE
            runContentOrderStatusHandler(this, selectedContentId!!)
            return true
        } else if (status == Constant.CONTENT_ORDER_STATUS_SUCCESS) {
            contentStatusProgress?.visibility = View.GONE
            tvDetailBtnTitleActionBar?.visibility = View.VISIBLE
            tvDetailBtnTitle?.visibility = View.VISIBLE
            tvDetailBtnTitle.text = getString(R.string.movie_str_7)
            tvDetailBtnTitleActionBar.text = getString(R.string.movie_str_7)
            llDetails2.visibility = View.VISIBLE
            llPlayMovieActionBar.visibility = View.VISIBLE
            ivDetailBtnIcon.setImageDrawable(
                requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite)
            )
            ivDetailBtnIconActionBar.setImageDrawable(
                requireContext().faDrawable(R.string.icon_play_2, R.color.colorWhite)
            )
            llPlayAllTvShow?.setOnClickListener(this)
            llPlayMovieActionBar?.setOnClickListener(this)
            return true
        }
        return false
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {
        getContentOrderStatus(this, contentId)

    }

    private fun showMiniVideoPlayer(intent: Intent) {
        if (intent?.hasExtra(Constant.VIDEO_START_POSITION) == true) {
            val video_start_position = intent.getLongExtra(Constant.VIDEO_START_POSITION, 0)
            val videoListModel: ArrayList<PlayableContentModel> =
                intent.getParcelableArrayListExtra(
                    Constant.VIDEO_LIST_DATA
                )!!
            val videoSeasonListModel: ArrayList<PlaylistModel.Data.Body.Row.Season> =
                intent.getParcelableArrayListExtra(
                    Constant.SEASON_LIST
                )!!
            val selectedTrackPosition = intent.getIntExtra(Constant.SELECTED_TRACK_POSITION, 0)
            BaseActivity.setVideoTrackListData(videoSeasonListModel)
            playAllVideo(
                videoListModel.get(selectedTrackPosition),
                video_start_position,
                selectedTrackPosition
            )
            setLog("VIDEO_START_POSITION-1", video_start_position.toString())
        }
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded) {
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.VIDEO_ACTIVITY_RESULT_CODE) {
                showMiniVideoPlayer(intent)
            }
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(
                    scrollView,
                    requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0),
                    0
                )
            }
        }
    }

    private fun playAllVideo(
        model: PlayableContentModel,
        trackPlayStartPosition: Long,
        selectedTrackPosition: Int
    ) {
        baseIOScope.launch {
            if (isAdded && context != null) {
                val videoUrl = model.data?.head?.headData?.misc?.url
                val videoDrmLicense = model.data?.head?.headData?.misc?.downloadLink?.drm?.token
                val videoTitle = model.data?.head?.headData?.title
                val videoSubTitle = model.data?.head?.headData?.subtitle
                val videoArtwork = model.data?.head?.headData?.image
                videoUrl?.let {
                    videoTitle?.let { it1 ->
                        playerType?.let { it2 ->
                            videoSubTitle?.let { it3 ->
                                videoArtwork?.let { it4 ->
                                    videoDrmLicense?.let { it5 ->
                                        CommonUtils.setVideoTrackList(
                                            requireContext(),
                                            model.data?.head?.headData?.id!!,
                                            it,
                                            it1,
                                            it2,
                                            it3,
                                            it4,
                                            it5,
                                            ContentTypes.TV_SHOWS.value
                                        )
                                    }
                                }
                            }
                            tracksViewModel.prepareTrackPlayback(
                                BaseActivity.nowPlayingCurrentIndex() + 1,
                                trackPlayStartPosition
                            )
                        }
                    }
                }
            }
        }

    }

    fun checkMiniVideoSound(){
        if(!Constant.isMute){
          //  VideoPlayerActivity.mPlayer?.volume = 0.0F
            if(VideoPlayerActivity?.mPlayer?.isPlaying==true) {
                VideoPlayerActivity?.mPlayer?.pause()
            }
            simpleExoplayerr?.volume = currentVolume

            if (activity != null && activity is MainActivity) {
                (activity as MainActivity).setPauseMusicPlayerOnVideoPlay()
            }
            ivMuteUnmuteMain?.setImageDrawable(
                requireContext()?.resources?.getDimensionPixelSize(R.dimen.font_16)
                    ?.let { it1 ->
                        requireContext()?.faDrawable(
                            R.string.icon_unmute,
                            R.color.colorWhite,
                            it1.toFloat()
                        )
                    })
        }
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
        intent.action = AudioPlayerService.PlaybackControls.PLAY.name
        intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
        intent.putExtra(Constant.SELECTED_TRACK_PLAY_START_POSITION, trackPlayStartPosition)
        intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.QUEUE_TRACKS)
        intent.putExtra(Constant.IS_TRACKS_QUEUEITEM, true)
        //(activity as MainActivity).reBindService()
        Util.startForegroundService(getViewActivity(), intent)
        (activity as MainActivity).reBindService()
    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }

    override fun onDownloadVideoQueueItemChanged(
        downloadManager: DownloadManager,
        download: Download
    ) {
        try {
            baseMainScope.launch {
                if (isAdded && context != null) {
                    val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                        ?.findByPlayableUrl(download.request.uri.toString())
                    val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                        ?.findByPlayableUrl(download.request.uri.toString())
                    val intent = Intent(Constant.TVSHOW_DOWNLOAD_EVENT)
                    intent.putExtra("EVENT", Constant.TVSHOW_DOWNLOAD_RESULT_CODE)

                    if (downloadQueue != null) {
                        for (season in seasonList.iterator()) {
                            val index = season?.data?.misc?.tracks?.indexOfFirst {
                                it?.data?.id == downloadQueue.contentId
                            }
                            if (index != null) {
                                intent.putExtra("index", index)
                                sendTVShowBroadcast(intent)
                            }
                        }
                    } else if (downloadedAudio != null) {
                        for (season in seasonList.iterator()) {
                            val index = season?.data?.misc?.tracks?.indexOfFirst {
                                it?.data?.id == downloadedAudio.contentId!!
                            }
                            if (index != null) {
                                intent.putExtra("index", index)
                                sendTVShowBroadcast(intent)
                            }
                        }
                    } else {
                        return@launch
                    }
                }

            }

        } catch (e: Exception) {

        }
        if (isAdded && context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                if (data.data.body.rows.isNotEmpty())
                    checkAllContentDWOrNot(data.data.body.rows.get(0))
            }
        }

        setLog("lahgkas", "DownLoadingQue")
    }

    override fun onDownloadProgress(
        downloads: List<Download?>?,
        progress: Int,
        currentExoDownloadPosition: Int
    ) {
    }

    override fun onDownloadsPausedChanged(
        downloadManager: DownloadManager,
        downloadsPaused: Boolean?
    ) {
        setLog("lahgkas", "onDownloadsPausedChanged")
    }

    private fun sendTVShowBroadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        checkMiniVideoSound()
    }

    override fun onPause() {
        super.onPause()
        if (!HungamaMusicApp.getInstance().activityVisible && simpleExoplayerr?.isPlaying == true)
            simpleExoplayerr?.pause()

    }

    private fun setLocalBroadcast() {
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(
            this,
            Constant.AUDIO_PLAYER_EVENT
        )
        HungamaMusicApp.hungamaMusicApp?.applicationContext?.let {
            LocalBroadcastManager.getInstance(
                it
            ).registerReceiver(broadCastReceiverTV, IntentFilter(Constant.TVSHOW))
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            baseMainScope.launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "TVLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
                checkMiniVideoSound()
            }
        } else {
            baseMainScope.launch {
                if (context != null) {
                    changeStatusbarcolor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.home_bg_color
                        )
                    )
                }
            }
        }
    }

    private fun redirectToMoreBucketListPage(
        bodyRowsItemsItem: ArrayList<BodyRowsItemsItem?>?,
        heading: String
    ) {
        val bundle = Bundle()
        val selectedMoreBucket = RowsItem()
        selectedMoreBucket.heading = heading
        selectedMoreBucket.items = bodyRowsItemsItem
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)
    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        onItemDetailPageRedirection(parent, parentPosition, childPosition, "")
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        baseMainScope.launch {
            if (isAdded && context != null) {
                val bundle = Bundle()
                bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
                setLog(TAG, "onMoreClick:selectedMoreBucket " + selectedMoreBucket?.heading)
                setLog(TAG, "onMoreClick:selectedMoreBucket " + selectedMoreBucket?.image)
                val moreBucketListFragment = MoreBucketListFragment()
                moreBucketListFragment.arguments = bundle
                addFragment(
                    R.id.fl_container,
                    this@TvShowDetailsFragment,
                    moreBucketListFragment,
                    false
                )

                val dataMap = java.util.HashMap<String, String>()
                dataMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + selectedMoreBucket?.heading)
                dataMap.put(
                    EventConstant.CONTENT_TYPE_EPROPERTY,
                    "" + tvShowDetailRespModel?.data?.head?.data?.title
                )

                dataMap.put(
                    EventConstant.SOURCEPAGE_EPROPERTY,
                    "" + Utils.getContentTypeDetailName("" + selectedMoreBucket?.type)
                )
                EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
            }
        }
    }
    private fun playTrailer(url: String) {
        baseMainScope.launch {
            try{
                if (isAdded && context != null && url.isNotEmpty()) {

                    BaseActivity.tvshowDetail = seasonList.get(0).data.misc.tracks.get(0)
                    val songsList =
                        CommonUtils.getVideoDummyData2(url)
                    val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                    val serviceBundle = Bundle()
                    serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
                    serviceBundle.putParcelableArrayList(Constant.SEASON_LIST, seasonList)
                    serviceBundle.putBoolean(Constant.IS_MUTE, Constant.isMute)
                    serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                    serviceBundle.putString(Constant.heading, tvTitle.text.toString()+" - Trailer")
                    serviceBundle.putString(Constant.Trailer_url, url)

                    serviceBundle.putInt(Constant.recent_episode, recent_episode.toInt())
                    serviceBundle.putInt(Constant.recent_season, recent_season.toInt())

                    simpleExoplayerr?.currentPosition
                        ?.let { serviceBundle.putLong(Constant.CURRNT_PLAYER_POSIITON, it) }

                    simpleExoplayerr?.currentPosition
                        ?.let { serviceBundle.putLong(Constant.VIDEO_START_POSITION, it) }

                    serviceBundle.putString(
                        Constant.SELECTED_CONTENT_ID,
                        seasonList.get(0).data.misc.tracks.get(0).data.id
                    )
                    serviceBundle.putInt(Constant.CONTENT_TYPE, Constant.CONTENT_TV_SHOW)
                    serviceBundle.putInt(
                        Constant.TYPE_ID,
                        seasonList.get(0).data.misc.tracks.get(0).data.type
                    )
                    intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                    intent.putExtra(
                        "thumbnailImg",
                        seasonList.get(0).data.misc.tracks.get(0).data.image
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    /*serviceBundle.putLong(
                        Constant.VIDEO_START_POSITION, TimeUnit.SECONDS.toMillis(
                            HungamaMusicApp.getInstance().getContentDuration(
                                seasonList.get(0).data.misc.tracks.get(0).data.id
                            )!!
                        )
                    )*/
                    (requireActivity() as MainActivity).setLocalBroadcastEventCall(
                        this@TvShowDetailsFragment,
                        Constant.VIDEO_PLAYER_EVENT
                    )
                    if (activity != null) {
                        val status = (activity as MainActivity).getAudioPlayerPlayingStatus()
                        if (status == Constant.pause) {
                            SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                        } else {
                            SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                        }
                        (activity as MainActivity).pausePlayer()
                    }
                    startActivity(intent)
                    /*startActivityForResult(
                        intent,
                        Constant.VIDEO_ACTIVITY_RESULT_CODE
                    )*/


                }
            }catch (e:Exception){}
        }
    }

    private fun showlastplayepisode(seasonsModel: PlaylistModel.Data.Body.Row) {


        var idList =""
        seasonsModel?.seasons?.forEachIndexed { i, season ->
            seasonsModel.seasons.get(i).data?.misc?.tracks?.forEachIndexed { index, track ->

                if(index==0) {
                    if (idList.isEmpty()) {
                        idList = track.data.id
                    } else idList += ",${track.data.id}"

                }else idList += ",${track.data.id}"

            }
        }

        if (ConnectionUtil(context).isOnline) {
                    recentlyPlayViewModel?.getContinueWhereLeftListDetails(requireContext(), idList)?.observe(this,
                        Observer {
                            if (it != null) {
                                continueWhereLeftModel = it?.data
                                if(!continueWhereLeftModel?.get(0)?.remark.isNullOrBlank()) {

                                    live_remark.postValue(continueWhereLeftModel?.get(0)?.remark?.replace(".","•"))
                                }
                            }
                        })
                }

    }

    private fun startTimer() {
        val timer = object : CountDownTimer(3 * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {


            }
        }
        timer.start()
    }

    private suspend fun checkAllContentDWOrNot(seasonsModel: PlaylistModel.Data.Body.Row){
        try {
            if (isAdded && context != null) {
                var isAllDownloaded = false
                var isAllDW = true
                var isAllDownloadInQueue = false
                var isCurrentContentPlayingFromThis = false
                for (item in  seasonsModel.seasons.iterator()) {
                    if (!item?.data?.misc?.tracks.isNullOrEmpty()) {
                            try {
                                item.data.misc.tracks.forEachIndexed { index, it ->
                                    if (it != null) {
                                        if (!isAllDownloadInQueue) {
                                            val downloadQueue =
                                                AppDatabase.getInstance()?.downloadQueue()
                                                    ?.getDownloadQueueItemsByContentIdAndContentType(
                                                        it.data.id.toString(),
                                                        ContentTypes.TV_SHOWS.value
                                                    )
                                            if (downloadQueue?.contentId.equals(it.data.id)) {
                                                    isAllDownloadInQueue = true
                                                } else {
                                                    isAllDownloadInQueue = false
                                                }
                                            }
                                        }

                                        if (isAllDW) {
                                            val downloadedAudio =
                                                AppDatabase.getInstance()?.downloadedAudio()
                                                    ?.getDownloadedAudioItemsByContentIdAndContentType(
                                                        it.data.id, ContentTypes.TV_SHOWS.value
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

                            } catch (e: Exception) {

                            }

                        }
                }
                setLog(
                    "isCurrentContentPlayingFromThis",
                    "isCurrentContentPlayingFromThis-4-$isCurrentContentPlayingFromThis"
                )
                setIsAllDownloadImage(isAllDownloadInQueue, isAllDownloaded)
            }
        } catch (e: Exception) {

        }
    }

    suspend fun setIsAllDownloadImage(isAllDownloadInQueue: Boolean, isAllDownloaded: Boolean) {
        baseMainScope.launch {
            if (isAllDownloadInQueue) {
                ivDownload.setImageDrawable(
                    requireContext().faDrawable(
                        R.string.icon_downloading,
                        R.color.colorWhite
                    )
                )
                tvDownload.text = "Downloading"
            } else {
                if (isAllDownloaded) {
                    ivDownload.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_downloaded2,
                            R.color.colorWhite
                        )
                    )
                    tvDownload.text = "Downloaded"
                } else {
                    ivDownload.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_download,
                            R.color.colorWhite
                        )
                    )
                    tvDownload.text = "Download"
                }
            }
        }
    }





}