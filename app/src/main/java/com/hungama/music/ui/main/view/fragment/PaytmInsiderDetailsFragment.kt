package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.Util
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.player.audioplayer.Injection
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.services.AudioPlayerService
import com.hungama.music.player.audioplayer.viewmodel.TracksViewModel
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.*
import com.hungama.music.ui.main.view.activity.LoginMainActivity
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.view.activity.PaytmInsiderWebviewActivity
import com.hungama.music.ui.main.viewmodel.*
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.customview.SaveState
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.common_details_page_back_menu_header.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.*
import kotlinx.android.synthetic.main.fragment_artist_details.*
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.*
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.ivPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_chart_detail_v2.tvPlayAllActionBar
import kotlinx.android.synthetic.main.fragment_event_detail.*
import kotlinx.android.synthetic.main.fragment_movie_v1.*
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.*
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.artistAlbumArtImageView
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.headBlur
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llPlayAllArtist
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.movieDetailroot
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.scrollView
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.shimmerLayout
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.tvTitle
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llDetails2
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.rlHeading
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llHeaderAlbums
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.tvDate
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.rvAlbums
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llAlbums
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.tvHeadingAlbums
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.rvSongs
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llHeaderSongs
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llSongs
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llMyTicketButtons
import kotlinx.android.synthetic.main.fragment_paytm_insider_details.llDescription
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

/**
 * A simple [Fragment] subclass.
 * Use the [PaytmInsiderDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PaytmInsiderDetailsFragment : BaseFragment(), OnParentItemClickListener, TracksContract.View,
    ViewTreeObserver.OnScrollChangedListener, BaseActivity.OnLocalBroadcastEventCallBack,
    BucketParentAdapter.OnMoreItemClick {
    var artImageUrl: String? = null
    lateinit var eventTopAdapter:PaytmEventTopSongAdapter
    //var artImageUrl = "https://files.hubhopper.com/podcast/313123/storytime-with-gurudev-sri-sri-ravi-shankar.jpg?v=1598432706&s=hungama"
    var selectedContentId: String? = null
    var playerType: String? = null

    var artistViewModel: ArtistViewModel? = null
    var fragmentName: ArrayList<String> = ArrayList()
    var fragmentList: ArrayList<Fragment> = ArrayList()

    var bucketRespModel: HomeModel? = null
    var paytmInsiderRespModel = PaytmInsiderDetailModel()
    var rowList: MutableList<RowsItem?>? = null
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null

    var topSongList: ArrayList<BodyRowsItemsItem?>? = null
    var topSongIndex = -1
    var selectedPodcast: RowsItem? = null
    var selectedPodcastChildPosition: Int? = null
    private var tracksViewModel: TracksContract.Presenter? = null
    var selectedArtistSongImage = ""
    var selectedArtistSongTitle = ""
    var selectedArtistSongSubTitle = ""
    var selectedArtistSongHeading = ""
    var playableItemPosition = 0
    var playableContentViewModel: PlayableContentViewModel = PlayableContentViewModel()
    var artworkProminentColor = 0
    var userViewModel: UserViewModel? = null
    var isFollowing = false
    var userSocialData: UserSocialData? = null
    var userFollowData: FollowModel? = null
    var isMorePage = false
    var morePageName = ""
    var Follow = false
    private var bucketParentAdapter: BucketParentAdapter? = null
    private var artistListAdapter: ArtistListAdapter? = null
    private var planListAdapter: PlanListAdapter? = null

    companion object {
        var paytmInsiderModel: PaytmInsiderDetailModel? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Constant.paytminsider_redirection = false
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paytm_insider_details, container, false)
    }

    override fun initializeComponent(view: View) {
        baseMainScope.launch {
            hideBottomNavigationAndMiniplayer()
            if (arguments != null) {
                //        artImageUrl =requireArguments().getString("image").toString()
                if (requireArguments().containsKey(Constant.defaultContentId)) {
                    selectedContentId =
                        requireArguments().getString(Constant.defaultContentId).toString()
                }
                if (requireArguments().containsKey(Constant.EXTRA_IS_MORE_PAGE)) {
                    isMorePage = requireArguments().getBoolean(Constant.EXTRA_IS_MORE_PAGE, false)
                }
                if (requireArguments().containsKey(Constant.EXTRA_MORE_PAGE_NAME)) {
                    morePageName =
                        requireArguments().getString(Constant.EXTRA_MORE_PAGE_NAME).toString()
                }
            }
            tvHeader.text = ""
            scrollView?.hide()
            shimmerLayout?.show()
            shimmerLayout?.startShimmer()
            ivBack?.setOnClickListener { view -> backPress() }
            ivBack2?.setOnClickListener { view -> backPress() }

            rlMyTicket?.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(Constant.tabName, "Purchases")
                bundle.putBoolean(Constant.isTabSelection, true)
                bundle.putBoolean("PaytmDetail", true)
                Constant.paytminsider_redirection = false
                val hashMapPageView = HashMap<String, String>()
                hashMapPageView[EventConstant.BUTTONNAME_EPROPERTY] = "View Ticket"
                EventButtonClickFunction(hashMapPageView,tv_view_ticket.text.toString())
                val fragment = LibraryMainTabFragment.newInstance(requireContext(), bundle)
                addFragment(R.id.fl_container, this@PaytmInsiderDetailsFragment, fragment, false)
                (activity as MainActivity).setLastClickedBottomMenu(Constant.BOTTOM_NAV_LIBRARY, 4)
            }

            scrollView?.viewTreeObserver?.addOnScrollChangedListener(this@PaytmInsiderDetailsFragment)

            setupArtistViewModel()
            tracksViewModel = TracksViewModel(
                Injection.provideTrackRepository(),
                this@PaytmInsiderDetailsFragment
            )
            userSubscriptionViewModel =
                ViewModelProvider(requireActivity()).get(UserSubscriptionViewModel::class.java)
            llPlayAllArtist?.setOnClickListener {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        CommonUtils.hapticVibration(
                            requireContext(), llPlayAllArtist!!,
                            HapticFeedbackConstants.CONTEXT_CLICK
                        )
                    }
                } catch (e: Exception) {

                }
                loginRedirection()

            }

            rlBuyMore?.setOnClickListener {

                if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                    val hashMapPageView = HashMap<String, String>()
                    hashMapPageView[EventConstant.BUTTONNAME_EPROPERTY] = "Buy More"
                    EventButtonClickFunction(hashMapPageView, tv_buy_ticket.text.toString())

                    var intent = Intent(requireContext(), PaytmInsiderWebviewActivity::class.java)
                            .putExtra(
                                Constant.EXTRA_URL,
                                paytmInsiderModel?.data?.head?.event?.webviewUrlAndroid
                            )
                            .putExtra(
                                EventConstant.EVENTID,
                                paytmInsiderModel?.data?.head?.event?.id
                            )
                            .putExtra(
                                EventConstant.EVENTNAME,
                                paytmInsiderModel?.data?.head?.event?.eventName
                            )
                        .putExtra(
                            EventConstant.SOURCE,
                            paytmInsiderModel?.data?.head?.event?.eventName
                        )

                        startActivityForResult(intent,Constant.payWebRedirect);
                }else{
                    val intent = Intent(requireActivity(), LoginMainActivity::class.java)
                    intent.putExtra("action", Constant.SIGNIN_WITH_ALL)
                    intent.putExtra("isForAudio", true)
                    startActivityForResult(intent,Constant.loginredirect);
                }
            }

            threeDotMenu.setBackgroundResource(R.drawable.ic_share_image)
            threeDotMenu.layoutParams.width =
                requireContext().resources.getDimensionPixelSize(R.dimen.dimen_18)
            threeDotMenu.layoutParams.height =
                requireContext().resources.getDimensionPixelSize(R.dimen.dimen_23)

            val param = threeDotMenu.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(
                0,
                0,
                requireContext().resources.getDimensionPixelSize(R.dimen.dimen_20),
                0
            )
            threeDotMenu.layoutParams = param

            threeDotMenu?.setOnClickListener(this@PaytmInsiderDetailsFragment)
            threeDotMenu2?.setOnClickListener(this@PaytmInsiderDetailsFragment)
//            llFollow?.setOnClickListener(this@PaytmInsiderDetailsFragment)
            tvLocation?.setOnClickListener(this@PaytmInsiderDetailsFragment)
//            llFollowActionBar?.setOnClickListener(this@PaytmInsiderDetailsFragment)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Constant.loginredirect){
            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                loginRedirection()
            }
        }else if(requestCode==Constant.payWebRedirect && Constant.paytminsider_redirection){
              showBottomNavigationAndMiniplayer()
        }
    }

    private fun staticToolbarColor() {
        //(activity as AppCompatActivity).window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
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
        baseIOScope.launch {
            try {
                if (activity != null && artImageUrl != null && !TextUtils.isEmpty(artImageUrl) && URLUtil.isValidUrl(
                        artImageUrl
                    ) && movieDetailroot != null
                ) {
                    val result: Deferred<Bitmap?> = baseIOScope.async {
                        val urlImage = URL(artImageUrl)
                        urlImage.toBitmap()
                    }

                    baseIOScope.launch(Dispatchers.IO) {
                        try {
                            // get the downloaded bitmap
                            val bitmap: Bitmap? = result.await()
                            val artImage = BitmapDrawable(resources, bitmap)
                            if (status) {
                                if (bitmap != null) {
                                    //val color = dynamicToolbarColor(bitmap)
                                    Palette.from(bitmap!!).generate { palette ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            artworkProminentColor =
                                                CommonUtils.calculateAverageColor(bitmap, 1)
                                            baseMainScope.launch {
                                                if (context != null) {
                                                    CommonUtils.setLog(
                                                        "ArtistLifecycle",
                                                        "setArtImageBg--$artworkProminentColor"
                                                    )
                                                    changeStatusbarcolor(artworkProminentColor)
                                                }
                                            }
                                        }
                                    }

                                }

                            }
                        } catch (exp: Exception) {
                            exp.printStackTrace()
                        }


                    }
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }

    }

    var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            CommonUtils.setLog("onPageSelected", "Selected position:" + position)
        }
    }

    private fun setupArtistViewModel() {
        try {
            if (isAdded && context != null) {
                if (ConnectionUtil(context).isOnline) {
                    artistViewModel = ViewModelProvider(this).get(ArtistViewModel::class.java)

                    artistViewModel?.getPaytmInsiderDetail(
                        requireContext(),
                        selectedContentId.toString())?.observe(this)
                    {
                        when (it.status) {
                            Status.SUCCESS -> {
                                setupUserViewModel()
                                fillArtistDetail(it?.data!!)
                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
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
                    }
                } else {
                    val messageModel = MessageModel(
                        getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                        MessageType.NEGATIVE, true
                    )
                    CommonUtils.showToast(
                        requireContext(),
                        messageModel,
                        "ArtistDetailsFragment",
                        "setupArtistViewModel"
                    )
                }
            }

        } catch (e: Exception) {

        }
    }

    fun loginRedirection(){
        if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
            val hashMapPageView = HashMap<String, String>()
            hashMapPageView[EventConstant.BUTTONNAME_EPROPERTY] = "Buy Now"
            EventButtonClickFunction(hashMapPageView, tvBuyNow.text.toString())

                var intent = Intent(requireContext(), PaytmInsiderWebviewActivity::class.java)
                    .putExtra(
                        Constant.EXTRA_URL,
                        paytmInsiderModel?.data?.head?.event?.webviewUrlAndroid
                    ).putExtra(
                        EventConstant.EVENTID,
                        paytmInsiderModel?.data?.head?.event?.id
                    )
                    .putExtra(
                        EventConstant.EVENTNAME,
                        paytmInsiderModel?.data?.head?.event?.eventName
                    )
                    .putExtra(
                        EventConstant.SOURCE,
                        paytmInsiderModel?.data?.head?.event?.eventName
                    )

                  startActivityForResult(intent,Constant.payWebRedirect);
        }else{
            val intent = Intent(requireActivity(), LoginMainActivity::class.java)
            intent.putExtra("action", Constant.SIGNIN_WITH_ALL)
            intent.putExtra("isForAudio", true)
            startActivityForResult(intent,Constant.loginredirect);

        }
    }

    fun getYouMayLikeCall(homedata: PaytmInsiderDetailModel?) {
        try {
            if (isAdded && context != null) {
                val homeViewModel = ViewModelProvider(
                    this
                ).get(HomeViewModel::class.java)
                val url = WSConstants.METHOD_YOU_MAY_LIKE_ARTIST + "?contentId=" + selectedContentId
                homeViewModel?.getTrendingPodcastList(requireContext(), url)?.observe(this,
                    Observer {
                        when (it.status) {
                            Status.SUCCESS -> {
                                var dailyDoseIndex = 0
                                /*      var podcastSize = homedata?.data?.body?.recomendation?.size
                                      if (it?.data != null && it?.data?.data?.body != null && it?.data?.data?.body?.searchRecommendations?.size!! > 0) {
                                          it?.data?.data?.body?.searchRecommendations?.forEach {
                                              if (podcastSize != null) {
                                                  homedata?.data?.body?.recomendation?.add(
                                                      podcastSize + dailyDoseIndex,
                                                      it
                                                  )
                                              }
                                              dailyDoseIndex += 1

                                              CommonUtils.setLog(
                                                  TAG,
                                                  "getYouMayLikePodcastCall: dailyDoseIndex:${dailyDoseIndex} "
                                              )
                                          }
                                      }*/
                                fillArtistDetail(homedata!!)
                            }

                            Status.LOADING -> {
                                setProgressBarVisible(true)
                            }

                            Status.ERROR -> {
                                setProgressBarVisible(false)
                                Utils.showSnakbar(
                                    requireContext(),
                                    requireView(),
                                    true,
                                    it.message!!
                                )
                                fillArtistDetail(homedata!!)
                            }
                        }
                    })
            }
        } catch (e: Exception) {

        }
    }

    private fun fillArtistDetail(data: PaytmInsiderDetailModel) {
        setMovieTrailerListData(data)
        getRentedMovieList(data.data.head.event.id)
        setDetails(data, true)
    }

    fun getRentedMovieList(eventId: String) {
        baseMainScope.launch {
            if (ConnectionUtil(requireContext()).isOnline) {
                userSubscriptionViewModel?.getOfflineEventData(requireContext())
                    ?.observe(requireActivity(),
                        Observer {
                            when (it.status) {
                                Status.SUCCESS -> {
                                    setProgressBarVisible(false)
                                    if (it?.data != null && it.data.success) {
                                        setLog(TAG, "isViewLoading $it")
                                        if (llMyTicketButtons != null)
                                            llMyTicketButtons.hide()
                                        if (llPlayAllArtist != null)
                                            llPlayAllArtist.show()
                                        for (item in it?.data.data.offlineEvent) {
                                            if (eventId == item.eventId) {
                                                if (llMyTicketButtons != null)
                                                    llMyTicketButtons.show()
                                                if (llPlayAllArtist != null)
                                                    llPlayAllArtist.hide()
                                                break
                                            }
                                        }
                                    } else {
                                        if (isAdded)
                                            Utils.showSnakbar(
                                                requireContext(),
                                                requireView(),
                                                false,
                                                it?.message!!
                                            )
                                }
                                    shimmerLayout?.hide()
                                    rlHeading?.show()
                                    shimmerLayout?.stopShimmer()
                                    scrollView?.show()
                            }

                                Status.LOADING -> {
                                    setProgressBarVisible(true)
                                }

                                Status.ERROR -> {
                                    setProgressBarVisible(false)
                                    shimmerLayout?.hide()
                                    rlHeading.show()
                                    shimmerLayout?.stopShimmer()
                                    scrollView?.show()
                                }
                            }
                        })
            } else {
                val messageModel = MessageModel(
                    getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                    MessageType.NEGATIVE, true
                )
                CommonUtils.showToast(
                    requireContext(),
                    messageModel,
                    "TicketDetailsUnderPurchasesFragment",
                    "getRentedMovieList"
                )
            }
        }

    }

    private fun setDetails(it: PaytmInsiderDetailModel?, status: Boolean) {
        paytmInsiderModel = it
        if (paytmInsiderModel?.data?.head?.event?.image?.isNotEmpty() == true) {
            artImageUrl = paytmInsiderModel?.data?.head?.event?.image?.get(0)
        }
        val ticketPrice = paytmInsiderModel?.data?.head?.event?.lowestTicketPrice
        if (!ticketPrice.isNullOrEmpty()) {
            if (paytmInsiderModel?.data?.head?.event?.country == Constant.DEFAULT_COUNTRY_CODE)
                tvBuyNow?.text =
                    "${getString(R.string.buy_now_for)} ${getString(R.string.rupee_sign)}${ticketPrice} ${
                        getString(R.string.onwards)
                    }"
            else
                tvBuyNow?.text =
                    "${getString(R.string.buy_now_for)} ${getString(R.string.dollar_sign)}${ticketPrice} ${
                        getString(R.string.onwards)
                    }"
        }



        llDetails2?.show()
        playerType = "" + paytmInsiderModel?.data?.head?.event?.type
        baseMainScope.launch {
            if (isAdded && context != null) {
                if (!TextUtils.isEmpty(artImageUrl)) {
                    ImageLoader.loadImage(
                        requireContext(),
                        artistAlbumArtImageView,
                        artImageUrl!!,
                        R.drawable.bg_gradient_placeholder
                    )
                    staticToolbarColor()
//                setArtImageBg(true)
                } else {
                    ImageLoader.loadImage(
                        requireContext(),
                        artistAlbumArtImageView,
                        "",
                        R.drawable.bg_gradient_placeholder
                    )
                    staticToolbarColor()
                }
                sendViewEvent(it?.data?.head?.event)

                if (!TextUtils.isEmpty(it?.data?.head?.event?.eventName)) {
                    tvTitle?.text = it?.data?.head?.event?.eventName
                    tvHeader?.text = it?.data?.head?.event?.eventName
                } else {
                    tvTitle?.hide()
                }
                if (!TextUtils.isEmpty(it?.data?.head?.event?.eventDate))
                    tvDate.text = it?.data?.head?.event?.eventDate.toString()
                else
                    tvDate.hide()

                if (!TextUtils.isEmpty(it?.data?.head?.event?.venue)) {
                    tvAddress?.text = it?.data?.head?.event?.venue
                } else {
                    tvAddress?.hide()
                }

                if (!TextUtils.isEmpty(it?.data?.head?.event?.age)) {
                    tvAge?.text = it?.data?.head?.event?.age
                } else {
                    tvAge?.hide()
                }

                if (!TextUtils.isEmpty(it?.data?.head?.event?.language)) {
                    tvLanguage?.text = it?.data?.head?.event?.language
                } else {
                    tvLanguage?.hide()
                }

                if (!TextUtils.isEmpty(it?.data?.head?.event?.duration)) {
                    tvDuration?.text = it?.data?.head?.event?.duration
                } else {
                    tvDuration?.hide()
                }

                rvArtistList.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                artistListAdapter = it?.data?.head?.artist?.let { it1 ->
                    ArtistListAdapter(
                        requireContext(),
                        it1,
                        object : ArtistListAdapter.OnItemClick {
                            override fun onUserClick(data: PaytmInsiderDetailModel.Data.Head.Artist) {
                                setFollowUnFollow(data)
                            }
                        })
                }
                rvArtistList.adapter = artistListAdapter
                if (it?.data?.head?.artist?.isEmpty() == true)
                    tvArtist.hide()

                setLog("akdhgHS", "${llPlans.width}")

                rvPlans.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                planListAdapter = it?.data?.head?.event?.ticketPrice?.let { it1 ->
                    PlanListAdapter(
                        requireContext(),
                        it1,
                        rvPlans.width.toDouble(),
                        object : PlanListAdapter.OnItemClick {
                            override fun onUserClick(data: PaytmInsiderDetailModel.Data.Head.Artist) {
                            }
                        })
                }
                rvPlans.adapter = planListAdapter

                if (it?.data?.head?.event?.ticketPrice?.isEmpty() == true)
                    llPlans.hide()


                if (!TextUtils.isEmpty(it?.data?.head?.event?.aboutEventDesc)) {
                    tvDescription?.text = it?.data?.head?.event?.aboutEventDesc?.let { it1 ->
                        HtmlCompat.fromHtml(
                            it1,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                    }
                    SaveState.isCollapse = true
                    tvDescription?.setShowingLine(3)
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
                    tvDescription?.visibility = View.VISIBLE
                } else {
                    tvDescription?.visibility = View.GONE
                }

                if (!TextUtils.isEmpty(it?.data?.head?.event?.aboutEventTitle)){
                    tvDesTitle?.text = it?.data?.head?.event?.aboutEventTitle
                        tvDesTitle?.show()
                    }
                else
                    tvDesTitle?.hide()

                if (TextUtils.isEmpty(it?.data?.head?.event?.aboutEventDesc) && TextUtils.isEmpty(it?.data?.head?.event?.aboutEventTitle))
                    llDescription.hide()
                else
                    llDescription?.show()
            }
        }
    }

    fun setMovieTrailerListData(artistModel: PaytmInsiderDetailModel) {

        if (artistModel != null && artistModel?.data?.body != null) {
            paytmInsiderRespModel = artistModel
            setArtistAlbumData(artistModel)
            setArtistTopSongData(artistModel)

            /*
           //            setupUserViewModel()
           //            setUpAllBucket(artistModel)
                       setArtistLatestNewsData(artistModel)
                       setArtistNewReleaseData(artistModel)
                       setArtistMusicVideoData(artistModel)
                       setArtistTVShowData(artistModel)
                       setArtistUpcomingData(artistModel)
                       setArtistMerchandiseData(artistModel)
                       setArtistLikeData(artistModel)
                       setMovieData(artistModel)*/
        }
    }

    fun setArtistTopSongData(artistModel: PaytmInsiderDetailModel) {


        if (artistModel?.data?.body?.songs != null && !artistModel?.data?.body?.songs?.items.isNullOrEmpty()) {
            topSongList = artistModel?.data?.body?.songs?.items
            llHeaderSongs?.setOnClickListener {
                redirectToMoreBucketListPage(artistModel?.data?.body?.songs?.items, "Top Songs")
            }
            val mLayoutManager = GridLayoutManager(context, 4, GridLayoutManager.HORIZONTAL, false)
            rvSongs?.layoutManager = mLayoutManager

            eventTopAdapter = PaytmEventTopSongAdapter(
                    requireContext(), artistModel?.data?.body?.songs?.items!!,
                    object : PaytmEventTopSongAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            if(childPosition <0 && childPosition >= artistModel?.data?.body?.songs?.items?.size!!) return

                            selectedArtistSongImage =
                                artistModel?.data?.body?.songs?.items?.get(childPosition)?.data?.image!!
                            selectedArtistSongTitle =
                                artistModel?.data?.body?.songs?.items?.get(childPosition)?.data?.title!!
                            selectedArtistSongSubTitle =
                                artistModel?.data?.body?.songs?.items?.get(childPosition)?.data?.subTitle!!
//                            selectedArtistSongHeading = artistModel.data?.head?.data?.title!!
                            selectedArtistSongHeading =
                                requireContext().resources.getString(R.string.artist_str_15)
                            playableItemPosition = childPosition
                            setUpPlayableContentListViewModel(
                                artistModel?.data?.body?.songs?.items?.get(
                                    childPosition
                                )?.data?.id!!
                            )
                            setEventModelDataAppLevel(
                                artistModel?.data?.body?.songs?.items?.get(
                                    childPosition
                                )?.data?.id!!, artistModel?.data?.body?.songs?.items?.get(
                                    childPosition
                                )?.data?.title!!, artistModel.data.head.event.eventName
                            )

                            baseMainScope.launch {
                                withContext(Dispatchers.Main) {
                                    delay(2000)
                                    eventTopAdapter.notifyDataSetChanged()
                                }
                            }

                      }
                    })

                rvSongs?.adapter = eventTopAdapter

                rvSongs?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                        val lastVisiable: Int =
                            mLayoutManager?.findLastCompletelyVisibleItemPosition()!!

                        setLog(
                            TAG,
                            "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                        )
                        if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                            var fromBucket = artistModel?.data?.body?.songs?.items?.get(lastVisiable)?.data?.title
                            var toBucket = artistModel?.data?.body?.songs?.items?.get(lastVisiable)?.data?.title

                            var sourcePage =
                                MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                            if (!fromBucket?.equals(toBucket, true)!!) {
                                callPageScrolledEvent(
                                    sourcePage,
                                    "" + lastVisiable,
                                    fromBucket!!,
                                    toBucket!!
                                )
                            }

                        }
                    }
                })

            llSongs.visibility = View.VISIBLE
        } else {
            llSongs.visibility = View.GONE
        }


    }

    fun setArtistAlbumData(artistModel: PaytmInsiderDetailModel) {
        if (artistModel.data?.body?.album != null && !artistModel.data?.body?.album?.items.isNullOrEmpty()) {
            tvHeadingAlbums?.text = /*artistModel?.data?.head?.data?.title + " " +*/
                getString(R.string.album_str_2)
            llHeaderAlbums.setOnClickListener {
                redirectToMoreBucketListPage(artistModel.data.body.album.items, "Album")
            }

            val mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

                var adapter = EventAlbumAdapter(requireContext(), artistModel.data?.body?.album?.items,
                    object : EventAlbumAdapter.OnItemClick {
                        override fun onUserClick(childPosition: Int) {
                            Constant.paytminsider_redirection = false
                            val albumDetailFragment = AlbumDetailFragment()
                            val bundle = Bundle()
                            bundle.putString(
                                "image",
                                artistModel.data?.body?.album?.items?.get(childPosition)?.data?.image!!
                            )
                            bundle.putString(
                                "id",
                                artistModel.data?.body?.album?.items?.get(childPosition)?.data?.id
                            )
                            bundle.putString("playerType", playerType)
                            albumDetailFragment.arguments = bundle
                            addFragment(
                                R.id.fl_container,
                                this@PaytmInsiderDetailsFragment,
                                albumDetailFragment,
                                false
                            )
                        }
                    })
                rvAlbums?.layoutManager = mLayoutManager
                rvAlbums?.adapter = adapter
                rvAlbums?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val firstVisiable: Int = mLayoutManager?.findFirstVisibleItemPosition()!!
                        val lastVisiable: Int = mLayoutManager?.findLastCompletelyVisibleItemPosition()!!

                        setLog(
                            TAG,
                            "onScrolled: firstVisiable:${firstVisiable} lastVisiable:${lastVisiable}"
                        )
                        if (firstVisiable != lastVisiable && firstVisiable > 0 && lastVisiable > 0 && lastVisiable > firstVisiable) {
                            var fromBucket = artistModel?.data?.body?.album?.items?.get(firstVisiable)?.data?.title
                            var toBucket = artistModel?.data?.body?.album?.items?.get(lastVisiable)?.data?.title

                            setLog("sarvesh", ":${firstVisiable} lastVisiable:${lastVisiable}")
                            var sourcePage =""+MainActivity.lastItemClicked + "_" + MainActivity.headerItemName
                            if (!fromBucket?.equals(toBucket, true)!!) {
                                callPageScrolledEvent(
                                    sourcePage,
                                    "" + lastVisiable,
                                    fromBucket!!,
                                    toBucket!!
                                )
                            }

                        }
                    }
                })
            llAlbums.visibility = View.VISIBLE
        } else {
            llAlbums.visibility = View.GONE
        }
    }

    fun sendViewEvent(event: PaytmInsiderDetailModel.Data.Head.Event?) {
        CoroutineScope(Dispatchers.Main).launch {
            val hashMapPageView = HashMap<String, String>()

            hashMapPageView[EventConstant.CONTENT_NAME_EPROPERTY] = event?.eventName.toString()
            hashMapPageView[EventConstant.CONTENT_TYPE_EPROPERTY] = "" + Utils.getContentTypeNameForStream("" + event?.type.toString())
            hashMapPageView[EventConstant.CONTENT_TYPE_ID_EPROPERTY] = event?.id.toString()
            hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
            hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] =  "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "offline_event_details"
            hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] = "details_offline_event"
            hashMapPageView[EventConstant.SOURCEPAGETYPE_EPROPERTY] = "details_offline_event"
            hashMapPageView[EventConstant.SOURCE] = MainActivity.lastItemClicked
            hashMapPageView[EventConstant.EVENTID] = selectedContentId.toString()
            hashMapPageView[EventConstant.EVENTNAME] = event?.eventName.toString()

            EventManager.getInstance().sendEvent(PageViewEvent(hashMapPageView))
        }
    }

    private fun redirectToMoreBucketListPage(
        bodyRowsItemsItem: java.util.ArrayList<BodyRowsItemsItem?>?,
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

    override fun startTrackPlayback(
        selectedTrackPosition: Int,
        tracksList: MutableList<Track>,
        trackPlayStartPosition: Long
    ) {
        baseMainScope.launch {
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

    var songDataList: ArrayList<Track> = arrayListOf()

    private fun setUpPlayableContentListViewModel(id: String) {
        try {
            if (isAdded && context != null) {
                playableContentViewModel = ViewModelProvider(
                    this
                ).get(PlayableContentViewModel::class.java)


                if (ConnectionUtil(context).isOnline) {
                    playableContentViewModel.getPlayableContentList(requireContext(), id)
                        ?.observe(this,
                            Observer {
                                when (it.status) {
                                    Status.SUCCESS -> {
                                        setProgressBarVisible(false)
                                        if (it?.data != null) {
                                            //setLog(TAG, "isViewLoading $it")
                                            if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                                setPlayableContentListData(it?.data!!)
                                            } else {
                                                playableItemPosition = playableItemPosition + 1
                                                if (playableItemPosition < topSongList?.size!!) {
                                                    setUpPlayableContentListViewModel(
                                                        topSongList?.get(
                                                            playableItemPosition
                                                        )?.data?.id!!
                                                    )
                                                }
                                            }

                                        }
                                    }

                                    Status.LOADING -> {
                                        setProgressBarVisible(true)
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
                    CommonUtils.showToast(
                        requireContext(),
                        messageModel,
                        "ArtistDetailsFragment",
                        "setUpPlayableContentListViewModel"
                    )
                }
            }
        } catch (e: Exception) {

        }


    }


    fun setPlayableContentListData(playableContentModel: PlayableContentModel) {
        baseIOScope.launch {
            if (isAdded && context != null) {
                if (playableContentModel != null) {
                    if (!CommonUtils.checkExplicitContent(
                            requireContext(),
                            playableContentModel.data.head.headData.misc.explicit
                        )
                    ) {
                        CommonUtils.setLog(
                            "PlayableItem",
                            playableContentModel?.data?.head?.headData?.id.toString()
                        )
                        //setPodcastEpisodeList(playableContentModel)
                        songDataList = arrayListOf()

                        for (i in topSongList?.indices!!) {
                            if (playableContentModel?.data?.head?.headData?.id == topSongList?.get(i)?.data?.id) {
                                setArtistContentList(
                                    playableContentModel,
                                    topSongList,
                                    playableItemPosition
                                )
                            } else if (i > playableItemPosition) {
                                setArtistContentList(null, topSongList, i)
                            }
                        }

                        BaseActivity.setTrackListData(songDataList)
                        tracksViewModel?.prepareTrackPlayback(0)
                    }
                }
            }
        }

    }

    fun setArtistContentList(
        playableContentModel: PlayableContentModel?,
        playableItem: List<BodyRowsItemsItem?>?,
        position: Int
    ) {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.id)) {
            try {
                track.id = Integer.parseInt(playableItem?.get(position)?.data?.id!!).toLong()
            } catch (e: NumberFormatException) {
                // Log error, change value of temperature, or do nothing
            }
        } else {
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.title)) {
            track.title = playableItem?.get(position)?.data?.title
        } else {
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.subTitle)) {
            track.subTitle = playableItem?.get(position)?.data?.subTitle
        } else {
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)) {
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        } else {
            track.url = ""
        }

        if (!playableItem?.get(position)?.data?.misc?.movierights.isNullOrEmpty()) {
            track.movierights = playableItem?.get(position)?.data?.misc?.movierights.toString()
        } else {
            track.movierights = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)) {
            track.drmlicence =
                playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        } else {
            track.drmlicence = ""
        }

        if (!TextUtils.isEmpty(playableItem?.get(position)?.data?.type.toString())) {
            track.playerType = playableItem?.get(position)?.data?.type.toString()
        } else {
            track.playerType = Constant.MUSIC_PLAYER
        }
        if (!TextUtils.isEmpty(selectedArtistSongHeading)) {
            track.heading = selectedArtistSongHeading
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

        if (!TextUtils.isEmpty(paytmInsiderRespModel.data.head.event.id)){
            track.parentId = paytmInsiderRespModel.data.head.event.id
        }
        if (!TextUtils.isEmpty(paytmInsiderRespModel.data.head.event.eventName)){
            track.pName = paytmInsiderRespModel.data.head.event.eventName
        }

        if (!TextUtils.isEmpty(paytmInsiderRespModel.data.head.event.aboutEventTitle)){
            track.pSubName = paytmInsiderRespModel.data.head.event.aboutEventTitle
        }

        if (!TextUtils.isEmpty(paytmInsiderRespModel.data.head.event.image[0])){
            track.pImage = paytmInsiderRespModel.data.head.event.image[0]
        }
        track.pType = DetailPages.ARTIST_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        if (playableItem?.get(position)?.data?.misc?.explicit != null){
            track.explicit = playableItem.get(position)?.data?.misc?.explicit!!
        }
        if (playableItem?.get(position)?.data?.misc?.restricted_download != null){
            track.restrictedDownload = playableItem.get(position)?.data?.misc?.restricted_download!!
        }
        if (playableItem?.get(position)?.data?.misc?.attributeCensorRating != null){
            track.attributeCensorRating =
                playableItem.get(position)?.data?.misc?.attributeCensorRating.toString()
        }

        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
    }

    override fun onScrollChanged() {
        if (isAdded) {
            /* get the maximum height which we have scroll before performing any action */
            //val maxDistance: Int = iv_collapsingImageBg.getHeight()
            //val maxDistance: Int = resources.getDimensionPixelSize(R.dimen.dimen_420)
            var maxDistance = resources.getDimensionPixelSize(R.dimen.dimen_345)
            /* how much we have scrolled */
            val movement = scrollView.scrollY
            maxDistance = maxDistance - resources.getDimensionPixelSize(R.dimen.dimen_43)
            setLog("lkajgdlas", " $movement $maxDistance")
            if (movement >= maxDistance) {
                headBlur.visibility = View.VISIBLE
                tvHeader.visibility = View.VISIBLE
            } else {
                headBlur.visibility = View.INVISIBLE
                tvHeader.visibility = View.INVISIBLE
            }
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v?.id
        if (id == R.id.threeDotMenu || id == R.id.threeDotMenu2) {
//            commonThreeDotMenuItemSetup(Constant.EVENT_DETAIL_PAGE)
            val shareurl=getString(R.string.music_player_str_18)+" "+ paytmInsiderModel?.data?.head?.event?.share
            Utils.shareItem(requireActivity(),shareurl)
        }
        else if(id == R.id.tvLocation){
            val strUri = "http://maps.google.com/maps?q=loc:"+paytmInsiderModel?.data?.head?.event?.venueMapLink+" (Label which you want)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            )
            startActivity(intent)
        }
    }

    private fun setupUserViewModel() {
        if (isAdded && context != null) {
            userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

            getUserSocialData()
        }
    }

    private fun setFollowUnFollow(data: PaytmInsiderDetailModel.Data.Head.Artist) {
        if (ConnectionUtil(context).isOnline) {
            val jsonObject1 = JSONObject()
            jsonObject1.put("contentId", data?.id)
            jsonObject1.put("typeId", "0")
            jsonObject1.put("action", data.isFollowed)
            jsonObject1.put("module", Constant.MODULE_FOLLOW)
            userViewModel?.followUnfollowModule(requireContext(), jsonObject1.toString())

            if (data.isFollowed) {
                val messageModel = MessageModel(
                    getString(R.string.artist_str_3),
                    getString(R.string.artist_str_17),
                    MessageType.NEUTRAL,
                    true
                )
                CommonUtils.showToast(
                    requireContext(),
                    messageModel,
                    "ArtistDetailsFragment",
                    "setFollowUnFollow"
                )

                baseIOScope.launch {
                    if (isAdded && context != null) {
                        val hashMap = java.util.HashMap<String, String>()
                        hashMap.put(
                            EventConstant.ALBUMID_EPROPERTY,
                            "" + paytmInsiderRespModel?.data?.head?.event?.id
                        )
                        val newContentId = paytmInsiderRespModel?.data?.head?.event?.id!!
                        val contentIdData = newContentId.replace("artist-", "")
                        hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + contentIdData)
                        val contentType = paytmInsiderRespModel?.data?.head?.event?.type
                        hashMap.put(
                            EventConstant.CONTENTTYPE_EPROPERTY,
                            "" + Utils.getContentTypeName("" + contentType)
                        )
                        hashMap.put(
                            EventConstant.NAME_EPROPERTY,
                            "" + paytmInsiderRespModel?.data?.head?.event?.eventName
                        )
                        hashMap.put(EventConstant.PODCASTHOST_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + paytmInsiderRespModel?.data?.head?.event?.eventName
                        )
                        hashMap.put(EventConstant.CREATOR_EPROPERTY, "Hungama")
                        EventManager.getInstance().sendEvent(FavouritedEvent(hashMap))
                    }
                }

            } else {
                val messageModel = MessageModel(
                    getString(R.string.artist_str_18), getString(R.string.artist_str_19),
                    MessageType.NEUTRAL, true
                )
                CommonUtils.showToast(
                    requireContext(),
                    messageModel,
                    "ArtistDetailsFragment",
                    "setFollowUnFollow"
                )
            }
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(
                requireContext(),
                messageModel,
                "ArtistDetailsFragment",
                "setFollowUnFollow"
            )
        }
    }

    private fun getUserSocialData() {
        try {
            if (isAdded && context != null) {
                if (ConnectionUtil(requireContext()).isOnline) {
                    userViewModel?.getUserFollowData(
                        requireContext(), SharedPrefHelper.getInstance()
                            .getUserId()!!, "5"
                    )?.observe(this,
                        Observer {
                            when (it.status) {
                                Status.SUCCESS -> {
                                    setProgressBarVisible(false)
                                    if (it?.data != null) {
                                        fillUIFollow(it.data)
                                    }

                                }

                                Status.LOADING -> {
                                    setProgressBarVisible(true)
                                }

                                Status.ERROR -> {
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
                    CommonUtils.showToast(
                        requireContext(),
                        messageModel,
                        "ArtistDetailsFragment",
                        "getUserSocialData"
                    )
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun fillUIFollow(data: FollowModel?) {
        this.userFollowData = data
        baseIOScope.launch {
            if (isAdded && context != null) {
                if (userFollowData != null && userFollowData?.data?.body?.rows != null && paytmInsiderModel != null && !paytmInsiderModel?.data?.head?.artist.isNullOrEmpty()) {
                    for (following in userFollowData?.data?.body?.rows?.iterator()!!) {
                        if (null != following) {
                            for (isFollow in paytmInsiderModel?.data?.head?.artist!!) {
                                if (isFollow?.id == following?.data?.id) {
                                    isFollow.isFollowed = true
                                }
                            }
                        }
                    }

                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            artistListAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
        if (!topSongList.isNullOrEmpty()) {
            checkAllContentDownloadedOrNot(topSongList)
        } else {
            playPauseStatusChange(true)
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
                if (!topSongList.isNullOrEmpty()) {
                    checkAllContentDownloadedOrNot(topSongList)
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
        if (!hidden) {
            if (!topSongList.isNullOrEmpty()) {
                checkAllContentDownloadedOrNot(topSongList)
            } else {
                playPauseStatusChange(true)
            }
            baseMainScope.launch {
                if (context != null) {
                    CommonUtils.setLog(
                        "ChartLifecycle",
                        "onHiddenChanged-$hidden--$artworkProminentColor"
                    )
                    changeStatusbarcolor(artworkProminentColor)
                }
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

           if (!hidden && !Constant.paytminsider_redirection) {
                hideBottomNavigationAndMiniplayer()
            }

        setLog("kjahfgsahdo", "isInLayout- $isInLayout isAdded- $isAdded isDetached-- $isDetached isResumed-$isResumed")
    }

    override fun onMoreClick(selectedMoreBucket: RowsItem?, position: Int) {
        baseMainScope.launch {
            val bundle = Bundle()
            bundle.putParcelable("selectedMoreBucket", selectedMoreBucket)
            CommonUtils.setLog(TAG, "onMoreClick:selectedMoreBucket " + selectedMoreBucket?.heading)
            CommonUtils.setLog(TAG, "onMoreClick:selectedMoreBucket " + selectedMoreBucket?.image)
            val moreBucketListFragment = MoreBucketListFragment()
            moreBucketListFragment.arguments = bundle
            addFragment(
                R.id.fl_container,
                this@PaytmInsiderDetailsFragment,
                moreBucketListFragment,
                false
            )

            val dataMap = HashMap<String, String>()
            dataMap.put(EventConstant.BUCKETNAME_EPROPERTY, "" + selectedMoreBucket?.heading)
            dataMap.put(
                EventConstant.CONTENT_TYPE_EPROPERTY,
                "" + paytmInsiderModel?.data?.head?.event?.eventName
            )

            dataMap.put(
                EventConstant.SOURCEPAGE_EPROPERTY,
                "" + Utils.getContentTypeDetailName("" + selectedMoreBucket?.type)
            )
            EventManager.getInstance().sendEvent(MoreClickedEvent(dataMap))
        }

    }

    override fun onParentItemClick(parent: RowsItem, parentPosition: Int, childPosition: Int) {
        if (parent != null
            && !parent.items.isNullOrEmpty()
            && parent.items?.size!! > childPosition
            && parent.items?.get(childPosition)?.data != null
            && parent.items?.get(childPosition)?.data?.type.equals(
                "21",
                true
            ) && paytmInsiderModel != null
        ) {
            selectedArtistSongImage =
                parent.items?.get(childPosition)?.data?.image!!
            selectedArtistSongTitle =
                parent.items?.get(childPosition)?.data?.title!!
            selectedArtistSongSubTitle =
                parent.items?.get(childPosition)?.data?.subTitle!!
            selectedArtistSongHeading = paytmInsiderModel?.data?.head?.event?.eventName!!
            playableItemPosition = childPosition
            setUpPlayableContentListViewModel(
                parent.items?.get(childPosition)?.data?.id!!
            )
            setEventModelDataAppLevel(
                parent.items?.get(childPosition)?.data?.id.toString(),
                parent.items?.get(childPosition)?.data?.title.toString(),
                paytmInsiderModel?.data?.head?.event?.eventName.toString()
            )
        } else {
            onItemDetailPageRedirection(
                parent,
                parentPosition,
                childPosition,
                "_" + parent.heading.toString()
            )
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
        super.onDestroy()
        tracksViewModel?.onCleanup()
    }

    var currentPlayingContentIndex = -1
    var lastPlayingContentIndex = -1
    var isPlaying = false
    private suspend fun checkAllContentDWOrNot(topSongList: ArrayList<BodyRowsItemsItem?>?): Boolean {
        try {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!topSongList.isNullOrEmpty()) {
                    try {
                        topSongList.forEachIndexed { index, it ->
                            if (it != null) {
                                if (!isCurrentContentPlayingFromThis && !BaseActivity.songDataList.isNullOrEmpty()
                                    && BaseActivity.songDataList?.size!! > BaseActivity.nowPlayingCurrentIndex()
                                ) {
                                    val currentPlayingContentId =
                                        BaseActivity.songDataList?.get(BaseActivity.nowPlayingCurrentIndex())?.id
                                    if (currentPlayingContentId?.toString()
                                            ?.equals(it.data?.id)!!
                                    ) {
                                        it.data?.isCurrentPlaying = true
                                        isCurrentContentPlayingFromThis = true
                                        CommonUtils.setLog(
                                            "isCurrentPlaying",
                                            "DetailChartAdapter-checkAllContentDWOrNot-1-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex-index-$index"
                                        )
                                        if (currentPlayingContentIndex >= 0) {
                                        } else {
                                            lastPlayingContentIndex = index
                                        }
                                        currentPlayingContentIndex = index
                                    } else {
                                        if (it.data?.isCurrentPlaying == true) {
                                            lastPlayingContentIndex = index
                                        }
                                        it.data?.isCurrentPlaying = false
                                        playPauseStatusChange(true)
                                    }
                                } else {
                                    if (it.data?.isCurrentPlaying == true) {
                                        lastPlayingContentIndex = index
                                    }
                                    it.data?.isCurrentPlaying = false
                                }
                            }
                        }
                        CommonUtils.setLog(
                            "isCurrentPlaying",
                            "DetailChartAdapter-checkAllContentDWOrNot-2-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex"
                        )
                    } catch (e: Exception) {

                    }
                    CommonUtils.setLog(
                        "isCurrentContentPlayingFromThis",
                        "isCurrentContentPlayingFromThis-3-$isCurrentContentPlayingFromThis"
                    )
                }
                CommonUtils.setLog(
                    "isCurrentContentPlayingFromThis",
                    "isCurrentContentPlayingFromThis-4-$isCurrentContentPlayingFromThis"
                )
                return isCurrentContentPlayingFromThis
            }
        } catch (e: Exception) {

        }

        return false
    }

    private fun checkAllContentDownloadedOrNot(topSongList: ArrayList<BodyRowsItemsItem?>?) {
        baseIOScope.launch {
            if (isAdded && context != null) {
                var isCurrentContentPlayingFromThis = false
                if (!topSongList.isNullOrEmpty()) {
                    CommonUtils.setLog(
                        "isCurrentContentPlayingFromThis",
                        "isCurrentContentPlayingFromThis-1-$isCurrentContentPlayingFromThis"
                    )
                    isCurrentContentPlayingFromThis =
                        withContext(Dispatchers.Default) {
                            CommonUtils.setLog(
                                "isCurrentContentPlayingFromThis",
                                "isCurrentContentPlayingFromThis-2-$isCurrentContentPlayingFromThis"
                            )
                            checkAllContentDWOrNot(topSongList)
                        }
                    CommonUtils.setLog(
                        "isCurrentContentPlayingFromThis",
                        "isCurrentContentPlayingFromThis-5-$isCurrentContentPlayingFromThis"
                    )
                    withContext(Dispatchers.Main) {
                        CommonUtils.setLog(
                            "isCurrentContentPlayingFromThis",
                            "isCurrentContentPlayingFromThis-6-$isCurrentContentPlayingFromThis"
                        )
                        if (isCurrentContentPlayingFromThis) {
                            if (activity != null) {
                                if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.playing) {
                                    playPauseStatusChange(false)
                                } else if ((requireActivity() as MainActivity).getAudioPlayerPlayingStatus() == Constant.pause) {
                                    playPauseStatusChange(true)
                                } else {
                                    playPauseStatusChange(true)
                                }
                            }
                            if (bucketParentAdapter != null) {
                                CommonUtils.setLog(
                                    "isCurrentPlaying",
                                    "DetailChartAdapter-lastPlayingContentIndex-$lastPlayingContentIndex-currentPlayingContentIndex-$currentPlayingContentIndex"
                                )
                                bucketParentAdapter?.notifyItemChanged(lastPlayingContentIndex)
                                bucketParentAdapter?.notifyItemChanged(currentPlayingContentIndex)
                            }
                            CommonUtils.setLog(
                                "isCurrentContentPlayingFromThis",
                                "isCurrentContentPlayingFromThis-7-$isCurrentContentPlayingFromThis"
                            )
                        }
                    }

                    CommonUtils.setLog(
                        "isCurrentContentPlayingFromThis",
                        "isCurrentContentPlayingFromThis-8-$isCurrentContentPlayingFromThis"
                    )
                }
            }
        }
    }

    private fun playPauseStatusChange(status: Boolean) {
        isPlaying = status
        baseMainScope.launch {
            if (isAdded && context != null) {
                val color = R.color.colorWhite
                if (status) {
                    ivBuyNow?.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_play_2,
                            color
                        )
                    )
//                    tvBuyNow?.text = getString(R.string.hero_banner_cta_str_6)
                    ivPlayAllActionBar?.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_play_2,
                            color
                        )
                    )
                    tvPlayAllActionBar?.text = getString(R.string.podcast_str_4)

                } else {
                    ivBuyNow?.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_pause_3,
                            color
                        )
                    )
//                    tvBuyNow?.text = getString(R.string.general_str)
                    ivPlayAllActionBar?.setImageDrawable(
                        requireContext().faDrawable(
                            R.string.icon_pause_3,
                            color
                        )
                    )
//                    tvPlayAllActionBar?.text = getString(R.string.general_str)
                }
//                CommonUtils.setGoldUserViewStyle(requireActivity(), tvPlayAll)
            }
        }
    }

    fun EventButtonClickFunction(hashMapPageView: HashMap<String, String>, buttonText: String) {

        hashMapPageView[EventConstant.TYPE_EPROPERTY] = "Offline Event"
        hashMapPageView[EventConstant.SOURCE_DETAILS_EPROPERTY] = MainActivity.lastItemClicked
        hashMapPageView[EventConstant.SOURCEPAGE_EPROPERTY] ="" + MainActivity.lastItemClicked + "_" + MainActivity.headerItemName + "_" + "PaytmInsider"
        hashMapPageView[EventConstant.button_text] = buttonText
        hashMapPageView[EventConstant.OPTION_SELECTED_EPROPERTY] = buttonText
        hashMapPageView[EventConstant.EVENTID] = selectedContentId.toString()
        hashMapPageView[EventConstant.EVENTNAME] = paytmInsiderModel?.data?.head?.event?.eventName!!
        hashMapPageView[EventConstant.PAGE_NAME_EPROPERTY] ="details_paytm_insider"


        EventManager.getInstance().sendEvent(BannerClickedEvent(hashMapPageView))

      }
    }