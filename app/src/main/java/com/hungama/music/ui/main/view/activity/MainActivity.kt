package com.hungama.music.ui.main.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.hungama.gamification.GamificationSDK
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.database.AppDatabase
import com.hungama.music.data.model.AdsConfigModel
import com.hungama.music.data.model.BodyDataItem
import com.hungama.music.data.model.ContentTypes
import com.hungama.music.data.model.DetailPages
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.data.model.PlayableContentModel
import com.hungama.music.data.model.PlaylistModel
import com.hungama.music.data.model.SongDetailModel
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.PageViewEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.home.eventsubscriber.AppsflyerSubscriber
import com.hungama.music.player.audioplayer.TracksContract
import com.hungama.music.player.audioplayer.model.Track
import com.hungama.music.player.audioplayer.model.Track_State
import com.hungama.music.player.videoplayer.services.ChangeAppIconWorker
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.main.adapter.BucketParentAdapter
import com.hungama.music.ui.main.adapter.Itype50PagerAdapter
import com.hungama.music.ui.main.view.fragment.AlbumDetailFragment
import com.hungama.music.ui.main.view.fragment.ArtistDetailsFragment
import com.hungama.music.ui.main.view.fragment.AudioBookDetailsFragment
import com.hungama.music.ui.main.view.fragment.BatteryOptimizationPermissionDialog
import com.hungama.music.ui.main.view.fragment.ChartDetailFragment
import com.hungama.music.ui.main.view.fragment.CollectionDetailsFragment
import com.hungama.music.ui.main.view.fragment.DiscoverMainTabFragment
import com.hungama.music.ui.main.view.fragment.DiscoverTabFragment
import com.hungama.music.ui.main.view.fragment.EarnCoinAllTabFragement
import com.hungama.music.ui.main.view.fragment.EarnCoinDetailFragment
import com.hungama.music.ui.main.view.fragment.EarnCoinProductFragment
import com.hungama.music.ui.main.view.fragment.EventDetailFragment
import com.hungama.music.ui.main.view.fragment.GameDetailFragment
import com.hungama.music.ui.main.view.fragment.LanguageArtistSelectBottomSheetFragment
import com.hungama.music.ui.main.view.fragment.LibraryMainTabFragment
import com.hungama.music.ui.main.view.fragment.MovieV1Fragment
import com.hungama.music.ui.main.view.fragment.MusicMainFragment
import com.hungama.music.ui.main.view.fragment.MusicVideoDetailsFragment
import com.hungama.music.ui.main.view.fragment.MyPlaylistDetailFragment
import com.hungama.music.ui.main.view.fragment.PaytmInsiderDetailsFragment
import com.hungama.music.ui.main.view.fragment.PlaylistDetailFragmentDynamic
import com.hungama.music.ui.main.view.fragment.PodcastDetailsFragment
import com.hungama.music.ui.main.view.fragment.PostNotificationPermissionDialog
import com.hungama.music.ui.main.view.fragment.ProfileFragment
import com.hungama.music.ui.main.view.fragment.SavedInstanceFragment
import com.hungama.music.ui.main.view.fragment.SearchAllTabFragment
import com.hungama.music.ui.main.view.fragment.SwipablePlayerFragment
import com.hungama.music.ui.main.view.fragment.TvShowDetailsFragment
import com.hungama.music.ui.main.view.fragment.UserProfileOtherUserProfileFragment
import com.hungama.music.ui.main.view.fragment.VideoMainTabFragment
import com.hungama.music.ui.main.viewmodel.PlayableContentViewModel
import com.hungama.music.ui.main.viewmodel.PlaylistViewModel
import com.hungama.music.ui.main.viewmodel.ProductViewModel
import com.hungama.music.ui.main.viewmodel.SongDetailsViewModel
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.applyAppLogo
import com.hungama.music.utils.CommonUtils.getDeeplinkIntentData
import com.hungama.music.utils.CommonUtils.getNextOnboardingScreen
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.isContentDownloaded
import com.hungama.music.utils.CommonUtils.isUserHasNoAdsSubscription
import com.hungama.music.utils.CommonUtils.saveUserProfileDetails
import com.hungama.music.utils.CommonUtils.setGoldButton
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.setPlayerSongList
import com.hungama.music.utils.CommonUtils.showToast
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.CONTENT_LIVE_RADIO
import com.hungama.music.utils.Constant.CONTENT_RADIO
import com.hungama.music.utils.Constant.defaultContentId
import com.hungama.music.utils.Constant.isPlay
import com.hungama.music.utils.Constant.isProfilePage
import com.hungama.music.utils.Constant.isRadio
import com.hungama.music.utils.Constant.isSearchScreen
import com.hungama.music.utils.Constant.isTabSelection
import com.hungama.music.utils.Constant.miniPlayerAction
import com.hungama.music.utils.Constant.playerArtworkChange
import com.hungama.music.utils.Constant.radioType
import com.hungama.music.utils.Constant.tabName
import com.hungama.music.utils.DateUtils
import com.hungama.music.utils.ImageLoader
import com.hungama.music.utils.Utils
import com.hungama.music.utils.customview.CustomTabView
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.BuildConfig
import com.hungama.myplay.activity.R
import com.moengage.cards.ui.CardActivity
import com.moengage.cards.ui.listener.OnCardClickListener
import com.moengage.cards.ui.model.ClickData
import com.moengage.inapp.MoEInAppHelper
import com.moengage.pushbase.MoEPushHelper
import kotlinx.android.synthetic.main.activity_main.swipablePlayerShimmerLayout
import kotlinx.android.synthetic.main.activity_main.tabMenu
import kotlinx.android.synthetic.main.fragment_song_detail.songDetailroot
import kotlinx.android.synthetic.main.header_main.dash_gold
import kotlinx.android.synthetic.main.header_main.ivCoin
import kotlinx.android.synthetic.main.header_main.ivMenuCount
import kotlinx.android.synthetic.main.header_main.ivUserPersonalImage
import kotlinx.android.synthetic.main.header_main.tvCoinCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.arrayListOf
import kotlin.collections.forEachIndexed
import kotlin.collections.isNullOrEmpty
import kotlin.collections.iterator
import kotlin.collections.removeLast
import kotlin.collections.set
import kotlin.collections.toTypedArray


const val READ_EXTERNAL_STORAGE_REQ_CODE: Int = 101
val TAG = MainActivity::class.java.name

class MainActivity : BaseActivity(), BaseActivity.OnLocalBroadcastEventCallBack, OnCardClickListener {
    var isMangeIntentCall = true
    var isDestroyedMainActivity = false
    var pageName = ""
    var pageId = ""
    var pageDetailName = ""
    var pageContentPlay = ""
    var pageContentPayment = ""
    var isSeason = false
    var seasonNumber = 0
    var isMorePage = false
    var morePageName = ""
    var isSubTabSelected = false
    var isCategoryPage = false
    var categoryName = ""
    var categoryId = ""
    var isTrailer = false
    var trailerId = ""
    var isEpisode = false
    var episodeName = ""
    var episodeId = ""
    var appLinkUrl = ""
    var liveShowArtistId = ""
    var queryParam = ""
    var selectedContentId:String = ""
    var songDetailsViewModel: SongDetailsViewModel? = null
    private lateinit var tracksViewModel: TracksContract.Presenter
    var nonRepeat = 0
    var isPlayFromBanner = false
    var hasNotificationPermissionGranted = false
    lateinit var context:MainActivity
    var ft: AdsConfigModel.Ft? = AdsConfigModel.Ft()
    var nonft: AdsConfigModel.Nonft? = AdsConfigModel.Nonft()
    var receiver : BroadcastReceiver? = null

    companion object {
        var callOnStartMain = false
        var lastItemClicked = ""
        var lastItemClickedTop = ""
        var headerItemName = ""
        var lastItemClickedForBTab = ""
        var headerItemNameForBTab = ""
        var subHeaderItemName = ""
        var subHeaderItemNameForBTab = ""
        var subHeader2nd = ""
        var headerItemPosition = 0
        var lastBottomItemPosClicked = 0
        var tempLastItemClicked = ""
        var tempLastBottomItemPosClicked = 0
        var isLaunched = true
        var songDetailModel: SongDetailModel?=null

        var lastClickedDataTopNav = ArrayList<String>()
        var lastClickedData = ArrayList<String>()
        var lastClickedDataSubTopNav = ArrayList<String>()
        var ivLogo: AppCompatImageView? = null


        fun clickedLastTop(value:String):String {
            var lastClicked = ""

            lastClickedData.add(0, value)

            when (lastClickedData.size) {
                1 -> lastClicked = lastClickedData[0]
                2 -> lastClicked = lastClickedData[1]
                3 -> {
                    lastClickedData.removeLast()
                    lastClicked = lastClickedData[1]
                }
            }

            return lastClicked.ifEmpty { "All" }
        }

        fun clickedLastTopNav(value:String):String{
            var lastClicked = ""

            lastClickedDataTopNav.add(0, value)

            when (lastClickedDataTopNav.size) {
                1 -> lastClicked = lastClickedDataTopNav[0]
                2 -> lastClicked = lastClickedDataTopNav[1]
                3 -> {
                    lastClickedDataTopNav.removeLast()
                    lastClicked = lastClickedDataTopNav[1]
                }
            }
            return lastClicked.ifEmpty { "All" }
        }

        fun clickedLastSubTopNav(value:String):String{
            var lastClicked = ""

            lastClickedDataSubTopNav.add(0, value)

            when (lastClickedDataSubTopNav.size) {
                1 -> lastClicked = lastClickedDataSubTopNav[0]
                2 -> lastClicked = lastClickedDataSubTopNav[1]
                3 -> {
                    lastClickedDataSubTopNav.removeLast()
                    lastClicked = lastClickedDataSubTopNav[1]
                }
            }
            return if (lastClicked.isNotEmpty()) "_$lastClicked" else ""
        }
    }


    var userViewModel: UserViewModel? = null
    var userSubscriptionViewModel: UserSubscriptionViewModel? = null
    var remoteConfig = FirebaseRemoteConfig.getInstance()
    var defaultValue = HashMap<String, Any>()
    var receiver1 : BroadcastReceiver? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            SavedInstanceFragment.getInstance(supportFragmentManager).popData()
            setLog("TooLargeTool", "MainActivity-onRestoreInstanceState")
        }
        AppsflyerSubscriber.deeplink = ""
        context = this
        callOnStartMain = true
        getUserSubscriptionStatus()
        HungamaMusicApp.getInstance().deleteCacheData()
        val remoteConfig = Firebase.remoteConfig
        var splashAd1 = remoteConfig.getString("allow_background_activity")
        setLog("allowBackgroundActivity", " " + splashAd1)
        val un_menu = remoteConfig.getString("un_menu_bottom_nav_sequence_preselect")
        setLog("allowBackgroundActivity", " " + un_menu)
        val un_seq = remoteConfig.getString("un_menu_bottom_nav_sequence")
        setLog("allowBackgroundActivity", " " + un_seq)

        var notification_popup_delay = remoteConfig.getDouble("notification_popup_delay")

        val packageName = packageName
        val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (splashAd1.isEmpty())
        {
            splashAd1 = "60000"
        }

        val tm: TelephonyManager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue: String = tm.networkCountryIso

        setLog("aggdhsao", "$countryCodeValue")

        if (SharedPrefHelper.getInstance().isUserLoggedIn())
            getUserProfile()
        
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {

               val batteryOpt = SharedPrefHelper.getInstance()[PrefConstant.backgroundActivity, ""]
                if (batteryOpt.contains("Custom Allowed, Default Allowed")){
                    SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "-")
                }

                CoroutineScope(Dispatchers.IO).launch {
                    delay(splashAd1.toLong())

                    val batteryOptimizationPermissionDialog = BatteryOptimizationPermissionDialog(object :
                            BatteryOptimizationPermissionDialog.BatteryPermission {

                            override fun onBatteryPermission() {
                                checkBatteryPermisssion()
                            }
                        })

                    if(!isFinishing && isDestroyedMainActivity)
                        if(!batteryOptimizationPermissionDialog.isVisible && !supportFragmentManager.isDestroyed) {
                    batteryOptimizationPermissionDialog.show(supportFragmentManager, "open logout dialog")
                    } else {
                        batteryOptimizationPermissionDialog.dismiss()
                    }
                }
            }

        CoroutineScope(Dispatchers.IO).launch {
            delay(notification_popup_delay.toLong())
            val postNotificationPermissionDialog = PostNotificationPermissionDialog(object :
                PostNotificationPermissionDialog.BatteryPermission {

                override fun onBatteryPermission() {
                    getNotificationPermission()
                }
            })

            if(!isFinishing && isDestroyedMainActivity)
                if(!postNotificationPermissionDialog.isVisible && !supportFragmentManager.isDestroyed) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        )
                            postNotificationPermissionDialog.show(
                                supportFragmentManager,
                                "open logout dialog"
                            )
                    }
                } else {
                    postNotificationPermissionDialog.dismiss()
                }
        }

        if (intent.getStringExtra(Constant.DeepLink_Payment) != null && intent.getStringExtra(Constant.DeepLink_Payment).toString().isNotEmpty()) {
            val url = intent.getStringExtra(Constant.DeepLink_Payment).toString()

            val intent = Intent(this@MainActivity, PaymentWebViewActivity::class.java)
            intent.putExtra("url", url)
            startActivity(intent)
        }
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (nonRepeat == 0) {
                    CommonUtils.openSubscriptionDialogPopupNew(
                        this@MainActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(5000)
                        nonRepeat = 0
                    }
                }
                nonRepeat += 1
            }
        }
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.registerReceiver(receiver as BroadcastReceiver, IntentFilter(Constant.CALL_BOTTOM_SHEET))


        receiver1 = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.hasExtra("callOnStart") == true)
                {
                    CommonUtils.showActivity(this@MainActivity, MainActivity::class.java)
                }
                else
                getUserSubscriptionStatus()
            }
        }
        val lbm1 = LocalBroadcastManager.getInstance(this)
        lbm1.registerReceiver(receiver1 as BroadcastReceiver, IntentFilter(Constant.PAYTM_INSIDER))
    }

    private fun checkBatteryPermisssion() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 100)

            }
        }



/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST)
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setLog("printRequestCode", " $requestCode")
        if(requestCode == 100){
            val dataMap= java.util.HashMap<String, String>()

            if (resultCode == RESULT_OK)
            {
                SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "Custom Allowed, Default Allowed")
            }
            else
            {
                SharedPrefHelper.getInstance().save(PrefConstant.backgroundActivity, "Custom Allowed, Default Denied")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

         if(permissions[0]== android.Manifest.permission.POST_NOTIFICATIONS){
             var perm= false
             if(grantResults[0]==1) perm = true

            MoEPushHelper.getInstance().pushPermissionResponse(applicationContext, perm)
            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.PostNotification, perm.toString())
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
             var pot_count= SharedPrefHelper.getInstance().get(PrefConstant.PostNotification,0)
             if(grantResults[0]==-1){
                 SharedPrefHelper.getInstance().save(PrefConstant.PostNotification,pot_count+1)
                 if(pot_count>=2){
                     MoEPushHelper.getInstance().navigateToSettings(this)
                 }
             }
         }
    }

    override fun getLayoutResourceId(): Int {

        CommonUtils.setLog("getLayoutResourceId", "Main Activity getLayoutResourceId called")
        return R.layout.activity_main
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
        MoEInAppHelper.getInstance().showInApp(HungamaMusicApp.getInstance())
        setLocalBroadcast()
        setLog("akjghasfjg", "${SharedPrefHelper.getInstance().get(PrefConstant.USER_NAME, "")}")
        if(SharedPrefHelper.getInstance().get(PrefConstant.USER_NAME, "").isEmpty()) {
            getUserProfile()
        }
    }

    override fun onStart() {
        super.onStart()
        isDestroyedMainActivity = true
        MoEInAppHelper.getInstance().showInApp(HungamaMusicApp.getInstance())
    }

    override fun onStop() {
        isDestroyedMainActivity = false
        super.onStop()

    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)
        setLog("MainActivity", "onViewCreated-isAppLanguageChanged-$isAppLanguageChanged")

//        callContinueWhereLeftListViewModel()
        setLog(TAG, "showDeepLinkUrl onViewCreated")
        initializeComponents()

        displayLanguageDialog()
        isMangeIntentCall = true
        setLog("BaseActivityLifecycleMethods", "MainActivity-onViewCreated-$isAppLanguageChanged")
        if (!isAppLanguageChanged){
            getLocalQueueFromDB()
        }



        defaultValue.put("review_close_app_version_android", 145)
        setUpRemoteConfig()

        addDefaultFragment()

    }

    private fun addDefaultFragment(){

        Constant.paytminsider_redirection = true
        val fragment = DiscoverMainTabFragment.newInstance(this, Bundle())
        replaceFragment(R.id.fl_container,fragment,false)
        lastItemClicked = Constant.Bottom_NAV_DISCOVER
        lastBottomItemPosClicked = 0
        headerItemPosition = 0
        headerItemName = "All"
        lastItemClickedTop = clickedLastTop(Constant.Bottom_NAV_DISCOVER)
        tempLastItemClicked = lastItemClicked
        tempLastBottomItemPosClicked = lastBottomItemPosClicked

    }

    private fun setUpRemoteConfig(){
        CoroutineScope(Dispatchers.IO).launch {
            setLog(TAG, "setUpRemoteConfig: 1")
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)//3600
                .build()

            remoteConfig.setConfigSettingsAsync(configSettings)

            remoteConfig.setDefaultsAsync(defaultValue)
            setLog(TAG, "setUpRemoteConfig: 2")
            val fetch = remoteConfig.fetch(0)
            fetch.addOnSuccessListener {
                setLog(TAG, "setUpRemoteConfig: 3")
                remoteConfig.fetchAndActivate()
                updateReview()
            }
        }
    }

    private fun updateReview() {
        val versionNumber = remoteConfig.getLong("review_close_app_version_android")
        setLog(
            TAG,
            "setUpRemoteConfig: versionNumber:${versionNumber} BuildConfig.VERSION_CODE:${BuildConfig.VERSION_CODE}"
        )
        if (BuildConfig.VERSION_CODE <= versionNumber) {
            displayReviewCloseDialog()
        }
    }

    private fun displayReviewCloseDialog() {
        CoroutineScope(Dispatchers.Main).launch{
           val reviewAlertDialog = AlertDialog.Builder(this@MainActivity).create()
        reviewAlertDialog.setCancelable(false)
        val message = getString(R.string.login_str_56)
        reviewAlertDialog.setMessage(message)
        reviewAlertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            "Ok"
        ) { dialog, which ->
            dialog?.dismiss()
            finish()
        }
        reviewAlertDialog.show()
        }
    }

    private fun getLocalQueueFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
                delay(1500)
                setLog(
                    "displayDiscover",
                    "MainActivity-getLocalQueueFromDB-isDisplayDiscover-$isDisplayDiscover"
                )
                val songDataList = AppDatabase?.getInstance()?.trackDao()?.getAllSong() as ArrayList<Track>?

                var isPlayingSongDetected = false
                if (songDataList != null && songDataList?.size!! > 0) {

                    songDataList.forEachIndexed { index, track ->
                        if (track.state == Track_State.PLAYING) {
                            updateNowPlayingCurrentIndex(index)
                            isPlayingSongDetected = true
                            return@forEachIndexed
                        }
                    }
                    withContext(Dispatchers.Main){
                        if (!isPlayingSongDetected) {
                            updateNowPlayingCurrentIndex(0)
                            setLog("applyScreen", "MainActivity-getLocalQueueFromDB-1")
                            callRecommendedApi()
                        }else{

                            if(isAppLanguageChanged){
                                isAppLanguageChanged = false
                            }else{
                                setLog("MainActivity", "getLocalQueueFromDB-isAppLanguageChanged-$isAppLanguageChanged")
                                if (songDataList.size > nowPlayingCurrentIndex() && !isAppLanguageChanged){
                                    val track = songDataList.get(nowPlayingCurrentIndex())
                                    setLog("applyScreen", "MainActivity-getLocalQueueFromDB-1--track-$track")
                                    if (track.pType == DetailPages.RECOMMENDED_SONG_LIST_PAGE.value
                                        || (track.playerType.equals(Constant.PLAYER_RADIO)
                                                || track.playerType.equals(Constant.PLAYER_LIVE_RADIO)
                                                || track.playerType.equals(Constant.PLAYER_ARTIST_RADIO)
                                                || track.playerType.equals(Constant.PLAYER_ON_DEMAND_RADIO)
                                                || track.playerType.equals(Constant.PLAYER_MOOD_RADIO))){
                                        setLog("applyScreen", "MainActivity-getLocalQueueFromDB-2")
                                        callRecommendedApi()
                                    }
                                    else{
                                        playContent(songDataList, true, START_STATUS)
                                        setLog("applyScreen", "MainActivity-getLocalQueueFromDB-applyScreen-0")
                                        //applyScreen(0, Bundle())
                                    }
                                }
                            }
                        }
                    }

                }else{
                    withContext(Dispatchers.Main){
                        setLog("applyScreen", "MainActivity-getLocalQueueFromDB-3")
                        callRecommendedApi()
                    }
                }
        }
    }

    fun callRecommendedApi(){
        isDisplaySkeleton(true)
        getRecommendedContentList()
    }

    private fun displayLanguageDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            if (SharedPrefHelper.getInstance().isUserLoggedIn() || SharedPrefHelper.getInstance()
                    .isUserGuestLogdIn()
            ) {
                val type = withContext(Dispatchers.Default) {
                    getNextOnboardingScreen(1)
                }
                if (type > 0) {
                    withContext(Dispatchers.Main){
                        delay(2000)
                        try {
                            if(!isFinishing){
                                val sheet = LanguageArtistSelectBottomSheetFragment(type)
                                sheet.show(supportFragmentManager, "OnboardingBottomSheetFragment")
                            }
                        }catch (e:Exception){

                        }
                    }
                }
            }
        }
    }

    private fun initializeComponents() {
        tabMenu.setItemChangeListener(object : CustomTabView.OnTabItemChange {
            override fun onTabItemClick(position: Int) {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            CommonUtils.hapticVibration(
                                this@MainActivity, tabMenu,
                                HapticFeedbackConstants.CONTEXT_CLICK, false
                            )
                        }
                    } catch (e: Exception) {

                    }

                applyScreen(position)
            }
        })
        getIntentData(intent, false)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getIntentData(intent, true)
    }

    private fun getIntentData(intent: Intent?, isNewIntent: Boolean) {
        isDeeplink = false
        if (intent != null && intent?.hasExtra(Constant.EXTRA_PAGE_NAME)!!) {
            if (isNewIntent) {
                isMangeIntentCall = true
                setLog(TAG, "showDeepLinkUrl onNewIntent")
            } else {
                setLog(TAG, "showDeepLinkUrl initializeComponents")
            }
            if (intent?.hasExtra(Constant.EXTRA_LIVESHOWARTIST_ID)!!) {
                liveShowArtistId = intent?.getStringExtra(Constant.EXTRA_LIVESHOWARTIST_ID)!!
            }

            if (intent?.hasExtra(Constant.EXTRA_APPLINKURL)!!) {
                appLinkUrl = intent?.getStringExtra(Constant.EXTRA_APPLINKURL)!!
            }

            if (intent?.hasExtra(Constant.EXTRA_PAGE_NAME)!!) {
                pageName = intent?.getStringExtra(Constant.EXTRA_PAGE_NAME)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_PAGE_DETAIL_ID)!!) {
                pageId = intent?.getStringExtra(Constant.EXTRA_PAGE_DETAIL_ID)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_PAGE_DETAIL_NAME)!!) {
                pageDetailName = intent?.getStringExtra(Constant.EXTRA_PAGE_DETAIL_NAME)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_PAGE_CONTENT_PLAY)!!) {
                pageContentPlay = intent?.getStringExtra(Constant.EXTRA_PAGE_CONTENT_PLAY)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_QUERYPARAM)!!) {
                queryParam = intent?.getStringExtra(Constant.EXTRA_QUERYPARAM)!!
            }

            if (intent?.hasExtra(Constant.EXTRA_PAGE_CONTENT_PAYMENT)!!) {
                pageContentPayment = intent?.getStringExtra(Constant.EXTRA_PAGE_CONTENT_PAYMENT)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_IS_SEASON)!!) {
                isSeason = intent?.getBooleanExtra(Constant.EXTRA_IS_SEASON, false)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_SEASON_NUMBER)!!) {
                seasonNumber = intent?.getIntExtra(Constant.EXTRA_SEASON_NUMBER, 0)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_IS_MORE_PAGE)!!) {
                isMorePage = intent?.getBooleanExtra(Constant.EXTRA_IS_MORE_PAGE, false)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_MORE_PAGE_NAME)!!) {
                morePageName = intent?.getStringExtra(Constant.EXTRA_MORE_PAGE_NAME)!!
            }
            if (intent?.hasExtra(Constant.EXTRA_IS_SUB_TAB_SELECTED)!!) {
                isSubTabSelected = intent?.getBooleanExtra(Constant.EXTRA_IS_SUB_TAB_SELECTED, false)!!
            }
            if (intent.hasExtra(Constant.EXTRA_IS_CATEGORY_PAGE)) {
                isCategoryPage = intent.getBooleanExtra(Constant.EXTRA_IS_CATEGORY_PAGE, false)
            }
            if (intent.hasExtra(Constant.EXTRA_CATEGORY_NAME)) {
                categoryName = intent.getStringExtra(Constant.EXTRA_CATEGORY_NAME)!!
            }
            if (intent.hasExtra(Constant.EXTRA_CATEGORY_ID)) {
                categoryId = intent.getStringExtra(Constant.EXTRA_CATEGORY_ID)!!
            }
            if (intent.hasExtra(Constant.EXTRA_IS_TRAILER)) {
                isTrailer = intent.getBooleanExtra(Constant.EXTRA_IS_TRAILER, false)
            }
            if (intent.hasExtra(Constant.EXTRA_TRAILER_ID)) {
                trailerId = intent.getStringExtra(Constant.EXTRA_TRAILER_ID)!!
            }
            if (intent.hasExtra(Constant.EXTRA_IS_EPISODE)) {
                isEpisode = intent.getBooleanExtra(Constant.EXTRA_IS_EPISODE, false)
            }
            if (intent.hasExtra(Constant.EXTRA_EPISODE_NAME)) {
                episodeName = intent.getStringExtra(Constant.EXTRA_EPISODE_NAME)!!
            }
            if (intent.hasExtra(Constant.EXTRA_EPISODE_ID)) {
                episodeId = intent.getStringExtra(Constant.EXTRA_EPISODE_ID)!!
            }
            if (intent.hasExtra(Constant.isPlayFromBanner)) {
                isPlayFromBanner = intent.getBooleanExtra(Constant.isPlayFromBanner,false)
            }
            manageIntent()
        } else {
//            setLog(TAG, "getIntentData applyScreen")
            //if (isDisplayDiscover){
            //setLog("applyScreen", "MainActivity-getIntentData-applyScreen-0")
                //applyScreen(0)
            //}
        }
    }


    private fun manageIntent() {
        setLog("deepLinkUrlMain", "showDeepLinkUrl isMangeIntentCall:$isMangeIntentCall pageName:$pageName pageId:$pageId pageDetailName:$pageDetailName isCategoryPage:$isCategoryPage")
        if (isMangeIntentCall) {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                val currentFragment = Utils.getCurrentFragment(this)

                if (!TextUtils.isEmpty(pageName) && !TextUtils.isEmpty(pageId) && !isCategoryPage)
                {
                    val bundle = Bundle()
                    bundle.putString(Constant.defaultContentImage, "")
                    bundle.putString(Constant.defaultContentId, "" + pageId)
                    bundle.putString(Constant.defaultContentPlayerType, "")
                    bundle.putBoolean(Constant.defaultContentVarient, true)
                    bundle.putBoolean(Constant.EXTRA_IS_SEASON, isSeason)
                    bundle.putInt(Constant.EXTRA_SEASON_NUMBER, seasonNumber)
                    bundle.putBoolean(Constant.EXTRA_IS_MORE_PAGE, isMorePage)
                    bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
                    bundle.putBoolean(Constant.EXTRA_IS_SUB_TAB_SELECTED, isSubTabSelected)
                    bundle.putBoolean(Constant.EXTRA_IS_TRAILER, isTrailer)
                    bundle.putString(Constant.EXTRA_TRAILER_ID, trailerId)
                    bundle.putBoolean(Constant.EXTRA_IS_EPISODE, isEpisode)
                    bundle.putString(Constant.EXTRA_EPISODE_NAME, episodeName)
                    bundle.putString(Constant.EXTRA_EPISODE_ID, episodeId)
                    setLog(TAG, "manageIntent: pageContentPlay:${pageContentPlay}")
                    if (!TextUtils.isEmpty(pageContentPlay)) {
                        bundle.putInt(isPlay, 1)
                    } else {
                        bundle.putInt(isPlay, 0)
                    }

                    if (!TextUtils.isEmpty(pageContentPayment)) {
                        bundle.putInt(Constant.isPayment, 1)
                        bundle.putString(Constant.EXTRA_QUERYPARAM, queryParam)
                    }
                    val varient = 1

                    setLog("DeeplinkPageName", " " + pageName.toString())

                    when (pageName) {
                        "song", "songs" -> {
                    /*        val songDetailFragment = SongDetailFragment()
                            songDetailFragment.arguments = bundle
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrlcurrentFragment1:${bundle?.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    songDetailFragment,
                                    false
                                )*/


                                setUpPlayableContentListViewModel(this,bundle.getString(defaultContentId).toString(), false)


                           /* } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }*/

                        }

                        "music" -> {
                            applyScreen(1)
                        }

                        "videos" -> {
                            applyScreen(2)
                        }

                        "library" -> {
                            if (!TextUtils.isEmpty(pageDetailName)) {
                                bundle.putBoolean(isTabSelection, true)
                                bundle.putString(tabName, pageDetailName)
                                if (isSubTabSelected) {
                                    bundle.putString(Constant.subTabName, morePageName)
                                    bundle.putBoolean(
                                        Constant.EXTRA_IS_SUB_TAB_SELECTED,
                                        isSubTabSelected
                                    )
                                }
                            }
                            applyScreen(4, bundle)
                        }

                        "video" -> {
                            applyScreen(2)
                            setPauseMusicPlayerOnVideoPlay()

                            val videoDetailsFragment = MusicVideoDetailsFragment()
                            videoDetailsFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass?.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    videoDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "chart", "charts" -> {
                            if(pageId.contains("Daily",true)){
                                bundle.putString("id", "" + pageId)
                            }else{
                                bundle.putString("id", "" + pageId)
                            }

                            val nextFragment = ChartDetailFragment.newInstance(varient)
                            nextFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(R.id.fl_container, currentFragment, nextFragment, false)
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "tv-show", "tv-shows" -> {
                            if (!isPlayFromBanner){
                                applyScreen(2)}

                            val tvShowDetailsFragment = TvShowDetailsFragment(varient)
                            tvShowDetailsFragment.arguments = bundle
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    tvShowDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "original", "originals" -> {

                        }

                        "music-video", "music-videos" -> {
                            applyScreen(2)
                            setPauseMusicPlayerOnVideoPlay()

                            val videoDetailsFragment = MusicVideoDetailsFragment()
                            videoDetailsFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    videoDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "game", "games" -> {
                            applyScreen(0)
                            val gameDetailFragment = GameDetailFragment()
                            gameDetailFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    gameDetailFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "music-video-playlist" -> {

                        }

                        "artist", "artists" -> {
                            bundle.putString("id", "artist-" + pageId)
                            val artistDetailsFragment = ArtistDetailsFragment()
                            artistDetailsFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    artistDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }

                        }

                        "podcast", "podcasts" -> {

                            val podcastDetailsFragment = PodcastDetailsFragment()
                            podcastDetailsFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    podcastDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }

                        }

                        "playlist", "playlists" -> {
                            if(pageId.contains("Daily",true)){
                                bundle.putString("id", "" + pageId)
                                bundle.putString(Constant.EXTRA_QUERYPARAM, queryParam)
                            }else{
                                bundle.putString("id", "playlist-" + pageId)
                            }

                            val nextFragment = PlaylistDetailFragmentDynamic.newInstance(varient)
                            nextFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrlcurrentFragment2:${bundle.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(R.id.fl_container, currentFragment, nextFragment, false)
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "movie", "movies", "short-film", "short-films" -> {
                            applyScreen(2)
                            val movieDetailsFragment = MovieV1Fragment(varient)
                            movieDetailsFragment.arguments = bundle
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    movieDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "radio" -> {
                            setLog(
                                TAG,
                                "showDeepLinkUrlcurrentFragment11:${bundle}"
                            )
                            bundle.putInt(isPlay, 1)
                            bundle.putBoolean(isRadio, true)
                            bundle.putInt(radioType, CONTENT_RADIO)
                            applyScreen(0, bundle)
                        }

                        "live-radio" -> {
                            setLog(
                                TAG,
                                "showDeepLinkUrlcurrentFragment1111:${bundle}"
                            )
                            bundle.putInt(isPlay, 1)
                            bundle.putBoolean(isRadio, true)
                            bundle.putInt(radioType, CONTENT_LIVE_RADIO)
                            applyScreen(0, bundle)
                        }

                        "live-show", "live-shows" -> {
                            bundle.putString(Constant.defaultArtistId, liveShowArtistId)
//                            bundle.putString(Constant.defaultArtistId, "artist-81488978")
                            BucketParentAdapter.isVisible = false
                            val eventDetailFragment = EventDetailFragment()
                            eventDetailFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass?.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    eventDetailFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        "user-profile" -> {

                        }

                        "premium" -> {

                        }

                        "notification" -> {

                        }

                        "search" -> {

                        }

                        "stories" -> {
                            val storyUsersList = ArrayList<BodyDataItem>()
                            val storyUser = BodyDataItem()
                            storyUser?.id = pageId
                            storyUsersList.add(storyUser)


                                val status = getAudioPlayerPlayingStatus()
                                if (status == Constant.pause){
                                    SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(true)
                                }else{
                                    SharedPrefHelper.getInstance().setLastAudioContentPlayingStatus(false)
                                }
                                pausePlayer()

                            val intent = Intent(this, StoryDisplayActivity::class.java)
                            intent.putExtra("position", 0)
                            intent.putParcelableArrayListExtra("list", storyUsersList)
                            startActivityForResult(
                                intent,
                                DiscoverTabFragment.LAUNCH_STORY_DISPLAY_ACTIVITY
                            )
                        }

                        "recently-played" -> {

                        }

                        "downloaded-songs" -> {

                        }

                        "album" -> {
                            if (!isFinishing) {
                                val albumDetailFragment = AlbumDetailFragment()
                                albumDetailFragment.arguments = bundle

                                if (currentFragment != null) {
                                    setLog(
                                        TAG,
                                        "showDeepLinkUrl currentFragment:${currentFragment.javaClass?.name}"
                                    )
                                    isMangeIntentCall = false
                                    addFragment(
                                        R.id.fl_container,
                                        currentFragment,
                                        albumDetailFragment,
                                        false
                                    )
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }
                            }
                        }

                        "audiobook", "audiostory"->{
                            if (!isFinishing) {

                                if (isEpisode && !TextUtils.isEmpty(episodeId)){
                                    setUpPlayableContentListViewModel(this, episodeId, false)
                                }
                                else{
                                    val audioBookFragment = AudioBookDetailsFragment()
                                    audioBookFragment.arguments = bundle

                                    if (currentFragment != null) {
                                        setLog(
                                            TAG,
                                            "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                        )
                                        isMangeIntentCall = false
                                        addFragment(
                                            R.id.fl_container,
                                            currentFragment,
                                            audioBookFragment,
                                            false
                                        )
                                    } else {
                                        isMangeIntentCall = true
                                        setLog(TAG, "showDeepLinkUrl currentFragment is null:")

                                    }
                              }
                            }
                        }
/*
                        "audiostory"->{
                            if (!isFinishing) {
                                val audioStoryFragment = PodcastDetailsFragment()
                                audioStoryFragment.arguments = bundle

                                if (currentFragment != null) {
                                    isMangeIntentCall = false
                                    addFragment(
                                        R.id.fl_container,
                                        currentFragment,
                                        audioStoryFragment,
                                        false
                                    )
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }
                            }
                        }
*/

                        "collection", "collections" -> {
                            val collectionDetailsFragment = CollectionDetailsFragment()
                            collectionDetailsFragment.arguments = bundle
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass?.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    collectionDetailsFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "redeem" -> {
                            if (pageDetailName.contains("category", true)) {
                                if (currentFragment != null) {
                                    val earnCoinAllTabFragement = EarnCoinAllTabFragement()
                                    earnCoinAllTabFragement.arguments = bundle
                                    isMangeIntentCall = false
                                    addFragment(
                                        R.id.fl_container,
                                        currentFragment!!,
                                        earnCoinAllTabFragement,
                                        false
                                    )
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }
                            } else if (pageDetailName.contains("product", true)) {
                                if (currentFragment != null) {
                                    callProductApi(pageId, currentFragment,bundle)
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }


                            }

                        }

                        "user-playlist" -> {
                            val myPlaylistDetailFragment = MyPlaylistDetailFragment(1,object :MyPlaylistDetailFragment.onBackPreesHendel{
                                override fun backPressItem(status: Boolean) {
                                }

                            })
                            myPlaylistDetailFragment.arguments = bundle

                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass?.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    myPlaylistDetailFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                    }
                }
                else if (!TextUtils.isEmpty(pageName) && !TextUtils.isEmpty(pageDetailName)) {
                    val bundle = Bundle()
                    bundle.putBoolean(isTabSelection, true)
                    bundle.putString(tabName, pageDetailName)
                    setLog("deepLinkUrl", "topTab == tabName-$pageDetailName")
                    if (isSubTabSelected) {
                        bundle.putString(Constant.subTabName, morePageName)
                        bundle.putBoolean(Constant.EXTRA_IS_SUB_TAB_SELECTED, isSubTabSelected)
                    }
                    bundle.putBoolean(Constant.EXTRA_IS_CATEGORY_PAGE, isCategoryPage)
                    bundle.putString(Constant.EXTRA_CATEGORY_NAME, categoryName)
                    bundle.putString(Constant.EXTRA_CATEGORY_ID, categoryId)
                    when (pageName) {
                        "payment" -> {
                            setLog(
                                "isGotoDownloadClicked",
                                "MainActivity-payment-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                            )
                            if (ConnectionUtil(this@MainActivity).isOnline) {
                                val genrtedURL = CommonUtils.genratePaymentPageURL(this@MainActivity, queryParam)
                                var intent = Intent()
                                if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                    intent = Intent(this@MainActivity, PaymentWebViewActivity::class.java)
                                    setLog("payment", "payment genrtedURL:${genrtedURL} queryParam:${queryParam}")
                                }

                                else{
                                    intent = Intent(this@MainActivity, LoginMainActivity::class.java)
                                }
                                setLog("PrintUrl", " deeplink " + genrtedURL)

                                intent.putExtra("url", genrtedURL)
                                startActivity(intent)

                            } else {
                                val messageModel = MessageModel(
                                    getString(R.string.toast_str_35),
                                    getString(R.string.toast_message_5),
                                    MessageType.NEGATIVE,
                                    true
                                )
                                showToast(this@MainActivity, messageModel,"MainActivity","manageIntent-payment")
                            }

                        }
                        "retry" -> {
                            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                setLog(
                                    "isGotoDownloadClicked",
                                    "MainActivity-retry-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                                )
                                if (ConnectionUtil(this@MainActivity).isOnline) {
                                    val intent =
                                        Intent(
                                            this@MainActivity,
                                            PaymentWebViewActivity::class.java
                                        )
                                    intent.putExtra("url", appLinkUrl)
                                    startActivity(intent)
                                } else {
                                    val messageModel = MessageModel(
                                        getString(R.string.toast_str_35),
                                        getString(R.string.toast_message_5),
                                        MessageType.NEGATIVE,
                                        true
                                    )
                                    showToast(this@MainActivity, messageModel,"MainActivity","manageIntent-retry")
                                }

                            }
                        }
                        "redeem" -> {
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinAllTabFragement(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "earn-coins" -> {
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinDetailFragment(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "music" -> {
                            if (pageDetailName == "more"){
                                bundle.putString(tabName, pageName)
                            }
                            applyScreen(1, bundle)
                        }
                        "rewind-2022" -> {
                            applyScreen(0, bundle)
                        }
                        "games" -> {
                            if (pageDetailName == "more"){
                                bundle.putString(tabName, pageName)
                            }
                            applyScreen(0, bundle)
                        }
                        "videos" -> {
                            if (pageDetailName == "more"){
                                bundle.putString(tabName, pageName)
                            }
                            applyScreen(2, bundle)
                        }
                        "search" -> {
                            bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, pageDetailName)
                            bundle.putBoolean(isSearchScreen, true)
                            applyScreen(0, bundle)
                        }
                        "library" -> {
                            applyScreen(4, bundle)
                        }
                        "offlineevent" ->{
                            if (pageDetailName == "more"){
                                bundle.putString(tabName, pageName)
                            }
                            val paytmInsiderDetailsFragment= PaytmInsiderDetailsFragment()
                            paytmInsiderDetailsFragment.arguments = bundle
                            bundle.putString(defaultContentId, pageDetailName)
                            addFragment(R.id.fl_container, currentFragment, paytmInsiderDetailsFragment, false)
                        }
                        "user-profile" -> {
                            bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, pageDetailName)
                            bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
                            if (!TextUtils.isEmpty(pageDetailName) && pageDetailName.length < 15){
                                val profileFragment = ProfileFragment()
                                profileFragment.arguments = bundle

                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    profileFragment,
                                    false
                                )
                            }else{
                                callUserAPI(bundle,pageDetailName,pageName,morePageName,currentFragment)
                            }
                        }
                        "user-signup" -> {
                            if(!SharedPrefHelper.getInstance().isUserLoggedIn())
                            startActivity(Intent(this@MainActivity, LoginMainActivity::class.java))
                        }
                        else -> {
                            if (CommonUtils.getMusicPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                if (pageDetailName == "more"){
                                    bundle.putString(tabName, pageName)
                                }
                                setLog("deepLinkUrl", "bottomTab-Music == pageName-$pageName")

                                if (pageDetailName == "more"){
                                    bundle.putString(tabName, pageName)
                                }
                                applyScreen(1, bundle)
                            } else if (CommonUtils.getVideoPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                if (pageDetailName == "more"){
                                    bundle.putString(tabName, pageName)
                                }
                                setLog("deepLinkUrl", "bottomTab-Video == pageName-$pageName")
                                if (pageDetailName == "more"){
                                    bundle.putString(tabName, pageName)
                                }
                                applyScreen(2, bundle)
                            } else {
                                if (pageDetailName == "more"){
                                    bundle.putString(tabName, pageName)
                                }
                                setLog("deepLinkUrl", "bottomTab-Discover == pageName-$pageName")
                                applyScreen(0, bundle)
                            }
                        }
                    }
                }
                else if (!TextUtils.isEmpty(pageName))
                {

                    when (pageName) {
                        "pllf"-> {
                            val url = appLinkUrl
                            val split = url.split("&").toTypedArray()
                            val json= JSONObject()
                            json.put("dsn",split[3].replace("af_sub3=", ""))
                            json.put("token",split[2].replace("af_sub2=", ""))
                            json.put("app_id",WSConstants.tata_app_id)
                            json.put("uid",SharedPrefHelper.getInstance().getUserId())
                            userViewModel?.tataplay_subscription(this@MainActivity,json.toString())

                            userViewModel?.live_satus?.observe(this, Observer {
                                if(it=="1"){
                                    getUserSubscriptionStatus()
                                }
                            })

                            setLog("DeeplinkPageName", " " + pageName)

                            val bundle = Bundle()
                            bundle.putString(Constant.defaultContentImage, "")

                            for (domain in split) {
                                println("applinkarray " +domain)
                            }

                            pageId = split[5].replace("af_sub5=", "")

                            val contentType =  split[4].replace("af_sub4=", "")


                            bundle.putString(Constant.defaultContentId, "" + pageId)
                            bundle.putString(Constant.defaultContentPlayerType, "")
                            bundle.putBoolean(Constant.defaultContentVarient, true)
                            bundle.putBoolean(Constant.EXTRA_IS_SEASON, isSeason)
                            bundle.putInt(Constant.EXTRA_SEASON_NUMBER, seasonNumber)
                            bundle.putBoolean(Constant.EXTRA_IS_MORE_PAGE, isMorePage)
                            bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
                            bundle.putBoolean(Constant.EXTRA_IS_SUB_TAB_SELECTED, isSubTabSelected)
                            bundle.putBoolean(Constant.EXTRA_IS_TRAILER, isTrailer)
                            bundle.putString(Constant.EXTRA_TRAILER_ID, trailerId)
                            bundle.putBoolean(Constant.EXTRA_IS_EPISODE, isEpisode)
                            bundle.putString(Constant.EXTRA_EPISODE_NAME, episodeName)
                            bundle.putString(Constant.EXTRA_EPISODE_ID, episodeId)
                            setLog(TAG, "manageIntent: pageContentPlay:${pageId}")
                            val varient = 1

                          //  userViewModel

                            if (contentType == "playlist") {
                                if(pageId.contains("Daily",true)){
                                    bundle.putString("id", "" + pageId)
                                    bundle.putString(Constant.EXTRA_QUERYPARAM, queryParam)
                                }else{
                                    bundle.putString("id", "playlist-" + pageId)
                                }
                                val nextFragment =
                                    PlaylistDetailFragmentDynamic.newInstance(varient)
                                nextFragment.arguments = bundle

                                if (currentFragment != null) {
                                    setLog(
                                        TAG,
                                        "showDeepLinkUrlcurrentFragment2:${bundle.javaClass.name}"
                                    )
                                    isMangeIntentCall = false
                                    addFragment(
                                        R.id.fl_container,
                                        currentFragment,
                                        nextFragment,
                                        false
                                    )
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }
                            }
                            else if(contentType == "audioTrack"){
                                setUpPlayableContentListViewModel(this, pageId, false)
                            }
                            else if(contentType == "audioAlbum"){
                                if (!isFinishing) {
                                    if(pageId.contains("Daily",true)){
                                        bundle.putString("id", "" + pageId)
                                        bundle.putString(Constant.EXTRA_QUERYPARAM, queryParam)
                                    }else{
                                        bundle.putString("id", pageId)
                                    }
                                    val albumDetailFragment = AlbumDetailFragment()
                                    albumDetailFragment.arguments = bundle

                                    if (currentFragment != null) {
                                        setLog(
                                            TAG,
                                            "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                        )
                                        isMangeIntentCall = false
                                        addFragment(
                                            R.id.fl_container,
                                            currentFragment,
                                            albumDetailFragment,
                                            false
                                        )
                                    } else {
                                        isMangeIntentCall = true
                                        setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                    }
                                }
                            }
                        }
                        "music" -> {
                            applyScreen(1)
                        }
                        "videos" -> {
                            applyScreen(2)
                        }
                        "search" -> {
                            val bundle = Bundle()
                            bundle.putBoolean(isSearchScreen, true)
                            applyScreen(0, bundle)
                        }
                        "library" -> {
                            applyScreen(4)
                        }
                        "notification" -> {
                            startActivity(Intent(this, CardActivity::class.java))
                        }
                        "payment","paymnet" -> {
                            setLog(
                                "isGotoDownloadClicked",
                                "MainActivity-payment-2-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                            )
                            if (ConnectionUtil(this@MainActivity).isOnline) {
                                var intent = Intent()
                                val genrtedURL = CommonUtils.genratePaymentPageURL(this@MainActivity, queryParam)
                                setLog("PaymentUrl", genrtedURL)
                                if(SharedPrefHelper.getInstance().isUserLoggedIn()){
                                    intent = Intent(this@MainActivity, PaymentWebViewActivity::class.java)
                                    intent.putExtra("url", genrtedURL)
                                }
                                else{
                                    setLog("PrintUrl", " deeplink 1196 " + genrtedURL)
                                    intent = Intent(this@MainActivity, LoginMainActivity::class.java)
                                    intent.putExtra(Constant.DeepLink_Payment, genrtedURL)
                                }

                                startActivity(intent)
                            } else {
                                val messageModel = MessageModel(
                                    getString(R.string.toast_str_35),
                                    getString(R.string.toast_message_5),
                                    MessageType.NEGATIVE,
                                    true
                                )
                                showToast(this@MainActivity, messageModel,"MainActivity","manageIntent-payment")
                            }

                        }
                        "retry" -> {
                            if (SharedPrefHelper.getInstance().isUserLoggedIn()) {
                                setLog(
                                    "isGotoDownloadClicked",
                                    "MainActivity-retry-2-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
                                )
                                if (ConnectionUtil(this@MainActivity).isOnline) {
                                    val intent =
                                        Intent(
                                            this@MainActivity,
                                            PaymentWebViewActivity::class.java
                                        )
                                    intent.putExtra("url", appLinkUrl)
                                    startActivity(intent)
                                } else {
                                    val messageModel = MessageModel(
                                        getString(R.string.toast_str_35),
                                        getString(R.string.toast_message_5),
                                        MessageType.NEGATIVE,
                                        true
                                    )
                                    showToast(this@MainActivity, messageModel,"MainActivity","manageIntent-payment")
                                }

                            }
                        }
                        "redeem" -> {
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinAllTabFragement(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "earn-coins" -> {
                            if (currentFragment != null) {
                                setLog(
                                    TAG,
                                    "showDeepLinkUrl currentFragment:${currentFragment.javaClass.name}"
                                )
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment!!,
                                    EarnCoinDetailFragment(),
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "user-profile" -> {
                            val bundle = Bundle()
                            bundle.putBoolean(isProfilePage, true)

                            val profileFragment=ProfileFragment()
                            profileFragment?.arguments = bundle

                            getUserProfile()

                            if (currentFragment != null) {

                                isMangeIntentCall = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1000)
                                }
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    profileFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }
                        "privacy-policy" ->{
                            CommonUtils.openCommonWebView(Constant.privacyPolicy,this)
                        }
                        else -> {

                            setLog(TAG, "showDeepLinkUrl SEO URL is pageName:" + pageName)
                            val bundle = Bundle()
                            bundle.putBoolean(isTabSelection, true)
                            bundle.putString(tabName, pageName)

                            if (CommonUtils.getMusicPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                setLog("deepLinkUrl", "bottomTab-Music == pageName-$pageName")
                                applyScreen(1, bundle)
                            } else if (CommonUtils.getVideoPageArrayList().toString().lowercase()
                                    .contains(pageName.lowercase())
                            ) {
                                setLog("deepLinkUrl", "bottomTab-Video == pageName-$pageName")
                                applyScreen(2, bundle)
                            } else {
                                setLog("deepLinkUrl", "bottomTab-Discover == pageName-$pageName")
                                applyScreen(0, bundle)
                            }

                        }
                    }
                } else
                {
                    setLog(TAG, "showDeepLinkUrl null applyScreen")
                    setLog("applyScreen", "MainActivity-manageIntent-applyScreen-0")
                    applyScreen(0)

                }

                if (!TextUtils.isEmpty(pageName)){
                        val dataMap = HashMap<String, String>()
                        dataMap[EventConstant.SOURCE_DETAILS_EPROPERTY] = appLinkUrl
                        dataMap[EventConstant.SOURCE] = "deeplink"
                        dataMap[EventConstant.CONTENTNAME_EPROPERTY] = pageDetailName
                        dataMap[EventConstant.CONTENT_TYPE_EPROPERTY] = pageName
                    val newContentId=pageId
                    val contentIdData=newContentId.replace("playlist-","")
                    dataMap[EventConstant.CONTENTID_EPROPERTY] = contentIdData
                        dataMap[EventConstant.PAGE_NAME_EPROPERTY] = "details_"+ pageName
                        EventManager.getInstance().sendEvent(PageViewEvent(dataMap))
                }
                setLog("IType104ViewHolder", "IType104ViewHolder deeplink call End:${DateUtils.getCurrentDateTimeNewFormat()}")
            }, 1000)
        }

    }



    private fun callUserAPI(
        bundle: Bundle,
        pageDetailName: String,
        pageName: String,
        morePageName: String,
        currentFragment: Fragment?
    ) {
        setLog(TAG, "showDeepLinkUrl pageDetailName:${pageDetailName} USER_SHARE:${SharedPrefHelper.getInstance().get(PrefConstant.USER_SHARE,"")}")
        //bundle.putString(Constant.EXTRA_PAGE_DETAIL_NAME, pageDetailName)
        //bundle.putString(Constant.EXTRA_MORE_PAGE_NAME, morePageName)
        var profileFragment:Fragment? = null
        if (ConnectionUtil(this@MainActivity).isOnline) {
            userViewModel?.getUserProfileData(
                this@MainActivity, pageDetailName
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{

                            setLog(TAG, "showDeepLinkUrl user profile:${it?.data}")


                            if(it?.data?.result?.get(0)?.uId?.equals(SharedPrefHelper.getInstance().getUserId())!!){
                                setLog(TAG, "showDeepLinkUrl user profile:${it?.data}")
                                profileFragment=ProfileFragment()
                                profileFragment?.arguments = bundle

                            }else{
                                setLog(TAG, "showDeepLinkUrl other user profile:${it?.data}")
                                val otherUserId= it?.data?.result?.get(0)?.uId.toString()
                                setLog(TAG, "showDeepLinkUrl otherUserId:${otherUserId}")
                                profileFragment=UserProfileOtherUserProfileFragment(otherUserId)

                            }

                            if (profileFragment != null) {
                                isMangeIntentCall = false
                                addFragment(
                                    R.id.fl_container,
                                    currentFragment,
                                    profileFragment,
                                    false
                                )
                            } else {
                                isMangeIntentCall = true
                                setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                            }
                        }

                        Status.LOADING ->{

                        }

                        Status.ERROR ->{
                            profileFragment?.arguments = bundle
                            profileFragment=ProfileFragment()
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(this@MainActivity, messageModel,"MainActivity","callUserAPI")
        }


    }

    public fun applyScreen(position: Int, bundle: Bundle = Bundle.EMPTY) {
        setLog(TAG, "applyScreen position:$position")
        headerItemPosition = 0
//        headerItemName = "All"

        when (position) {
            0 -> {
                var fragment: Fragment? = null
                setLog(
                    "displayDiscover",
                    "MainActivity-applyScreen-isDisplayDiscover-$isDisplayDiscover"
                )
                Constant.paytminsider_redirection = true
                fragment = DiscoverMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.Bottom_NAV_DISCOVER, position)
            }
            1 -> {
                Constant.paytminsider_redirection = true
                isDisplaySkeleton(false)
                val fragment = MusicMainFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_MUSIC, position)
            }
            2 -> {
                Constant.paytminsider_redirection = true
                isDisplaySkeleton(false)
                val fragment = VideoMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                val hashMap = HashMap<String, String>()
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_VIDEOS, position)
            }
            3 -> {
                Constant.paytminsider_redirection = true
                isDisplaySkeleton(false)
                val fragment = SearchAllTabFragment()
                fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_SEARCH, position)
            }
            4 -> {
                Constant.paytminsider_redirection = true
                isDisplaySkeleton(false)
                val fragment = LibraryMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
                setLastClickedBottomMenu(Constant.BOTTOM_NAV_LIBRARY, position)
            }
            5 -> {
                Constant.paytminsider_redirection = true
                openPlayerScreen(position, bundle)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    BucketParentAdapter.isVisible = false
                    Itype50PagerAdapter.callPlayerList()?.pause()
                }

            }
            else -> {
                Constant.paytminsider_redirection = true
                isDisplaySkeleton(false)
                val fragment = DiscoverMainTabFragment.newInstance(this, bundle)
                //fragment.arguments = bundle
                replaceFragment(R.id.fl_container, fragment, false)
            }
        }
    }

    fun getUserProfile() {
        userViewModel = ViewModelProvider(
            this@MainActivity
        ).get(UserViewModel::class.java)
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-getUserProfile-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this@MainActivity).isOnline(!Constant.ISGOTODOWNLOADCLICKED)) {
            userViewModel?.getUserProfileData(
                this@MainActivity, SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this@MainActivity,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            if (it?.data != null) {
                                saveUserProfileDetails(it?.data)
                                updateProfile()
                                callEventUserProperty(it?.data)
                            }
                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {

                        }
                    }
                })
        }

    }


    fun updateProfile() {
        try {
            setLog(TAG, "Gamification"+ ivUserPersonalImage.toString())

            if (ivUserPersonalImage != null) {
                if (!TextUtils.isEmpty(
                        SharedPrefHelper.getInstance().get(PrefConstant.USER_IMAGE, "")
                    )
                ) {
                    ImageLoader.loadImage(
                        this@MainActivity, ivUserPersonalImage!!,
                        SharedPrefHelper.getInstance().get(
                            PrefConstant.USER_IMAGE, ""
                        ), R.drawable.ic_no_user_img
                    )
                } else {
                    ImageLoader.loadImage(
                        this@MainActivity,
                        ivUserPersonalImage!!,
                        "",
                        R.drawable.ic_no_user_img
                    )
                }
                if(SharedPrefHelper.getInstance().isUserLoggedIn()){
                    var gmfSDKCoins = GamificationSDK.getPoints()

                    if (gmfSDKCoins < 0){
                        gmfSDKCoins = 0
                    }
                    val userCoinDetailRespModel = SharedPrefHelper?.getInstance()?.getObjectUserCoin(
                        PrefConstant.USER_COIN
                    )
                    userCoinDetailRespModel?.actions?.get(0)?.total = gmfSDKCoins
                    if (userCoinDetailRespModel != null && userCoinDetailRespModel?.actions != null) {
                        tvCoinCount?.text =
                            CommonUtils?.ratingWithSuffix("" + userCoinDetailRespModel?.actions?.get(0)?.total!!)
                    } else {
                        tvCoinCount?.text = "" + gmfSDKCoins
                    }
                    setLog(TAG, "updateProfile tvCoinCount 2 login:${SharedPrefHelper.getInstance().isUserLoggedIn()} tvCoinCount:${tvCoinCount?.text}")
                }else{
                    tvCoinCount?.text = "0"
                    setLog(TAG, "updateProfile tvCoinCount 3 login:${SharedPrefHelper.getInstance().isUserLoggedIn()} tvCoinCount:${tvCoinCount?.text}")
                }

                if (CommonUtils.isUserHasGoldSubscription()) {
                    ivMenuCount?.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_round_count_bg_home_action_bar_gold
                    )
                    ivCoin?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.bg_coin_profile_black
                        )
                    )
                    tvCoinCount?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorBlack))
                } else {
                    ivMenuCount?.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_round_count_bg_home_action_bar
                    )
                    ivCoin?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.bg_coin_profile
                        )
                    )
                    tvCoinCount?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorWhite))
                }
                ivMenuCount?.visibility=View.VISIBLE
            }
        } catch (e: Exception) {

        }
    }

    fun getUserSubscriptionStatus() {
        userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-getUserSubscriptionStatus-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this).isOnline) {
            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(this)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {

                            /*if (it?.data != null && it?.data?.success!! && it?.data?.data != null && it?.data?.data?.status!!) {

                            }*/
                            updateViewBasedOnSubscription()
                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {

                        }
                    }
                })
        }
    }

    fun updateViewBasedOnSubscription() {
        setIsGoldUser()
        if (isUserHasNoAdsSubscription()){
            setLog(
                "setPageBottomSpacing",
                "MainActivity-true-isBottomStickyAdLoaded: ${isBottomStickyAdLoaded}"
            )
            isBottomStickyAdLoaded = false
        } else {
            setLog(
                "setPageBottomSpacing",
                "MainActivity-false-isBottomStickyAdLoaded: ${isBottomStickyAdLoaded}"
            )
        }

        if (MainActivity.ivLogo != null) {
            applyAppLogo(this, MainActivity.ivLogo!!)
        }

        if (dash_gold != null) {
            setGoldButton(this, dash_gold)
        }
    }



    override fun onDestroy() {

        /**
         * Logic for app icon change
         * FreeAlias=> free user
         * GoldAlias=> Gold user
         */
        ChangeAppIconWorker.enqueue(this)
        stopChormeCast()
        if (!isAppLanguageChanged){
            audioPlayerService?.hidePlayerNotification(true)
        }
        callOnStartMain = false
        val lbm1 = LocalBroadcastManager.getInstance(this)
        if (receiver1 != null)
        {
            lbm1.unregisterReceiver(receiver1 as BroadcastReceiver)
        }
        val lbm = LocalBroadcastManager.getInstance(this)
        if (receiver != null)
        {
            lbm.unregisterReceiver(receiver as BroadcastReceiver)
        }

        super.onDestroy()
    }



    private fun setLocalBroadcast() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_EVENT));
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_MINI_PLAYER_EVENT));
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter(Constant.AUDIO_PLAYER_UI_EVENT));
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        val event = intent.getIntExtra("EVENT", 0)
        setLog("MainActivity", "PlayerEvent-$intent")
        if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
            try {
                when (getAudioPlayerPlayingStatus()) {
                    Constant.playing -> {
                        setLog("MainActivity", "PlayerEvent-playing")
                        //tabMenu?.updatePlayPauseIcon(Constant.playing)
                    }
                    Constant.pause -> {
                        setLog("MainActivity", "PlayerEvent-pause")
                        //tabMenu?.updatePlayPauseIcon(Constant.pause)
                    }
                    else -> {
                        setLog("MainActivity", "PlayerEvent-none")
                        //tabMenu?.updatePlayPauseIcon(Constant.noneAudio)
                    }
                }
            } catch (e: java.lang.Exception) {

            }
        } else if (event == Constant.AUDIO_MINI_PLAYER_CLICK_RESULT_CODE) {
            val miniPlayerAction = intent.getIntExtra(miniPlayerAction, 0)
            if (miniPlayerAction == Constant.playerCollapsed) {
                setLog(
                    "miniplayerAction",
                    "MainActivity-onLocalBroadcastEventCallBack-action-playerCollapsed"
                )
            } else if (miniPlayerAction == Constant.playerExpand) {

                setLog(TAG, "miniplayerAction applyScreen")
                setLog(
                    "miniplayerAction",
                    "MainActivity-onLocalBroadcastEventCallBack-action-playerExpand"
                )

                applyScreen(5)
            }
        }else if (event == Constant.AUDIO_PLAYER_UI_RESULT_CODE) {
            setLog("MainActivity", "PlayerUIEvent-change")
            val miniPlayerAction = intent.getStringExtra(playerArtworkChange)
            tabMenu?.updatePlayerTabArtwork(miniPlayerAction)
        }else if (event == Constant.AUDIO_PLAYER_END_RESULT_CODE){
            setLog("MainActivity", "AUDIO_PLAYER_END_RESULT_CODE")
        }
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setLog("BroadcastReceiver-1", "MainActivity-mMessageReceiver-" + intent)
            if (intent != null) {
                if (intent.hasExtra("EVENT")) {
                    setLog(
                        "BroadcastReceiver-1",
                        "MainActivity-mMessageReceiver-" + intent.getIntExtra("EVENT", 0)
                    )
                    onLocalBroadcastEventCallBack(context, intent)
                }
            }
        }
    }

    var recommendedListViewModel: PlaylistViewModel? = null
    var recommendedSongList: ArrayList<PlaylistModel.Data.Body.Row> = ArrayList()
    fun getRecommendedContentList(bottomTabPosition:Int = -1, isReplaceScreen:Boolean = false) {
        setLog(
            "displayDiscover",
            "MainActivity-getRecommendedContentList-isDisplayDiscover-$isDisplayDiscover"
        )
        setLog("SwipablePlayerFragment", "MainActivity-getRecommendedContentList()-1-songDataList-${BaseActivity.songDataList?.size}")
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-getRecommendedContentList-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        if (ConnectionUtil(this).isOnline) {
            recommendedListViewModel = ViewModelProvider(
                this
            ).get(PlaylistViewModel::class.java)
            recommendedListViewModel?.getRecommendedContentList(this, "")?.observe(this
            ) {
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it?.data != null) {
                            recommendedSongList = it.data.data.body.rows
                            if (!recommendedSongList.isNullOrEmpty()) {
                                //if (isOnClick()) {
                                val songList: ArrayList<Track> = arrayListOf()
                                for (item in recommendedSongList.iterator()) {
                                    var playerImage = ""
                                    if (item?.data?.playble_image != null && !TextUtils.isEmpty(item?.data?.playble_image)) {
                                        playerImage = item.data.playble_image
                                    } else {
                                        playerImage = item.data.image
                                    }

                                    val track = setPlayerSongList(
                                        item.data.id,
                                        item.data.title,
                                        item.data.subtitle,
                                        "",
                                        "",
                                        "",
                                        item.data.type,
                                        playerImage,
                                        "Recommended",
                                        it.data.data.head.data.id,
                                        it.data.data.head.data.title,
                                        it.data.data.head.data.subtitle,
                                        it.data.data.head.data.image,
                                        DetailPages.RECOMMENDED_SONG_LIST_PAGE.value,
                                        ContentTypes.AUDIO.value,
                                        item.data.misc.explicit,
                                        item.data.misc.restricted_download,
                                        item.data.misc.attributeCensorRating.toString(),
                                        item.data.misc.movierights.toString(),
                                        item.data.misc.share
                                    )
                                    songList.add(track)
                                }
                                updateNowPlayingCurrentIndex(0)
                                playContent(songList, true, START_STATUS)
                                setLog(TAG, "getRecommendedContentList applyScreen")
                                setLog(
                                    "applyScreen",
                                    "MainActivity-getRecommendedContentList-applyScreen-$bottomTabPosition"
                                )
                                if (bottomTabPosition > -1) {
                                    if (isReplaceScreen) {
                                        openPlayerScreen(
                                            bottomTabPosition,
                                            Bundle(),
                                            isReplaceScreen
                                        )
                                    } else {
                                        applyScreen(bottomTabPosition, Bundle())
                                    }
                                }
                                //}
                            }
                        }
                    }

                    Status.LOADING -> {

                    }

                    Status.ERROR -> {
                        val messageModel = it.message?.let { it1 ->
                            MessageModel(
                                "", it1,
                                MessageType.NEGATIVE, true
                            )
                        }
                        if (messageModel != null) {
                            showToast(
                                this,
                                messageModel,
                                "MainActivity",
                                "getRecommendedContentList"
                            )
                        }
                        isDisplaySkeleton(false)
                    }
                }
            }
        }
    }

    fun playContent(songDataList: ArrayList<Track>, isPause:Boolean, isNextClick:Int = START_STATUS) {
            try {
                setLog("NotificationManager", "MainActivity-playContent-isPause-$isPause")
                setTrackListData(songDataList)
                this.isNextClick = isNextClick
                val playingIndex = nowPlayingCurrentIndex()

                val isOfflinePlay = isContentDownloaded(BaseActivity.songDataList, playingIndex)
                if (isOfflinePlay && !BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > playingIndex) {
                    setEventModelDataAppLevel(
                        "" + BaseActivity.songDataList?.get(playingIndex)?.id!!,
                        BaseActivity.songDataList?.get(playingIndex)?.title!!,
                        BaseActivity.songDataList?.get(playingIndex)?.pName!!,
                        EventConstant.CONSUMPTIONTYPE_OFFLINE
                    )
                    playContentOffline(BaseActivity.songDataList, playingIndex, isPause)
                } else {
                    if (!BaseActivity.songDataList.isNullOrEmpty() && BaseActivity.songDataList?.size!! > playingIndex) {
                        if (BaseActivity.songDataList?.get(playingIndex)?.pType == DetailPages.LOCAL_DEVICE_SONG_PAGE.value) {
                            setEventModelDataAppLevel(
                                "" + BaseActivity.songDataList?.get(playingIndex)?.id!!,
                                BaseActivity.songDataList?.get(playingIndex)?.title!!,
                                BaseActivity.songDataList?.get(playingIndex)?.pName!!,
                                EventConstant.CONSUMPTIONTYPE_LOCAL
                            )
                            playContentOfflineDeviceSongs(
                                BaseActivity.songDataList,
                                playingIndex,
                                isPause
                            )

                        } else {
                            setEventModelDataAppLevel(
                                "" + BaseActivity.songDataList?.get(playingIndex)?.id!!,
                                BaseActivity.songDataList?.get(playingIndex)?.title!!,
                                BaseActivity.songDataList?.get(playingIndex)?.pName!!,
                                EventConstant.CONSUMPTIONTYPE_ONLINE
                            )
                            playContentOnline(BaseActivity.songDataList, playingIndex, isPause)

                        }

                    }

                }
            } catch (e: Exception) {

            }
    }

    private fun callProductApi(productId: String, currentFragment: Fragment, bundle: Bundle) {
        setLog(TAG, "callProductApi called")
        CommonUtils.setLog(
            "isGotoDownloadClicked",
            "MainActivity-callProductApi-isGotoDownloadClicked-${Constant.ISGOTODOWNLOADCLICKED}"
        )
        val productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        if (ConnectionUtil(this@MainActivity).isOnline) {
            productViewModel.getProductDetails(this@MainActivity, productId)?.observe(this)
                {
                    when (it.status) {
                        Status.SUCCESS -> {
                            if (it?.data?.product != null) {
                                CommonUtils.setLog(
                                    "MyOrder",
                                    "MyOrderListFragment-callProductApi-Responce-${it.data}"
                                )

                                if (currentFragment != null) {
                                    val earnCoinProductFragment = EarnCoinProductFragment(it?.data?.product)
                                    isMangeIntentCall = false
                                    addFragment(
                                        R.id.fl_container,
                                        currentFragment!!,
                                        earnCoinProductFragment,
                                        false
                                    )
                                } else {
                                    isMangeIntentCall = true
                                    setLog(TAG, "showDeepLinkUrl currentFragment is null:")
                                }

                            }
                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {
                        }
                    }
                }
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true
            )
            CommonUtils.showToast(this@MainActivity, messageModel,"MainActivity","callProductApi")
        }
    }

    fun isDisplaySkeleton(isDisplay:Boolean){
        /*if(isDisplay){
            swipablePlayerShimmerLayout.visibility = View.VISIBLE
            swipablePlayerShimmerLayout.startShimmer()
        }else{
            swipablePlayerShimmerLayout.stopShimmer()
            swipablePlayerShimmerLayout.visibility = View.GONE
        }*/
        swipablePlayerShimmerLayout.stopShimmer()
        swipablePlayerShimmerLayout.visibility = View.GONE
    }


    fun unSelectAllTab(){
        tabMenu?.unselectAll()
    }

    fun openPlayerScreen(position: Int, bundle: Bundle = Bundle.EMPTY, isReplaceScreen:Boolean = false){
        closePIPVideoPlayer()
        var fragment: Fragment = SwipablePlayerFragment()
        setLog(
            "displayDiscover",
            "MainActivity-applyScreen()-isDisplayDiscover-$isDisplayDiscover"
        )

        if (!BaseActivity.songDataList.isNullOrEmpty()){
            if (songDataList?.size!! > nowPlayingCurrentIndex()){
                setLog("SwipablePlayerFragment", "MainActivity-applyScreen()-songDataList-${BaseActivity.songDataList?.size}-contentType-${songDataList?.get(nowPlayingCurrentIndex())?.contentType}")
                if (songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.VIDEO.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.SHORT_VIDEO.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.SHORT_FILMS.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.LIVE_CONCERT.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.TV_SHOWS.value
                    || songDataList?.get(nowPlayingCurrentIndex())?.contentType == ContentTypes.MOVIES.value){
                    setLog("SwipablePlayerFragment", "MainActivity-applyScreen()-closeVideoMiniplayer()-${songDataList?.get(nowPlayingCurrentIndex())?.title}")
                    closeVideoMiniplayer()
                }
            }
            isNewSwipablePlayerOpen = true
            hideMiniPlayer()
            hideStickyAds()
            fragment = SwipablePlayerFragment()

            if (songDataList != null){
                //SharedPrefHelper.getInstance().save("songlist", Gson().toJson(BaseActivity.songDataList))
                /*val str = Gson().toJson(BaseActivity.songDataList)
                val list:ArrayList<Track> = ArrayList()
                for (item in BaseActivity.songDataList?.iterator()!!){
                    list.add(item)
                }
                bundle.putSerializable("songlist", list)*/
            }
            isDisplaySkeleton(false)
        }else{
            getRecommendedContentList(5, isReplaceScreen)
            return
        }
        fragment.arguments = bundle
        if (isReplaceScreen){
            replaceFragment(R.id.fl_container, fragment, false)
        }else{
            val currentFragment = Utils.getCurrentFragment(this)
            if (currentFragment?.javaClass?.simpleName.equals(
                    MusicVideoDetailsFragment().javaClass.simpleName,
                    true
                )
            ) {
              onBackPressed()
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                addFragment(R.id.fl_container, currentFragment, fragment, false)
            }
        }

        setLastClickedBottomMenu(Constant.BOTTOM_NAV_PLAYER, position)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            SavedInstanceFragment.getInstance(supportFragmentManager).pushData(outState.clone() as Bundle)
            outState.clear() // We don't want a TransactionTooLargeException, so we handle things via the SavedInstanceFragment
            setLog("TooLargeTool", "MainActivity-onSaveInstanceState")
        }catch (e:Exception){

        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        try {
            SavedInstanceFragment.getInstance(supportFragmentManager).popData()
                ?.let { super.onRestoreInstanceState(it) }
            setLog("TooLargeTool", "MainActivity-onRestoreInstanceState")
        }catch (e:Exception){

        }
    }

    fun setLastClickedBottomMenu(bottomNavMenu: String, position: Int) {
        lastItemClicked = bottomNavMenu
        lastItemClickedTop = clickedLastTop(bottomNavMenu)
        lastBottomItemPosClicked = position
        if (!bottomNavMenu.equals(Constant.BOTTOM_NAV_PLAYER)) {
            tempLastItemClicked = bottomNavMenu
            tempLastBottomItemPosClicked = position
        }
        tabMenu?.setBottomTabSelection(position)

        setLog(
            "setLastClickedBottomMenu",
            "MainActivity bottomNavMenu:${bottomNavMenu} position:${position}"
        )
    }
    fun openSwapePlayer(bundle: Bundle){
        if (bundle != null) {
            if (bundle.containsKey(Constant.defaultContentId)) {
                selectedContentId = bundle.getString(Constant.defaultContentId).toString()
                setLog(TAG, "initializeComponent: selectedContentId " + selectedContentId)
            }
            setSongDetailViewModel()

            if (!TextUtils.isEmpty(selectedContentId)) {
                val downloadedAudio = AppDatabase.getInstance()?.downloadedAudio()
                    ?.findByContentId(selectedContentId)
                if (downloadedAudio != null && downloadedAudio?.contentId.equals(selectedContentId)
                ) {
                    val playableContentModel = PlayableContentModel()
                    playableContentModel.data?.head?.headData?.id =
                        downloadedAudio.contentId!!
                    playableContentModel.data?.head?.headData?.misc?.url =
                        downloadedAudio.downloadedFilePath
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.url =
                        downloadedAudio.downloadUrl!!
                    playableContentModel.data?.head?.headData?.misc?.downloadLink?.drm?.token =
                        downloadedAudio.drmLicense
                    playableContentModel.data?.head?.headData?.misc?.sl?.lyric?.link =
                        downloadedAudio.lyricsUrl
                    setPlayableContentListData1(playableContentModel)
                } else {
                    setUpPlayableContentListViewModel(selectedContentId.toLong(),
                        songDataList[0].playerType?.toInt(),
                        isPause)
                }
            }

        }
    }

    fun setPlayableContentListData1(playableContentModel: PlayableContentModel) {

        if (playableContentModel != null) {
            if (!CommonUtils.checkExplicitContent(this, playableContentModel.data.head.headData.misc.explicit)){
                setLog("PlayableItem", "setPlayableContentListData id:"+playableContentModel?.data?.head?.headData?.id.toString())
                setLog("PlayableItem", "setSongLyricsData setPlayableContentListData"+playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link.toString())

                songDataList = arrayListOf()

                if (playableContentModel?.data?.head?.headData?.id == selectedContentId) {
                    setSongList(playableContentModel)
                }

                BaseActivity.setTrackListData(songDataList)
                tracksViewModel.prepareTrackPlayback(0)
            }
        }
    }

    var songDataList:ArrayList<Track> = arrayListOf()
    fun setSongList(
        playableContentModel: PlayableContentModel
    ): ArrayList<Track> {
        val track: Track = Track()
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.id)){
            track.id = playableContentModel.data.head.headData.id.toLong()
        }else{
            track.id = 0
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.title)){
            track.title = playableContentModel?.data?.head?.headData?.title
        }else{
            track.title = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.subtitle)){
            track.subTitle = playableContentModel?.data?.head?.headData?.subtitle
        }else{
            track.subTitle = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.url)){
            track.url = playableContentModel?.data?.head?.headData?.misc?.url
        }else{
            track.url = ""
        }

        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token)){
            track.drmlicence = playableContentModel?.data?.head?.headData?.misc?.downloadLink?.drm?.token
        }else{
            track.drmlicence = ""
        }


        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link)) {
            track.songLyricsUrl = playableContentModel?.data?.head?.headData?.misc?.sl?.lyric?.link
        } else {
            track.songLyricsUrl = ""
        }
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.type.toString())){
            track.playerType = playableContentModel?.data?.head?.headData?.type.toString()
        }else{
            track.playerType = Constant.MUSIC_PLAYER
        }
        /*if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.title)){
            track.heading = AlbumDetailFragment.albumRespModel?.data?.head?.data?.title
        }else{
            track.heading = ""
        }*/
        if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.playble_image)){
            track.image = playableContentModel?.data?.head?.headData?.playble_image
        }else if (!TextUtils.isEmpty(playableContentModel?.data?.head?.headData?.image)){
            track.image = playableContentModel?.data?.head?.headData?.image
        }else{
            track.image = ""
        }
        if (!playableContentModel?.data?.head?.headData?.misc?.pid.isNullOrEmpty()){
            track.parentId = playableContentModel?.data?.head?.headData?.misc?.pid?.get(0).toString()
        }

        if (!playableContentModel?.data?.head?.headData?.misc?.pName.isNullOrEmpty()){
            track.pName = playableContentModel?.data?.head?.headData?.misc?.pName?.get(0).toString()
        }

        /* if (!TextUtils.isEmpty(AlbumDetailFragment.albumRespModel?.data?.head?.data?.subtitle!!)){
             track.pSubName = AlbumDetailFragment.albumRespModel?.data?.head?.data?.subtitle
         }*/

        if (!TextUtils.isEmpty(playableContentModel.data.head.headData.misc.movierights.toString())){
            track.movierights = playableContentModel.data.head.headData.misc.movierights.toString()
        }

        track.pType = DetailPages.SONG_DETAIL_PAGE.value
        track.contentType = ContentTypes.AUDIO.value

        track.explicit = playableContentModel?.data?.head?.headData?.misc?.explicit!!
        track.restrictedDownload = playableContentModel?.data?.head?.headData?.misc?.restricted_download!!
        track.attributeCensorRating =
            playableContentModel?.data?.head?.headData?.misc?.attributeCensorRating.toString()


        if (playableContentModel != null){
            track.urlKey = playableContentModel.data.head.headData.misc.urlKey
        }
        songDataList.add(track)
        return songDataList
    }

    private fun setSongDetailViewModel() {
        songDetailsViewModel = ViewModelProvider(
            this
        ).get(SongDetailsViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            songDetailsViewModel?.getSongDetail(this, selectedContentId)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{

                            if (it?.data != null) {
                                setLog(TAG, "shortFilmDetailResp: $it")
                                songDetailModel =it?.data
                                songDetailroot.visibility = View.VISIBLE
                               // setDetails(it?.data.data?.head)
                            }
                        }

                        Status.LOADING ->{
                        }

                        Status.ERROR ->{
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
        }
    }


    override fun onCardClick(clickData: ClickData): Boolean {
        val intent = getDeeplinkIntentData(Uri.parse(clickData.navigationAction.value))
        intent.setClass(this@MainActivity, MainActivity::class.java)
        startActivity(intent)
        return false
    }

    fun setUpPlayableContentListViewModel(context:Context,id: String, isCallFromBanner:Boolean) {
        if (isCallFromBanner) {
            openPlayerScreen(5, Bundle(), false)
            BucketParentAdapter.isVisible = false
        }
        val playableContentViewModel: PlayableContentViewModel
        if (ConnectionUtil(context).isOnline) {
            playableContentViewModel = ViewModelProvider(
                this
            ).get(PlayableContentViewModel::class.java)

            playableContentViewModel?.getPlayableContentList(context, id)?.observe(this) {
                when (it.status) {
                    Status.SUCCESS -> {
                        if (it?.data != null) {
                            if (!TextUtils.isEmpty(it?.data?.data?.head?.headData?.misc?.url)) {
                                SongLinkOpenInPlayer(it?.data)
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(700)
                                    if (!isCallFromBanner)
                                        openPlayerScreen(5, Bundle(), true)
                                }

                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun SongLinkOpenInPlayer(playableContentModel: PlayableContentModel){

        songDataList = arrayListOf()
        setSongList(playableContentModel)

        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            setTrackListData(songDataList)
            updateNowPlayingCurrentIndex(0)

            if (!CommonUtils.checkGoldMusicPodcast()){
                playContent(songDataList, true, START_STATUS)
                CommonUtils.openSubscriptionDialogPopupNew(this@MainActivity)
                return@launch
            }
            playContent(songDataList, false, START_STATUS)
        }

    }
    private fun getNotificationPermission() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATION_PERMISION)
          //  MoEPushHelper.getInstance().requestPushPermission(this)

        }
    }
}
