package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.media3.common.util.Util
import com.google.android.material.tabs.TabLayout
import com.hungama.fetch2.Download
import com.hungama.fetch2core.Reason
import com.hungama.music.HungamaMusicApp
import com.hungama.myplay.activity.R
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.FavouritedEvent
import com.hungama.music.eventanalytic.eventreporter.MoreClickedEvent
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.player.videoplayer.VideoPlayerActivity
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseActivity.Companion.setTrackListData
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.DetailAudioBookAdapter
import com.hungama.music.ui.main.adapter.TabAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PodcastViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.calculateAverageColor
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.AUDIO_BOOK_STORY_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.AUDIO_BOOK_STORY_DETAIL_PAGE
import com.hungama.music.utils.Constant.PODCAST_DETAIL_ADAPTER
import com.hungama.music.utils.Constant.PODCAST_DETAIL_PAGE
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.customview.downloadmanager.model.DownloadQueue
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fr_tv_show_details_v1.*
import kotlinx.android.synthetic.main.fragment_artist_details.ivFollow
import kotlinx.android.synthetic.main.fragment_artist_details.ivFollowActionBar
import kotlinx.android.synthetic.main.fragment_artist_details.tvFollow
import kotlinx.android.synthetic.main.fragment_artist_details.tvFollowActionBar
import kotlinx.android.synthetic.main.fragment_artist_details.tvTitle
import kotlinx.android.synthetic.main.fragment_audio_book_details.*
import kotlinx.android.synthetic.main.fragment_audio_book_details.headBlur
import kotlinx.android.synthetic.main.fragment_audio_book_details.llDetails
import kotlinx.android.synthetic.main.fragment_audio_book_details.llToolbar
import kotlinx.android.synthetic.main.fragment_audio_book_details.rlHeading
import kotlinx.android.synthetic.main.fragment_audio_book_details.rvRecomendation
import kotlinx.android.synthetic.main.fragment_audio_book_details.scrollView
import kotlinx.android.synthetic.main.fragment_audio_book_details.tabLayout
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AudioBookDetailsFragment : BaseFragment(), OnParentItemClickListener,
    TabLayout.OnTabSelectedListener, TracksContract.View, BucketParentAdapter.OnMoreItemClick,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnDownloadQueueItemChanged,
    OnUserSubscriptionUpdate, BaseActivity.PlayItemChangeListener,
    BaseActivity.OnLocalBroadcastEventCallBack, BaseFragment.OnMenuItemClicked {
    var artImageUrl: String? = null
    var selectedContentId: String? = null
    var selectedContentId1: String? = null
    var playerType: String? = null
    private var chartDetailBgArtImageDrawable: LayerDrawable? = null
    var tabAdapter: TabAdapter? = null
    var podcastListViewModel: PodcastViewModel? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()
    var isFavorite = false
    var bucketRespModel: HomeModel? = null
    var rowList: MutableList<RowsItem?>? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var podcastEpisodeList: List<PlaylistModel.Data.Body.Row.Data.Misc.Track> = ArrayList()
    var ascending = true
    var episodeAdpter: DetailAudioBookAdapter? = null
    var artworkProminentColor = 0
    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var page = 1
    var limit = 10
    var userViewModel: UserViewModel? = null
    var isFollowing = false
    var userSocialData: UserSocialData? = null
    var bookmarkDataModel: BookmarkDataModel? = null
    var isPlaying = false
    var isFromVerticalPlayer = false
    var defaultContentVarient = false
    var userFollowData: FollowModel? = null
    var tempPodcastRespModel: PlaylistDynamicModel? = null
    var tempPodcastEpisode: PlaylistModel.Data.Body.Row.Data.Misc.Track? = null
    var share = ""
    companion object {
        var podcastRespModel: PlaylistDynamicModel? = null
        var podcastEpisode: PlaylistModel.Data.Body.Row.Data.Misc.Track? = null
        var playableItemPosition = 0
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
        return inflater.inflate(R.layout.fragment_audio_book_details, container, false)
    }

    override fun initializeComponent(view: View) {
        applyButtonTheme(requireContext(), llListen)
        applyButtonTheme(requireContext(), llListenActionBar)
        /*val icon_sort = FontDrawable(requireContext(), R.string.icon_sort)
        icon_sort.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        ivShort?.setImageDrawable(icon_sort)*/
        if (arguments != null){
            if(requireArguments().containsKey(Constant.defaultContentId)){
                selectedContentId = requireArguments().getString(Constant.defaultContentId).toString()
                selectedContentId1 = requireArguments().getString(Constant.defaultContentId).toString()
       //         setLog(TAG, "initializeComponent: selectedContentId "+selectedContentId)
            }
            if(requireArguments().containsKey(Constant.isFromVerticalPlayer)){
                isFromVerticalPlayer = requireArguments().getBoolean(Constant.isFromVerticalPlayer)
                setLog(TAG, "initializeComponent: selectedContentId "+selectedContentId)
            }
            if(requireArguments().containsKey(Constant.defaultContentVarient)){
                defaultContentVarient = requireArguments().getBoolean(Constant.defaultContentVarient)
            }

        }
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        ivBack?.setOnClickListener { view -> backPress() }
        scrollView.viewTreeObserver.addOnScrollChangedListener(this)
        rvChapter.visibility = View.VISIBLE
        //rvMain.visibility = View.VISIBLE

        setUpPodcastDetailListViewModel()

        //setUpPodcastSimilarViewModel()
        tracksViewModel = TracksViewModel(Injection.provideTrackRepository(), this)

        rlShowMore.setOnClickListener {
            setLog("rlShowMore","rlShowMore podcastEpisodeList?.size:${podcastEpisodeList.size} count:${podcastRespModel?.data?.head?.data?.misc?.episodeCount} isLoading:${isLoading} isLastPage:${isLastPage}")
            if (podcastEpisodeList.size < podcastRespModel?.data?.head?.data?.misc?.episodeCount!! && !isLoading && !isLastPage) {
                isLoading = true
                //you have to call loadmore items to get more data
                page++
                getMoreEpisode()
            }
        }

        threeDotMenu?.setOnClickListener(this)
        threeDotMenu2?.setOnClickListener(this)
        llListen?.setOnClickListener(this)

        shimmerLayoutPodcast.visibility = View.VISIBLE
        shimmerLayoutPodcast.startShimmer()

        if(defaultContentVarient){
//            podcastAlbumArtImageView_v2.show()
            podcastAlbumArtImageView.hide()
            llDetails.hide()
        }else{
            podcastAlbumArtImageView_v2.hide()
            podcastAlbumArtImageView.show()
            llDetails.show()
        }

        (context as MainActivity).addPlayItemChangeListener(this)
        CommonUtils.setPageBottomSpacing(
            scrollView,
            requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0),
            0
        )
    }

    private fun playAllPodcast() {
        if (isPlaying) {
            if (podcastEpisodeList != null && podcastEpisodeList.size > 0) {
                playableItemPosition = 0

                if (!CommonUtils.isUserHasGoldSubscription()) {
                    if (CommonUtils.checkMovieRight(podcastEpisodeList.get(playableItemPosition).data.misc.movierights)){

                        CommonUtils.openSubscriptionDialogPopup(
                            requireContext(),
                            PlanNames.SVOD.name,
                            "",
                            true,
                            null,
                            "",
                            null, Constant.drawer_streaming_podcast
                        )
                        return
                    }
                 }
                        setUpPlayableContentListViewModel(
                            podcastEpisodeList.get(
                                playableItemPosition
                            )?.data?.id!!
                        )
                        setEventModelDataAppLevel(
                            podcastEpisodeList.get(playableItemPosition)?.data?.id!!,
                            podcastEpisodeList.get(playableItemPosition)?.data?.title!!,
                            podcastRespModel?.data?.head?.data?.title!!,
                            playableItemPosition
                        )
                    }
            } else {
            (requireActivity() as MainActivity).pausePlayer()
            playPauseStatusChange(true)
        }
    }

    private fun staticToolbarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //(activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
            MainScope().launch {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
        return onCreateOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.podcast_menu, menu)
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
        if (activity != null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                artImageUrl
            ) && chartDetailroot != null
        ) {
            val bgColor = ColorDrawable(resources.getColor(R.color.home_bg_color))
            val bgImage: Drawable? =
                ContextCompat.getDrawable(requireContext(), R.drawable.audio_player_bg_two)
            val gradient: Drawable? = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.audio_player_gradient_drawable
            )
            val result: Deferred<Bitmap?> = GlobalScope.async {
                val urlImage = URL(artImageUrl)
                urlImage.toBitmap()
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // get the downloaded bitmap
                    val bitmap: Bitmap? = result.await()

                    val artImage = BitmapDrawable(resources, bitmap)
                    if (status) {
                        if (bitmap != null) {
                            //val color = dynamicToolbarColor(bitmap)
                            Palette.from(bitmap).generate { palette ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                      artworkProminentColor = calculateAverageColor(bitmap, 1)
                                    //(activity as AppCompatActivity).window.statusBarColor = artworkProminentColor
                                    MainScope().launch {
                                        if (context != null) {
                                            setLog(
                                                "PodcastLifecycle",
                                                "setArtImageBg--$artworkProminentColor"
                                            )
                                            changeStatusbarcolor(artworkProminentColor)
                                        }
                                    }
                                }

                                val color2 = ColorDrawable(palette?.getDominantColor(R.attr.colorPrimaryDark)!!)
                                chartDetailBgArtImageDrawable =
                                    LayerDrawable(arrayOf<Drawable>(bgColor, color2, gradient!!))
                                chartDetailroot?.background = artImage
                            }

                        }

                    }
                } catch (e: Exception) {

                }


            }
        }

    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            setLog("onPageSelected", "Selected position:" + position)
        }
    }


    private fun getUserBookmarkedData() {
        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getUserBookmarkedData(requireContext(), Constant.MODULE_FAVORITE)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    fillUIs(it.data)
                                }

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","getUserBookmarkedData")
        }
    }

    private fun fillUIs(bookmarkDataModel: BookmarkDataModel) {
        this.bookmarkDataModel = bookmarkDataModel
        if (bookmarkDataModel != null && bookmarkDataModel.data?.body?.rows != null && bookmarkDataModel.data?.body?.rows?.size!! > 0) {
            for (bookmark in bookmarkDataModel.data?.body?.rows?.iterator()!!) {
                if (podcastRespModel?.data?.head?.data?.id.equals(bookmark.data?.id)) {
                    setLog(TAG, "fillUI: after" + isFavorite)
                    isFavorite = true
                    setLog(TAG, "fillUI:before " + isFavorite)
                }
            }
        }
    }

    private fun setupTab() {
        for (k in 1..5) {
            tabLayout.addTab(tabLayout.newTab().setText("Season " + k))
        }
        tabLayout.addOnTabSelectedListener(this)
    }

    private fun setUpPodcastDetailListViewModel() {
        podcastListViewModel = ViewModelProvider(
            this
        ).get(PodcastViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getAudioBookDetailList(requireContext(), selectedContentId!!)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setLog(TAG, "setUpPodcastDetailListViewModel: getPodcastDetailList called")


//                                fillPodcastData(it?.data!!)
                                val image = it.data?.data?.head?.data?.image.toString()
                                share = it.data?.data?.head?.data?.misc?.share.toString()

                                if (ConnectionUtil(context).isOnline) {
                                    if (it?.data?.data?.body?.rows?.size!! > 0 && it.data.data.body.rows.get(0).data.misc.tracks.size > 0)
                                    it.data.data?.body?.rows?.get(0)?.data?.misc?.tracks?.get(0)?.data?.id?.let { it1 ->
                                        podcastListViewModel?.getAudioBookDetailList(requireContext(),
                                            it1
                                        )
                                            ?.observe(this,
                                                Observer {
                                                    when (it.status) {
                                                        Status.SUCCESS -> {
                                                            setLog(TAG, "setUpPodcastDetailListViewModel: getPodcastDetailList called")

                                                            if (!TextUtils.isEmpty(share))
                                                                it?.data?.data?.head?.data?.misc?.share = share

                                                            if (!TextUtils.isEmpty(image))
                                                                it?.data?.data?.head?.data?.image = image


                                                            fillPodcastData(it?.data!!)

                                                        }

                                                        Status.LOADING -> {
                                                            setProgressBarVisible(false)
                                                        }

                                                        Status.ERROR -> {
                                                            setEmptyVisible(false)
                                                            setProgressBarVisible(false)
                                                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                                                        }
                                                    }
                                                })
                                    }
                                } else {
                                    val messageModel = MessageModel(
                                        getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                                        MessageType.NEGATIVE, true
                                    )
                                    CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","setUpPodcastDetailListViewModel")
                                }

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","setUpPodcastDetailListViewModel")
        }
        getUserBookmarkedData()
    }




    fun fillPodcastData(homedata: PlaylistDynamicModel?){

        var isPodcastHaveSeason=false
        var seasonTrack: PlaylistModel.Data.Body.Row.Data.Misc.Track? =null
        try {
            if (!homedata?.data?.body?.rows?.get(0)?.data?.misc?.tracks.isNullOrEmpty()) {
                homedata?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.forEach {
                    if (it != null && it.data?.type == 111) {
                        seasonTrack = it
                        isPodcastHaveSeason = true
                        return@forEach
                    }
                }
            }
        } catch (e: Exception) {

        }


        setLog("Podcast","isPodcastHaveSeason:${isPodcastHaveSeason}")


        if (homedata != null) {
            //setLog(TAG, "isViewLoading $it")
            podcastRespModel = homedata
            chartDetailroot.visibility = View.VISIBLE
//            view1.visibility = View.VISIBLE
//            views.visibility = View.VISIBLE
            scrollView.visibility = View.VISIBLE
            setDetails(homedata, true)

        }
        if(isPodcastHaveSeason&&seasonTrack!=null){
            getSeasonTrackList(seasonTrack?.data?.id!!)
        }else{

            try{
                setPodcastDetailsListData(homedata!!)
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

    }

    fun setPodcastDetailsListData(podcastModel: PlaylistDynamicModel) {

        if (podcastModel != null && podcastModel.data?.body?.rows!= null
            && podcastModel.data?.body?.rows?.get(0)?.data?.misc?.tracks != null) {

            llListen?.setOnClickListener(this)
            llListenActionBar?.setOnClickListener(this)
            setupUserViewModel()
            if (podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks != null) {
                podcastEpisodeList = podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks!!
                //setPodcastSongDataList(podcastModel.body!!.items)

                var totalDuration = "00:00:00"
                if (podcastRespModel?.data?.body?.rows?.isNotEmpty() == true)
                {
                    val tracks = podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks
                    var duration = 0
                    if (tracks != null) {
                        for (item in tracks){
                            duration += item.data.duration.toInt()
                        }
                    }
                    if (duration>0){
                        var hours = duration.div(3600).toString()
                        var minutes = (duration.rem(3600))?.div(60).toString()
                        var seconds = (duration.rem(60)).toString()
                        if (hours.toInt() <= 9)
                            hours = "0$hours"
                        if (minutes.toInt() <= 9)
                            minutes = "0$minutes"
                        if (seconds.toInt() <= 9)
                            seconds = "0$seconds"

                        totalDuration = "$hours:$minutes:$seconds"
                    }

                }

                val tvEpisodesCountData = podcastRespModel?.data?.head?.data?.misc?.episodeCount.toString() + " " +
                        if (podcastRespModel?.data?.head?.data?.type == 116) podcastRespModel?.data?.head?.data?.misc?.episodeCount?.let {
                            removeLastChar(
                                it, getString(R.string.podcast_str_9))
                        } else podcastRespModel?.data?.head?.data?.misc?.episodeCount?.let {
                            removeLastChar(
                                it, getString(R.string.audio_books_stories_str_library_chapters_heading))
                        }

                tvEpisodesCount.text = tvEpisodesCountData + " • $totalDuration ${getString(R.string.live_events_str_7).lowercase()}"

                sortView.visibility = View.VISIBLE

                if (podcastEpisodeList.size >= podcastRespModel?.data?.head?.data?.misc?.episodeCount!!) {
                    rlShowMore.visibility = View.GONE
                    isLastPage = true
                } else {
                    rlShowMore.visibility = View.VISIBLE
                }
                val layoutManager = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                /*rvPodcastMain.apply {
                    setRecycledViewPool(RecyclerView.RecycledViewPool())
                    setHasFixedSize(false)
                }*/
                rvChapter.isNestedScrollingEnabled = false
                rvChapter.layoutManager = layoutManager
                setPodcastEpisodeAdapter(!ascending)

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val hashMapPageView = HashMap<String, String>()
                        if (podcastRespModel != null) {
                            hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = podcastRespModel?.data?.head?.data?.title.toString()
                            hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] =
                                "" + Utils.getContentTypeNameForStream("" + podcastRespModel?.data?.head?.data?.type)
                            hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = podcastRespModel?.data?.head?.data?.id.toString()
                            hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
                            hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =
                                "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + if (podcastRespModel?.data?.head?.data?.type == 115) "Audio Story" else "Audio Book"
                            hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "" + if (podcastRespModel?.data?.head?.data?.type == 115) "Audio Story" else "Audio Book"

                            EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
                        }
                    }catch (e:Exception){

                    }
                }
            }
            if (podcastModel.data.body?.recomendation != null && podcastModel.data.body?.recomendation?.size!! > 0) {
                rvRecomendation.visibility = View.VISIBLE
                rvRecomendation.visibility = View.VISIBLE

                val varient = Constant.ORIENTATION_HORIZONTAL

                val bucketParentAdapter = BucketParentAdapter(
                    podcastModel.data.body?.recomendation!!,
                    requireContext(),
                    this,
                    this,
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


                bucketParentAdapter.addData(podcastModel.data.body?.recomendation!!)
                rvRecomendation?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val firstVisiable: Int = mLayoutManager.findFirstVisibleItemPosition()
                        val lastVisiable: Int =
                            mLayoutManager.findLastCompletelyVisibleItemPosition()

                        setLog(
                            TAG,
                            "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                        )
                        if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                            val fromBucket =
                                podcastModel.data.body?.recomendation?.get(firstVisiable)?.heading
                            val toBucket =
                                podcastModel.data.body?.recomendation?.get(lastVisiable)?.heading
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
                rvRecomendation.setPadding(0, 0, 0, 0)
            }

            var isContentAutoPlay = 0
            if (requireArguments().containsKey("isPlay")) {
                isContentAutoPlay = requireArguments().getInt("isPlay")
            }
            setLog(TAG, "setPlayListSongAdapter: auto play${isContentAutoPlay}")
            if (isContentAutoPlay == 1) {
                llListen?.performClick()
            }
        }
        setProgressBarVisible(false)
        shimmerLayoutPodcast?.visibility = View.GONE
        shimmerLayoutPodcast?.stopShimmer()
    }

    fun removeLastChar(trackCount:Int, title:String):String{
        return if (trackCount>1) title else title.dropLast(1)
    }

    private fun setPodcastEpisodeAdapter(asc: Boolean) {
        val tempList: ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track> = ArrayList()
        setLog("podcastDetailsFragment", "setPodcastEpisodeAdapter-tempList.size-${tempList.size} - asc-$asc")
        podcastEpisodeList.forEachIndexed { index, track ->
            if (track!=null&&track.itype != Constant.podcastNativeAds){
                tempList.add(track)
            }
        }
        podcastEpisodeList = ArrayList()
        podcastEpisodeList = tempList
        /*if (asc) {
            podcastEpisodeList = podcastEpisodeList.sortedBy {
                it.data?.releasedate
            }
            tvSort.text = resources.getText(R.string.podcast_str_1)
            //ivShort.setRotation(0F)
        } else {
            podcastEpisodeList = podcastEpisodeList.sortedByDescending { it?.data?.releasedate }
            tvSort.text = resources.getText(R.string.podcast_str_2)
            //ivShort.setRotation(180F)
        }*/
        checkAllContentDownloadedOrNot(podcastEpisodeList)
//        podcastEpisodeList = getAdsData(podcastEpisodeList)
        episodeAdpter = DetailAudioBookAdapter(
            requireContext(), podcastEpisodeList,
            object : DetailAudioBookAdapter.OnChildItemClick {
                override fun onUserClick(
                    childPosition: Int,
                    isMenuClick: Boolean,
                    isDownloadClick: Boolean
                ) {
                    podcastEpisode = podcastEpisodeList.get(childPosition)
                    playableItemPosition = childPosition
                    if (isMenuClick) {
                        if (isOnClick()) {
                            commonThreeDotMenuItemSetup(AUDIO_BOOK_STORY_DETAIL_ADAPTER, null, false,
                                podcastEpisodeList.get(childPosition), 1)
                        }
                    } else if (isDownloadClick) {
                        val dpm = DownloadPlayCheckModel()
                        dpm.contentId = podcastEpisodeList.get(childPosition)?.data?.id?.toString()!!
                        dpm.contentTitle =
                            podcastEpisodeList.get(childPosition)?.data?.title?.toString()!!
                        dpm.planName =
                            podcastEpisodeList.get(childPosition)?.data?.misc?.movierights.toString()
                        dpm.isAudio = true
                        dpm.isDownloadAction = true
                        dpm.isShowSubscriptionPopup = true
                        dpm.clickAction = ClickAction.FOR_SINGLE_CONTENT
                        dpm.restrictedDownload = RestrictedDownload.valueOf(podcastEpisodeList.get(childPosition)?.data?.misc?.restricted_download!!)

                        Constant.screen_name ="Audio Book Details"
                        if (CommonUtils.userCanDownloadContent(
                                requireContext(),
                                chartDetailroot,
                                dpm,
                                this@AudioBookDetailsFragment,Constant.drawer_downloads_exhausted
                            )
                        ) {
                            var downloadQueueList: ArrayList<DownloadQueue> = ArrayList()

                            val dq = DownloadQueue()
                            if (!TextUtils.isEmpty(podcastEpisodeList.get(childPosition)?.data?.id.toString())) {
                                dq.contentId = podcastEpisodeList.get(childPosition)?.data?.id.toString()
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList.get(childPosition)?.data?.title!!)) {
                                dq.title = podcastEpisodeList.get(childPosition)?.data?.title!!
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList.get(childPosition)?.data?.subtitle!!)) {
                                dq.subTitle = podcastEpisodeList.get(childPosition)?.data?.subtitle!!
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList.get(childPosition)?.data?.image!!)) {
                                dq.image = podcastEpisodeList.get(childPosition)?.data?.image!!
                            }

                            if (!TextUtils.isEmpty(selectedContentId1)) {
                                dq.parentId = selectedContentId1
                            }
                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title!!)) {
                                dq.pName = podcastRespModel?.data?.head?.data?.title
                            }

                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.subtitle!!)) {
                                dq.pSubName = podcastRespModel?.data?.head?.data?.subtitle
                            }

                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.releasedate!!)) {
                                dq.pReleaseDate = podcastRespModel?.data?.head?.data?.releasedate
                            }

                            if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.image!!)) {
                                dq.pImage = podcastRespModel?.data?.head?.data?.image
                            }

                            if (!TextUtils.isEmpty(podcastEpisodeList.get(childPosition)?.data?.misc?.movierights.toString())) {
                                dq.planName = podcastEpisodeList.get(childPosition)?.data?.misc?.movierights.toString()
                            }

                            if (podcastRespModel?.data?.head?.data?.type == 115 || podcastRespModel?.data?.head?.data?.type == 116){
                                dq.pType = ContentTypes.AUDIO_STORY.value
                                dq.contentType = ContentTypes.AUDIO_STORY.value
                            }
                            else{
                                dq.pType = ContentTypes.AUDIO_BOOK.value
                                dq.contentType = ContentTypes.AUDIO_BOOK.value
                            }

                            val eventModel = HungamaMusicApp.getInstance().getEventData(selectedContentId.toString())
                            dq.source = eventModel.sourceName

                            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()
                                ?.findByContentId(podcastEpisodeList.get(childPosition)?.data?.id!!.toString())
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(podcastEpisodeList.get(childPosition)?.data?.id!!.toString())
                            if ((!downloadQueue?.contentId.equals(podcastEpisodeList.get(childPosition)?.data?.id.toString()))
                                && (!downloadedAudio?.contentId.equals(podcastEpisodeList.get(childPosition)?.data?.id!!.toString()))) {
                                downloadQueueList.add(dq)
                            }
                            (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
                                downloadQueueList,
                                this@AudioBookDetailsFragment,
                                null,
                                true,
                                true
                            )
                        }
                    }
                    else {
                        var dpm : DownloadPlayCheckModel? = null
                        if (CommonUtils.checkEnableFlag(CommonUtils.getFirebaseConfigAdsData().drawerStreamingAudiobook, podcastEpisodeList.get(childPosition).data.misc.movierights.toString()) != null){
                            dpm = DownloadPlayCheckModel()
                            dpm.isDirectPaymentAction = true
                            dpm.planName = podcastEpisodeList.get(childPosition).data.misc.movierights.toString()
                        }

                        if (!CommonUtils.isUserHasGoldSubscription()){
                            if (CommonUtils.checkMovieRight(podcastEpisodeList.get(playableItemPosition).data.misc.movierights)){
                                CommonUtils.openSubscriptionDialogPopup(
                                    requireContext(),
                                    PlanNames.SVOD.name,
                                    "",
                                    true,
                                    null,
                                    "",
                                    dpm,
                                    dpm?.planName ?: "",Constant.drawer_streaming_podcast
                                )
                                return
                            }
                        }
                        if (isOnClick()) {
                            val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                                ?.findByContentId(podcastEpisodeList.get(childPosition)?.data?.id!!.toString())
                            if (downloadedAudio != null && downloadedAudio.contentId.equals(
                                    podcastEpisodeList.get(childPosition)?.data?.id!!.toString()
                                )
                            ) {
                                val playableContentModel = PlayableContentModel()
                                playableContentModel.data.head?.headData?.id =
                                    downloadedAudio.contentId!!
                                playableContentModel.data.head?.headData?.misc?.url =
                                    downloadedAudio.downloadedFilePath
                                playableContentModel.data.head?.headData?.misc?.downloadLink?.drm?.url =
                                    downloadedAudio.downloadUrl!!
                                playableContentModel.data.head?.headData?.misc?.downloadLink?.drm?.token =
                                    downloadedAudio.drmLicense
                                playableContentModel.data.head?.headData?.misc?.sl?.lyric?.link =
                                    downloadedAudio.lyricsUrl
                                setPlayableContentListData(playableContentModel)
                            } else {
                                setUpPlayableContentListViewModel(podcastEpisodeList.get(childPosition)?.data?.id!!)

                                setEventModelDataAppLevel(
                                    podcastEpisodeList.get(
                                        childPosition
                                    )?.data?.id!!, podcastEpisodeList.get(
                                        childPosition
                                    )?.data?.title!!,
                                    podcastRespModel?.data?.head?.data?.title!!,childPosition
                                )
                            }
                        }

                    }

                }

                override fun onPlayPauseClick(position: Int) {

                    if (!CommonUtils.isUserHasGoldSubscription()){
                        if (CommonUtils.checkMovieRight(podcastEpisodeList.get(playableItemPosition).data.misc.movierights)) {
                            CommonUtils.openSubscriptionDialogPopup(
                                requireContext(),
                                PlanNames.SVOD.name,
                                "",
                                true,
                                null,
                                "",
                                null, Constant.drawer_streaming_podcast
                            )
                            return
                        }
                    }

                    if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList.size > position
                        && !BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList.size > BaseActivity.nowPlayingCurrentIndex()) {
                        val currentPlayingContentId =
                            BaseActivity.songDataList.get(BaseActivity.nowPlayingCurrentIndex())?.id
                        if (currentPlayingContentId?.toString()
                                ?.equals(podcastEpisodeList.get(position).data.id)!!
                        ) {
                            if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                (requireActivity() as MainActivity).pausePlayer()
                                if ((activity as MainActivity).audioPlayer != null)
                                    (activity as MainActivity).audioPlayer?.currentPosition?.let {
                                        TimeUnit.MILLISECONDS.toSeconds(
                                            it
                                        )
                                    }?.let {
                                        HungamaMusicApp.getInstance().userStreamList.put(podcastEpisodeList.get(position).data.id,
                                            it
                                        )
                                    }
                                episodeAdpter?.notifyItemChanged(position)
                                playPauseStatusChange(true)
                            } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                                (requireActivity() as MainActivity).playPlayer()
                            } else {
                                (requireActivity() as MainActivity).playPlayer()
                            }
                        }
                    }
                }


            })
        rvChapter.adapter = episodeAdpter

        CoroutineScope(Dispatchers.Main).launch {
            if (requireArguments().getBoolean(Constant.isPlayFromBanner)) {
                playPauseStatusChange(true)
                if (podcastRespModel != null && podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.size!! > 0) {
                   isPlaying = true

                    if (!CommonUtils.isUserHasGoldSubscription()){
                        if (CommonUtils.checkMovieRight(podcastEpisodeList.get(playableItemPosition).data.misc.movierights)) {
                            CommonUtils.openSubscriptionDialogPopup(
                                requireContext(),
                                PlanNames.SVOD.name,
                                "",
                                true,
                                null,
                                "",
                                null, Constant.drawer_streaming_podcast
                            )
                            return@launch
                        }
                    }
                    playAllPodcast()
                }
            }
        }
    }

    private fun setDetails(it: PlaylistDynamicModel?, status: Boolean) {
        artImageUrl = podcastRespModel?.data?.head?.data?.image
        playerType = "" + podcastRespModel?.data?.head?.data?.type
        if (podcastRespModel?.data?.head?.data?.variant == "v2"){
            llDetails.hide()
            podcastAlbumArtImageView.visibility = View.GONE
            podcastAlbumArtImageView_v2.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(artImageUrl)) {
/*                ImageLoader.loadImage(
                    requireContext(),
                    podcastAlbumArtImageView,
                    artImageUrl!!,
                    R.drawable.bg_gradient_placeholder
                )*/

                ImageLoader.loadImage(
                    requireContext(),
                    podcastAlbumArtImageView_v2,
                    artImageUrl!!,
                    R.drawable.bg_gradient_placeholder
                )
                setArtImageBg(true)
            } else {
                ImageLoader.loadImage(
                    requireContext(),
                    podcastAlbumArtImageView_v2,
                    "",
                    R.drawable.bg_gradient_placeholder
                )

/*                ImageLoader.loadImage(
                    requireContext(),
                    podcastAlbumArtImageView_v2,
                    "",
                    R.drawable.bg_gradient_placeholder
                )*/
                staticToolbarColor()
            }
        }
        else{

            podcastAlbumArtImageView.visibility = View.VISIBLE
            llDetails.show()
            podcastAlbumArtImageView_v2.visibility = View.GONE
            relative_v2.visibility = View.GONE
            if (!TextUtils.isEmpty(artImageUrl)){
                ImageLoader.loadImage(
                    requireContext(),
                    podcastAlbumArtImageView,
                    artImageUrl.toString(),
                    R.drawable.bg_gradient_placeholder
                )
                setArtImageBg(true)
            }
            else{
                ImageLoader.loadImage(
                    requireContext(),
                    podcastAlbumArtImageView,
                    "",
                    R.drawable.bg_gradient_placeholder
                )
            }

                staticToolbarColor()
        }

        if (!TextUtils.isEmpty(it?.data?.head?.data?.misc?.trailer)){
            llSample.show()
            llSample.setOnClickListener(this@AudioBookDetailsFragment)
        }
        else
            llSample.hide()

        iv_collapsingImageBg?.visibility = View.VISIBLE
        realtimeBlurView?.visibility = View.VISIBLE
        //mainGradientView?.visibility = View.VISIBLE
        if (it?.data?.head?.data?.title != null && !TextUtils.isEmpty(it.data?.head?.data?.title)) {
            tvTitle.text = it.data?.head?.data?.title
        } else {
            tvTitle.text = ""
            tvTitle.hide()
        }

        if (it?.data?.head?.data?.misc?.artist != null && it.data?.head?.data?.misc?.artist?.size!! > 0) {
            val artist = TextUtils.join("/", it.data?.head?.data?.misc?.artist!!)
            tvSubTitle.text = getString(R.string.playlist_str_2) + " " + artist
        } else {
            tvSubTitle.text = ""
            tvSubTitle.hide()
//            tvSubTitle.hide()
        }


        if (it?.data?.head?.data?.misc != null) {
            var subtitle = ""
            if (!TextUtils.isEmpty(it.data?.head?.data?.misc?.f_playcount)) {
                subtitle += it.data?.head?.data?.misc?.f_playcount + " " + context?.getString(R.string.podcast_str_7) + " • "
            }
            if (!TextUtils.isEmpty(it.data?.head?.data?.misc?.f_FavCount)) {
                subtitle += it.data?.head?.data?.misc?.f_FavCount + " " + context?.getString(R.string.podcast_str_8)
            }
            tvSubTitle.text = subtitle
        } else {
            tvSubTitle.text = ""
        }
        tvSubTitle.hide()

        if (it?.data?.head?.data?.misc?.description != null && !TextUtils.isEmpty(it.data?.head?.data?.misc?.description?.trim())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvReadMore.text = Html.fromHtml(
                    it.data?.head?.data?.misc?.description!!,
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else {
                tvReadMore.text = Html.fromHtml(it.data?.head?.data?.misc?.description)
            }
            SaveState.isCollapse = true
            tvReadMore.setShowingLine(2)
            tvReadMore.addShowMoreText("read more")
            tvReadMore.addShowLessText("read less")
            tvReadMore.setShowMoreColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            tvReadMore.setShowLessTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            tvReadMore.setShowMoreStyle(Typeface.BOLD)
            tvReadMore.setShowLessStyle(Typeface.BOLD)
        } else {
            tvReadMore.text = ""
        }

    }


    override fun onTabSelected(tab: TabLayout.Tab?) {
        setLog("TabSelected", "selected")
        //setUpPodcastDetailListViewModel()
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onStop() {
        super.onStop()
        setLog("PodcastDetailFragment", "onStop")
    }


    override fun onDestroy() {
        super.onDestroy()
        setLog("PodcastDetailFragment", "onDestroy-isFromVerticalPlayer-$isFromVerticalPlayer")
        setLog("PodcastDetailFragment", "onDestroy-isNewSwipablePlayerOpen-${BaseActivity.isNewSwipablePlayerOpen}")
        if (isFromVerticalPlayer){
            if (activity != null){
                BaseActivity.isNewSwipablePlayerOpen = true
                (activity as MainActivity).hideMiniPlayer()
                (activity as MainActivity).hideStickyAds()
                (activity as MainActivity).showBottomNavigationAndMiniplayerBlurView()
            }
        }
        tracksViewModel.onCleanup()
        (requireActivity() as MainActivity).removeLocalBroadcastEventCallBack()
        MainScope().launch {
            if (context != null) {
                changeStatusbarcolor(ContextCompat.getColor(requireContext(), R.color.home_bg_color))
            }
        }
    }

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        if (activity != null){
            val intent = Intent(getViewActivity(), AudioPlayerService::class.java)
            intent.action = AudioPlayerService.PlaybackControls.PLAY.name
            intent.putExtra(Constant.SELECTED_TRACK_POSITION, selectedTrackPosition)
            intent.putExtra(Constant.PLAY_CONTEXT_TYPE, Constant.PLAY_CONTEXT.LIBRARY_TRACKS)
            Util.startForegroundService(getViewActivity(), intent)
            (activity as MainActivity).reBindService()

            playPauseStatusChange(false)
        }

    }

    override fun getViewActivity(): AppCompatActivity {
        return activity as AppCompatActivity
    }

    override fun getApplicationContext(): Context {
        return (activity as AppCompatActivity).applicationContext
    }


    private fun getSeasonTrackList(seasonId:String) {
        selectedContentId=seasonId
        podcastListViewModel = ViewModelProvider(
            this
        ).get(PodcastViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getAudioBookDetailList(requireContext(), seasonId)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setLog(TAG, "setUpPodcastDetailListViewModel: getPodcastDetailList called")

                                if(it?.data!=null&& it.data.data !=null){
                                    podcastRespModel?.data?.body?.rows= it.data.data?.body?.rows!!
                                    podcastRespModel?.data?.head?.data?.misc?.episodeCount= it.data.data.head.data.misc.episodeCount

                                    try{
                                        setPodcastDetailsListData(podcastRespModel!!)
                                    }catch (e : Exception){
                                        e.printStackTrace()
                                    }
                                }


                            }

                            Status.LOADING -> {
                                setProgressBarVisible(false)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","getSeasonTrackList")
        }
    }

    var songDataList: ArrayList<Track> = arrayListOf()
    fun setPodcastSongDataList(
        playableContentModel: PlayableContentModel,
        type: String?,
        heading: String?,
        image: String?
    ) {
        val track: Track = Track()
        track.id = playableContentModel.data?.head?.headData?.id!!.trim().toLong()
        track.title = playableContentModel.data?.head?.headData?.title
        track.url = playableContentModel.data?.head?.headData?.misc?.url
        track.drmlicence = playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token
        track.playerType = type
        track.heading = heading
        track.image = image
        track.pType = DetailPages.PODCAST_DETAIL_PAGE.value
        track.contentType = ContentTypes.PODCAST.value

        track.explicit = playableContentModel.data?.head?.headData?.misc?.explicit!!
        track.restrictedDownload =
            playableContentModel.data?.head?.headData?.misc?.restricted_download!!
        track.attributeCensorRating =
            playableContentModel.data?.head?.headData?.misc?.attributeCensorRating.toString()
        track.urlKey = playableContentModel.data.head.headData.misc.urlKey

        if (!playableContentModel.data?.head?.headData?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableContentModel.data?.head?.headData?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        songDataList.add(track)
        setLog("SongData", BaseActivity.songDataList.toString())
    }


    private fun setUpPlayableContentListViewModel(id: String) {
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)
            playableContentViewModel.getPlayableContentList(requireContext(), id)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                if (!TextUtils.isEmpty(it.data.data?.head?.headData?.misc?.url)) {
                                    setPlayableContentListData(it.data)
                                } else {
                                    playableItemPosition = playableItemPosition + 1
                                    if (playableItemPosition < podcastEpisodeList.size) {
                                        setUpPlayableContentListViewModel(podcastEpisodeList.get(playableItemPosition)?.data?.id!!)
                                        setEventModelDataAppLevel(
                                            podcastEpisodeList.get(
                                                playableItemPosition
                                            )?.data?.id!!,
                                            podcastEpisodeList.get(
                                                playableItemPosition
                                            )?.data?.title!!,
                                            podcastRespModel?.data?.head?.data?.title!!,playableItemPosition
                                        )
                                    }
                                }

                            }
                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","setUpPlayableContentListViewModel")
        }
    }


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            if (!CommonUtils.checkExplicitContent(
                    requireContext(),
                    playableContentModel.data.head.headData.misc.explicit
                )
            ) {
                setLog("PlayableItem", playableContentModel.data?.head?.headData?.id.toString())
                songDataList = arrayListOf()

                for (i in podcastEpisodeList.indices) {
                    if (playableContentModel.data?.head?.headData?.id == podcastEpisodeList.get(i)?.data?.id) {
                        setPodcastEpisodeList(
                            playableContentModel,
                            podcastEpisodeList,
                            playableItemPosition
                        )
                    } else if (i > playableItemPosition) {
                        setPodcastEpisodeList(null, podcastEpisodeList, i)
                    }
                }

                setTrackListData(songDataList)
                tracksViewModel.prepareTrackPlayback(
                    0,
                    HungamaMusicApp.getInstance()
                        .getContentDuration(playableContentModel.data?.head?.headData?.id!!)!!
                )
            }
        }
    }

    fun setPodcastEpisodeList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<PlaylistModel.Data.Body.Row.Data.Misc.Track?>?,
        position: Int
    ): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            track.id = playableItem?.get(position)?.data?.id!!.trim().toLong()
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)) {
            track.title = playableItem?.get(position)?.data?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subtitle)) {
            track.subTitle = playableItem?.get(position)?.data?.subtitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }
        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()){
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        }else{
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.type.toString())) {
            track.playerType = playableItem?.get(position)?.data?.type.toString()
        } else {
            track.playerType = Constant.PLAYER_PODCAST_AUDIO_TRACK
        }

        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title)) {
            track.heading = podcastRespModel?.data?.head?.data?.title
        } else {
            track.heading = ""
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.playble_image)) {
            track.image = playableItem?.get(position)?.data?.playble_image
        }else if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.image)) {
            track.image = playableItem?.get(position)?.data?.image
        } else {
            track.image = ""
        }

        if (!TextUtils.isEmpty(selectedContentId1)) {
            track.parentId = selectedContentId1
        }
        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.title!!)) {
            track.pName = podcastRespModel?.data?.head?.data?.title
        }

        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.subtitle!!)) {
            track.pSubName = podcastRespModel?.data?.head?.data?.subtitle
        }

        if (!TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.image!!)) {
            track.pImage = podcastRespModel?.data?.head?.data?.image
        }

        track.pType = DetailPages.PODCAST_DETAIL_PAGE.value
        track.contentType = ContentTypes.PODCAST.value

        if (playableItem?.get(position)?.data?.misc?.explicit != null) {
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem?.get(position)?.data?.misc?.restricted_download != null) {
            track.restrictedDownload = playableItem.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null) {
            track.attributeCensorRating =
                playableItem.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }

        songDataList.add(track)
        return songDataList
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.heading)
        setLog(TAG, "onMoreClick:selectedMoreBucket "+selectedMoreBucket?.image)
        val moreBucketListFragment = MoreBucketListFragment()
        moreBucketListFragment.arguments = bundle
        addFragment(R.id.fl_container, this, moreBucketListFragment, false)

        val dataMap= HashMap<String,String>()
        dataMap.put(EventConstant.BUCKETNAME_EPROPERTY,""+selectedMoreBucket?.heading)
        dataMap.put(EventConstant.CONTENT_TYPE_EPROPERTY,""+ podcastRespModel?.data?.head?.data?.title)

        dataMap.put(EventConstant.SOURCEPAGE_EPROPERTY,""+Utils.getContentTypeDetailName(""+selectedMoreBucket?.type))
        EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
    }

    override fun onScrollChanged() {
        if (isAdded) {

            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_382)
            /* how much we have scrolled */
            val movement = scrollView.scrollY

            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63)
            if (movement >= maxDistance) {
                //setLog("OnNestedScroll-m", movement.toString())
                //setLog("OnNestedScroll-d", maxDistance.toString())
                headBlur.visibility = View.INVISIBLE
                llToolbar.visibility = View.VISIBLE
                if (artworkProminentColor == 0) {
                    rlHeading.setBackgroundColor(resources.getColor(R.color.home_bg_color))
                } else {
                    rlHeading.setBackgroundColor(artworkProminentColor)
                }
            } else {
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
            /*finally calculate the alpha factor and set on the view */
            /* val alphaFactor: Float =
                 movement * 1.0f / (maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_63))
             if (movement >= 0 && movement <= maxDistance) {
                 *//*for image parallax with scroll *//*
                // iv_collapsingImageBg.setTranslationY((-movement / 2).toFloat())
                *//* set visibility *//*
                //toolbar.setAlpha(alphaFactor)
                //llToolbar.alpha = alphaFactor

                if (alphaFactor > 1) {
                    headBlur.visibility = View.VISIBLE
                    llToolbar.visibility = View.VISIBLE
                    rlHeading.setBackgroundColor(artworkProminentColor)
                    setLog("aaaaa1", alphaFactor.toString())
                } else {
                    llToolbar.visibility = View.INVISIBLE
                    headBlur.visibility = View.INVISIBLE
                    rlHeading.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.transparent
                        )
                    )
                    setLog("aaaaa2", alphaFactor.toString())
                }
            }*/

            /*val view = scrollView.getChildAt(scrollView.getChildCount() - 1) as View

            val diff: Int = view.bottom - (scrollView.getHeight() + scrollView
                .getScrollY())
            if (diff == 0){
                if (podcastEpisodeList?.size!! < podcastRespModel?.data?.head?.data?.misc?.episodeCount!! && !isLoading && !isLastPage){
                    isLoading = true
                    //you have to call loadmore items to get more data
                    page++
                    getMoreEpisode()
                }
            }*/
        }
    }

    private fun getMoreEpisode() {
        podcastListViewModel = ViewModelProvider(
            this
        ).get(PodcastViewModel::class.java)


        if (ConnectionUtil(context).isOnline) {
            podcastListViewModel?.getAudioBookStoryEpisodeList(requireContext(), podcastRespModel?.data?.head?.data?.id.toString(), page, podcastEpisode?.data?.type == 113)
                ?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setProgressBarVisible(false)
                                if (it?.data != null) {
                                    setEpisodeList(it.data)
                                }

                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
                                setEmptyVisible(false)
                                setProgressBarVisible(false)
                                Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                            }
                        }
                    })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","getMoreEpisode")
        }
    }


    private fun setEpisodeList(podcastDetailsRespModel: PlaylistDynamicModel) {
        isLoading = false
        val episodeList = ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track>()
        episodeList.addAll(podcastEpisodeList)
        episodeList.addAll(podcastDetailsRespModel.data.body.rows.get(0).data.misc.tracks)
        podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks = episodeList
        podcastEpisodeList = episodeList
        if (podcastEpisodeList.size >= podcastRespModel?.data?.head?.data?.misc?.episodeCount!!) {
            isLastPage = true
            rlShowMore.visibility = View.GONE
        }
        Handler(Looper.getMainLooper()).post {
            //episodeAdpter!!.notifyDataSetChanged()
            setPodcastEpisodeAdapter(ascending)
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2) {
            setLog(TAG, "onUserClick: is call")
            if (podcastRespModel!=null){
                commonThreeDotMenuItemSetup(AUDIO_BOOK_STORY_DETAIL_PAGE, this, false, null,2)
                setLog(TAG, "onUserClick: isFavorite" + isFavorite)
            }
        } else if (id == llSample.id) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireContext(), llSample!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            } catch (e: Exception) {

            }
            if (podcastRespModel != null && !TextUtils.isEmpty(podcastRespModel?.data?.head?.data?.misc?.trailer)) {
                val songsList = CommonUtils.getVideoDummyData2("https://hunstream.hungama.com/c/5/481/3d4/48090348/48090348_,100,400,750,1000,1600,.mp4.m3u8?rtLFaR4wQhnQIwZj-gbvlKvXi6fnpm8zqQD_AVZHY1bwN0aPUIi99NRWCgtfsYx_4rANuyEvwF6-l4O1vfy8khCL2v6l-9IL1Knc0y-Oc_WoL5hQeTmyi3HxvwLA")
                val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                val serviceBundle = Bundle()
                serviceBundle.putParcelableArrayList(Constant.ITEM_KEY, songsList)
//                serviceBundle.putParcelableArrayList(Constant.SEASON_LIST, seasonList)
//                serviceBundle.putBoolean(Constant.IS_MUTE, Constant.isMute)
                serviceBundle.putString(Constant.LIST_TYPE, Constant.VIDEO_LIST)
                serviceBundle.putString(Constant.heading, "${podcastRespModel?.data?.head?.data?.title} - Trailer")
                serviceBundle.putString(Constant.Trailer_url, podcastRespModel?.data?.head?.data?.misc?.trailer)
                serviceBundle.putString(
                    Constant.SELECTED_CONTENT_ID,podcastRespModel?.data?.head?.data?.id)
                serviceBundle.putInt(Constant.CONTENT_TYPE, Constant.CONTENT_TV_SHOW)
                podcastRespModel?.data?.head?.data?.type?.let { serviceBundle.putInt(Constant.TYPE_ID, it) }
                intent.putExtra(Constant.BUNDLE_KEY, serviceBundle)
                intent.putExtra(
                    "thumbnailImg",
                    podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.get(0)?.data?.image
                )
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
/*                serviceBundle.putLong(
                    Constant.VIDEO_START_POSITION, TimeUnit.SECONDS.toMillis(
                        HungamaMusicApp.getInstance().getContentDuration(
                            seasonList.get(0).data.misc.tracks.get(recent_episode.toInt()).data.id
                        )!!
                    )
                )*/
                (requireActivity() as MainActivity).setLocalBroadcastEventCall(
                    this@AudioBookDetailsFragment,
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
                    playPauseStatusChange(true)
                }
                startActivity(intent)
            }
        } else if (id == llListen.id || id == llListenActionBar.id) {

            if (!CommonUtils.isUserHasGoldSubscription()){
                if (podcastEpisodeList.isEmpty() || podcastEpisodeList == null)
                    return
                if (CommonUtils.checkMovieRight(podcastEpisodeList.get(playableItemPosition).data.misc.movierights)) {
                    CommonUtils.openSubscriptionDialogPopup(
                        requireContext(),
                        PlanNames.SVOD.name,
                        "",
                        true,
                        null,
                        "",
                        null, Constant.drawer_streaming_podcast
                    )
                    return
                }
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(
                        requireContext(), llListen,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            } catch (e: Exception) {

            }
            if (podcastRespModel != null && podcastRespModel?.data?.body?.rows?.get(0)?.data?.misc?.tracks?.size!! > 0) {
                playAllPodcast()
            }
        }
    }

    private fun setupUserViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)


        getUserSocialData()
    }

    private fun setFollowUnFollow(data: PlaylistModel.Data.Head.Data) {
        if (ConnectionUtil(context).isOnline) {
            isFollowing = !isFollowing
            val jsonObject = JSONObject()
            jsonObject.put("followingId", data.id)
            jsonObject.put("follow", isFollowing)
            userViewModel?.followUnfollowSocial(requireContext(), jsonObject.toString())

            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId", data.id)
            jsonObject1.put("typeId", "" + data.type)
            jsonObject1.put("action", isFollowing)
            jsonObject1.put("module", Constant.MODULE_FOLLOW)
            userViewModel?.followUnfollowModule(requireContext(), jsonObject1.toString())

            setFollowingStatus()


            if(isFollowing){
                val messageModel = MessageModel(getString(R.string.artist_str_3), getString(R.string.toast_str_22),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","setFollowUnFollow")
                baseIOScope.launch {
                    val hashMap = HashMap<String, String>()
                    hashMap.put(
                        EventConstant.ACTOR_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.actorf!!)
                    )
                    hashMap.put(
                        EventConstant.ALBUMID_EPROPERTY,
                        "" + podcastRespModel?.data?.head?.data?.id
                    )
                    hashMap.put(
                        EventConstant.CATEGORY_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.category!!)
                    )
                    var newContentId=podcastRespModel?.data?.head?.data?.id!!
                    var contentIdData=newContentId.replace("playlist-","")
                    hashMap.put(
                        EventConstant.CONTENTID_EPROPERTY, "" + contentIdData
                    )
                    val contentType=podcastRespModel?.data?.head?.data?.type!!
                    setLog(
                        TAG,
                        "setAddOrRemoveFavourite: type:${Utils.getContentTypeName("" +contentType)} contentType:${contentType}"
                    )
                    hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "" + Utils.getContentTypeName("" +contentType))

                    hashMap.put(
                        EventConstant.GENRE_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.genre!!)
                    )
                    hashMap.put(
                        EventConstant.LANGUAGE_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.lang!!)
                    )
                    hashMap.put(
                        EventConstant.LYRICIST_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.lyricist!!)
                    )
                    hashMap.put(
                        EventConstant.MOOD_EPROPERTY,
                        "" + podcastRespModel?.data?.head?.data?.misc?.mood
                    )
                    hashMap.put(
                        EventConstant.MUSICDIRECTOR_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.musicdirectorf!!)
                    )
                    hashMap.put(
                        EventConstant.NAME_EPROPERTY,
                        "" + podcastRespModel?.data?.head?.data?.title
                    )
                    hashMap.put(EventConstant.PODCASTHOST_EPROPERTY, "")
                    hashMap.put(
                        EventConstant.SINGER_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.singerf!!)
                    )
                    hashMap.put(
                        EventConstant.SOURCE_EPROPERTY,
                        "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + podcastRespModel?.data?.head?.data?.title
                    )
                    hashMap.put(
                        EventConstant.TEMPO_EPROPERTY,
                        Utils.arrayToString(podcastRespModel?.data?.head?.data?.misc?.tempo!!)
                    )
                    hashMap.put(EventConstant.CREATOR_EPROPERTY, "Hungama")
                    hashMap.put(
                        EventConstant.YEAROFRELEASE_EPROPERTY,
                        "" + DateUtils.convertDate(
                            DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS,
                            DateUtils.DATE_YYYY,
                            podcastRespModel?.data?.head?.data?.releasedate
                        )
                    )
                    EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
                }
            }else{
                val messageModel = MessageModel(getString(R.string.artist_str_18), getString(R.string.toast_str_24),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","setFollowUnFollow")
            }
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","setFollowUnFollow")
        }
    }

    private fun getUserSocialData() {
        if (ConnectionUtil(requireContext()).isOnline) {

            userViewModel?.getUserFollowData(requireContext(), SharedPrefHelper.getInstance().getUserId()!!,"5")?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                fillUIFollow(it.data)
                            }

                        }

                        Status.LOADING -> {
                            setProgressBarVisible(false)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(requireContext(), messageModel,"PodcastDetailsFragment","getUserSocialData")
        }
    }

    private fun fillUIFollow(data: FollowModel?) {
        this.userFollowData = data
        baseIOScope.launch {
            if (isAdded && context != null) {
                if (userFollowData != null && userFollowData?.data?.body?.rows != null){
                    for (following in userFollowData?.data?.body?.rows?.iterator()!!){
                        if (null != following) {
                            if (podcastRespModel?.data?.head?.data?.id.equals(following.data?.id)) {
                                isFollowing = true
                            }
                        }
                    }
                    setFollowingStatus()
                }
            }
        }
    }

    fun setFollowingStatus(){
        baseMainScope.launch {
            if (isAdded && context != null) {
                if (isFollowing){
                    tvFollow?.text = getString(R.string.profile_str_5)
                    tvFollowActionBar?.text = getString(R.string.profile_str_5)
                    ivFollow?.setImageDrawable(requireContext().faDrawable(R.string.icon_following, R.color.colorWhite))
                    ivFollowActionBar?.setImageDrawable(requireContext().faDrawable(R.string.icon_following, R.color.colorWhite))
                }else{
                    tvFollow?.text = getString(R.string.profile_str_2)
                    tvFollowActionBar?.text = getString(R.string.profile_str_2)
                    ivFollow?.setImageDrawable(requireContext().faDrawable(R.string.icon_follow, R.color.colorWhite))
                    ivFollowActionBar?.setImageDrawable(requireContext().faDrawable(R.string.icon_follow, R.color.colorWhite))
                }
            }
        }

    }


    override fun onDownloadQueueItemChanged(data: Download, reason: Reason) {
        baseIOScope.launch {
            setLog("DWProgrss-onChangedid", data.id.toString())
            setLog("DWProgrss-onChanged", reason.toString())
            val downloadQueue = AppDatabase.getInstance()?.downloadQueue()?.findByDownloadManagerId(data.id)
            val downloadedAudio =
                AppDatabase.getInstance()?.downloadedAudio()?.findByDownloadManagerId(data.id)

            when (reason) {
                Reason.DOWNLOAD_ADDED -> {
                    setLog("DWProgrss-ADDED", data.id.toString())
                }
                Reason.DOWNLOAD_QUEUED -> {
                    setLog("DWProgrss-QUEUED", data.id.toString())
                    if (episodeAdpter != null) {
                        if (downloadQueue != null && downloadQueue.parentId != null && downloadQueue.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList.indexOfFirst {
                                it.data?.id == downloadQueue.contentId
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        } else if (downloadedAudio != null && downloadedAudio.parentId != null && downloadedAudio.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList.indexOfFirst {
                                it.data?.id == downloadedAudio.contentId!!
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        }
                    }
                }
                Reason.DOWNLOAD_STARTED -> {
                    setLog("DWProgrss-STARTED", data.id.toString())
                    if (episodeAdpter != null) {
                        if (downloadQueue != null && downloadQueue.parentId != null && downloadQueue.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList.indexOfFirst {
                                it.data?.id == downloadQueue.contentId
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        } else if (downloadedAudio != null && downloadedAudio.parentId != null && downloadedAudio.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList.indexOfFirst {
                                it.data?.id == downloadedAudio.contentId!!
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        }
                    }
                }
                Reason.DOWNLOAD_PROGRESS_CHANGED -> {
                    setLog("DWProgrss-CHANGED", data.id.toString())
                }
                Reason.DOWNLOAD_RESUMED -> {
                    setLog("DWProgrss-RESUMED", data.id.toString())
                }
                Reason.DOWNLOAD_PAUSED -> {
                    setLog("DWProgrss-PAUSED", data.id.toString())
                }
                Reason.DOWNLOAD_COMPLETED -> {
                    setLog("DWProgrss-COMPLETED", data.id.toString())
                    if (episodeAdpter != null) {
                        if (downloadQueue != null && downloadQueue.parentId != null && downloadQueue.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList.indexOfFirst {
                                it.data?.id == downloadQueue.contentId
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        } else if (downloadedAudio != null && downloadedAudio.parentId != null && downloadedAudio.parentId?.equals(
                                podcastRespModel?.data?.head?.data?.id!!
                            )!!
                        ) {
                            val index = podcastEpisodeList.indexOfFirst {
                                it.data?.id == downloadedAudio.contentId!!
                            }
                            withContext(Dispatchers.Main){
                                if (index != null) {
                                    episodeAdpter?.notifyItemChanged(index)
                                }
                            }

                        }
                    }
                }
                Reason.DOWNLOAD_CANCELLED -> {
                    setLog("DWProgrss-CANCELLED", data.id.toString())
                }
                Reason.DOWNLOAD_REMOVED -> {
                    setLog("DWProgrss-REMOVED", data.id.toString())
                }
                Reason.DOWNLOAD_DELETED -> {
                    setLog("DWProgrss-DELETED", data.id.toString())
                }
                Reason.DOWNLOAD_ERROR -> {
                    setLog("DWProgrss-ERROR", data.id.toString())
                }
                Reason.DOWNLOAD_BLOCK_UPDATED -> {
                    setLog("DWProgrss-UPDATED", data.id.toString())
                }
                Reason.DOWNLOAD_WAITING_ON_NETWORK -> {
                    setLog("DWProgrss-NETWORK", data.id.toString())
                }

                else -> {}
            }
        }

    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        (requireActivity() as MainActivity).addOrUpdateDownloadMusicQueue(
            ArrayList(),
            this,
            null,
            true,
            false
        )
        if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList.size > 0) {
            checkAllContentDownloadedOrNot(podcastEpisodeList)
        } else {
            playPauseStatusChange(true)
        }
        setLog("PodcastDetailFragment", "onResume-isFromVerticalPlayer-$isFromVerticalPlayer")
        setLog("PodcastDetailFragment", "onResume-isNewSwipablePlayerOpen-${BaseActivity.isNewSwipablePlayerOpen}")
        BaseActivity.isNewSwipablePlayerOpen = false
        showBottomNavigationAndMiniplayer()
    }

    override fun onUserSubscriptionUpdateCall(status: Int, contentId: String) {

    }

    override fun playItemChange() {
        if (episodeAdpter != null && isVisible) {
            setLog(
                "TAG",
                "bind: content id : play track id: ${(activity as BaseActivity).fetchTrackData().id} title:${(activity as BaseActivity).fetchTrackData().title}"
            )

            episodeAdpter?.notifyDataSetChanged()
        }
    }

    private fun checkAllContentDownloadedOrNot(podcastEpisodeList: List<PlaylistModel.Data.Body.Row.Data.Misc.Track?>) {
        if (isAdded){
            var isCurrentContentPlayingFromThis = false
            if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList.size > 0) {
                if (podcastRespModel != null && podcastRespModel?.data?.head?.data != null) {
                    var index = 0
                    for (item in podcastEpisodeList.iterator()) {
                        if (!isCurrentContentPlayingFromThis && !BaseActivity.songDataList.isNullOrEmpty()
                            && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                        ) {
                            val currentPlayingContentId =
                                BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                            if (currentPlayingContentId?.toString()?.equals(item?.data?.id)!!) {
                                isCurrentContentPlayingFromThis = true
                                if (episodeAdpter != null) {
                                        episodeAdpter?.notifyDataSetChanged()
                                }
                                if (activity != null){
                                    if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                        playPauseStatusChange(false)
                                    } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                                playPauseStatusChange(true)
                                    } else {
                                        playPauseStatusChange(true)
                                    }
                                }

                            } else {
                                playPauseStatusChange(true)
                            }
                        }
                        index++
                    }
                }
            }
        }
    }

    private fun playPauseStatusChange(status: Boolean) {
        isPlaying = status
        if (status) {
            ivListen?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_play_2,
                    R.color.colorWhite
                )
            )
            tvListenActionBar?.text = getString(R.string.audio_books_str_detail_page_cta1)
            ivListenActionBar?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_play_2,
                    R.color.colorWhite
                )
            )
            tvListen?.text = getString(R.string.audio_books_str_detail_page_cta1)
        } else {
            ivListen?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_pause,
                    R.color.colorWhite
                )
            )
            tvListen?.text = getString(R.string.general_str)
            ivListenActionBar?.setImageDrawable(
                requireContext().faDrawable(
                    R.string.icon_pause,
                    R.color.colorWhite
                )
            )
            tvListenActionBar?.text = getString(R.string.general_str)
        }
    }

    private fun setLocalBroadcast() {
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(
            this,
            Constant.AUDIO_PLAYER_EVENT
        )
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded) {
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList.size > 0) {
                    checkAllContentDownloadedOrNot(podcastEpisodeList)
                } else {
                    playPauseStatusChange(true)
                }

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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setLog("podcastLifecycle", "onHiddenChanged-$hidden")
        if (!hidden) {
            podcastRespModel = tempPodcastRespModel
            podcastEpisode = tempPodcastEpisode
            if (!podcastEpisodeList.isNullOrEmpty() && podcastEpisodeList.size > 0) {
                checkAllContentDownloadedOrNot(podcastEpisodeList)
            } else {
                playPauseStatusChange(true)
            }
            MainScope().launch {
                if (context != null) {
                    setLog(
                        "ChartLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
            }
        } else {
            tempPodcastRespModel = podcastRespModel
            tempPodcastEpisode = podcastEpisode
            MainScope().launch {
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

    private fun getAdsData(podcastList: List<PlaylistModel.Data.Body.Row.Data.Misc.Track>): ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track> {
        val podEpisodeList: ArrayList<PlaylistModel.Data.Body.Row.Data.Misc.Track> =
            ArrayList()
        if (!podcastList.isNullOrEmpty()){
            podEpisodeList.addAll(podcastList)
            if (CommonUtils.isDisplayAds() && CommonUtils.getFirebaseConfigAdsData().podcastDetailsPageNativeAd.displayAd) {
                val adDisplayFirstPosition =
                    CommonUtils.getFirebaseConfigAdsData().podcastDetailsPageNativeAd.firstAdPositionAfterEpisodes
                val adDisplayPositionFrequency =
                    CommonUtils.getFirebaseConfigAdsData().podcastDetailsPageNativeAd.repeatFrequencyAfterRows
                var adDisplayPosition = adDisplayFirstPosition
                //val adDisplayPosition = 4
                var isFirstAds = true
                val adUnitIdList = arrayListOf(
                    Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_1,
                    Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_2,
                    Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_3,
                    Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_4,
                    Constant.AD_MANAGER_NATIVE_AD_UNIT_ID_5
                )
                val adTotalIds = adUnitIdList.size
                var adIdCount = 0
                var i = 0
                var k = 0
                val iterator = podEpisodeList.listIterator()
                while (iterator.hasNext()) {
                    //setLog("adInserted-1", i.toString())

                    if (k > 0 && k % adDisplayPosition == 0) {
                        if (isFirstAds) {
                            k = 0
                            isFirstAds = false
                            adDisplayPosition = adDisplayPositionFrequency
                        }
                        //setLog("adInserted-2", i.toString())
                        //setLog("adInserted", "Befor==" + homeModel.data?.body?.rows?.get(i)?.heading)

                        val podcastRow = PlaylistModel.Data.Body.Row.Data.Misc.Track()
                        podcastRow.itype = Constant.podcastNativeAds

                        if (adTotalIds > adIdCount) {
                            //setLog("adInserted-3", adIdCount.toString())
                            //setLog("adInserted-3", adUnitIdList.get(adIdCount))
                            podcastRow.adUnitId = adUnitIdList.get(adIdCount)
                            adIdCount++
                        } else {
                            adIdCount = 0
                            podcastRow.adUnitId = adUnitIdList.get(adIdCount)
                            //setLog("adInserted-4", adIdCount.toString())
                            //setLog("adInserted-4", adUnitIdList.get(adIdCount))
                            adIdCount++
                        }

                        iterator.add(podcastRow)
                    }
                    val item = iterator.next()
                    i++
                    k++
                }
            }
        }
        return podEpisodeList
    }

    override fun onContentLikedFromThreeDotMenu(isFavorite: Boolean, position: Int) {
        super.onContentLikedFromThreeDotMenu(isFavorite, position)
        if (podcastEpisodeList != null && podcastEpisodeList.size > 0) {
            podcastEpisodeList.get(position)?.data?.isFavorite = isFavorite
        }
    }
    private fun redirectToMoreBucketListPage(bodyRowsItemsItem: ArrayList<BodyRowsItemsItem?>?, heading: String) {
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

}