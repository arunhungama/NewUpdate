package com.hungama.music.ui.main.view.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.appsflyer.AppsFlyerLib
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.*
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.*
import com.hungama.music.home.eventreporter.LoginEvent
import com.hungama.music.home.eventreporter.TappedTCContinueEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.home.eventsubscriber.AppsflyerSubscriber
import com.hungama.music.ui.main.adapter.CountryListAdapter
import com.hungama.music.ui.main.adapter.LoginPlatformSequenceAdapter
import com.hungama.music.ui.main.adapter.LoginSliderAdapter
import com.hungama.music.ui.main.view.activity.EmailOTPVarifyActivity.FacebookFields
import com.hungama.music.ui.main.viewmodel.LoginSliderModel
import com.hungama.music.ui.main.viewmodel.UserSubscriptionViewModel
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.isPackageInstalled
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant.SIGNIN_WITH_ALL
import com.hungama.music.utils.Constant.SIGNIN_WITH_APPLE
import com.hungama.music.utils.Constant.SIGNIN_WITH_EMAIL
import com.hungama.music.utils.Constant.SIGNIN_WITH_FACEBOOK
import com.hungama.music.utils.Constant.SIGNIN_WITH_GOOGLE
import com.hungama.music.utils.Constant.SIGNIN_WITH_MOBILE
import com.hungama.music.utils.customview.applelogin.*
import com.hungama.music.utils.customview.bottomsheet.CornerRadiusFrameLayout
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.act_login_main.*
import kotlinx.android.synthetic.main.act_login_main.btnSendOTP
import kotlinx.android.synthetic.main.act_login_main.coordinator
import kotlinx.android.synthetic.main.act_login_main.etCountryCode
import kotlinx.android.synthetic.main.act_login_main.etMobileNumber
import kotlinx.android.synthetic.main.act_login_main.imageFlag
import kotlinx.android.synthetic.main.act_login_main.llFacebook
import kotlinx.android.synthetic.main.act_login_main.llGoogle
import kotlinx.android.synthetic.main.act_login_main.tvPrivacyPolicy
import kotlinx.android.synthetic.main.act_login_main.tvTermCondtion
import kotlinx.android.synthetic.main.act_login_main.tvTermsOfServices
import kotlinx.android.synthetic.main.act_login_main.tv_send_otp
import kotlinx.android.synthetic.main.activity_enter_mobile.*
import kotlinx.android.synthetic.main.activity_otp_varify.*
import kotlinx.android.synthetic.main.activity_phone_number.*
import kotlinx.android.synthetic.main.layout_progress.*
import kotlinx.android.synthetic.main.list_bottom_sheet.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

class LoginMainActivity() : AppCompatActivity() {

    // get reference of the firebase auth
    var userViewModel: UserViewModel? = null
    val RC_SIGN_IN = 100
    val TAG = javaClass.name.toString()
    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    var callbackManager: CallbackManager?=null
    val provider = OAuthProvider.newBuilder("apple.com")
    var SIGNUPMETHOD="EMAIL"
    var action = 0
    var isForAudio = false
    var countryDataList = CountryDataModel()
    var listOfCountry = mutableListOf<CountryModel>()
    var listOfCountrySearch = mutableListOf<CountryModel>()
    var layoutManger: LinearLayoutManager? = null
    lateinit var viewPager2: ViewPager2
    private val sliderHandler = Handler()
    private var sliderItems = mutableListOf<LoginSliderModel>()
    private lateinit var animation : Animation
    private lateinit var loginSliderAdapter : LoginSliderAdapter
    private var loginPlatformSequenceList:ArrayList<LoginPlatformSequenceModel> = ArrayList()
    private lateinit var sheetBehavior: BottomSheetBehavior<CornerRadiusFrameLayout>
    var selectedCountry : String ="India"
    var selectedCountryCode : String ="+91"
    var isHEDataLoaded = false
    var isCountryDataLoaded = false
    var heMsisdnWithIsdCode = ""
    var heCountryCode = ""
    var heCountryName = ""
    var heMobileNumber = ""
    var number : String =""
    object AppleConstants {

        val CLIENT_ID = "com.hungama.myplay.staging.signin"
        val REDIRECT_URI = "https://user.api.hungama.com/v1/apple-callback"
        val SCOPE = "email"
        val AUTHURL = "https://appleid.apple.com/auth/authorize"
        val TOKENURL = "https://appleid.apple.com/auth/token"

    }

    lateinit var appleAuthURLFull: String
    lateinit var appledialog: Dialog
    lateinit var appleAuthCode: String
    lateinit var appleClientSecret: String
    var dotscount : Int = 0
    var url = ""


    var appleId = ""
    var appleFirstName = ""
    var appleMiddleName = ""
    var appleLastName = ""
    var appleEmail = ""
    var appleAccessToken = ""
    var receiver : BroadcastReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login_main)
        //tvTermsOfServices.text = Html.fromHtml(getString(R.string.login_str_61))
        loginPlatformSequenceList = CommonUtils.getLoginPlateformSequence()
        sliderItems.add(LoginSliderModel(R.drawable.login_movie))
        sliderItems.add(LoginSliderModel(R.drawable.login_music))
        sliderItems.add(LoginSliderModel(R.drawable.login_podcast))
        isActiveActivity = true
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                CommonUtils.showActivity(this@LoginMainActivity, LoginMainActivity::class.java)
            }
        }
        val lbm = LocalBroadcastManager.getInstance(this)
        lbm.registerReceiver(receiver as BroadcastReceiver, IntentFilter(Constant.LOGIN_MAIN))
        if (intent.hasExtra(Constant.DeepLink_Payment))
            url = intent.getStringExtra(Constant.DeepLink_Payment).toString()
        isForAudio = intent.getBooleanExtra("isForAudio", false)
        setLog("PrintUrl", " Login oncreate url " + url)

        viewPager2 = findViewById(R.id.viewpager)
        val sliderDotspanel = findViewById<LinearLayout>(R.id.ivImageThreeDots)
        loginSliderAdapter = LoginSliderAdapter()
        loginSliderAdapter.setdata(sliderItems,this)
        viewPager2.adapter = loginSliderAdapter
        dotscount = loginSliderAdapter.itemCount
        setLog(TAG, "onCreate: dotscount "+dotscount)
        val dots: Array<ImageView?> = arrayOfNulls(dotscount)
        setUpViewModel()
        setUpCountryData()
        requestSMSPermisstion()


        for (i in 0 until dotscount) {
            dots[i] = ImageView(this)
            dots[i]!!.setImageDrawable(getDrawable(R.drawable.non_active_dot))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            sliderDotspanel.addView(dots[i], params)
        }

        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.offscreenPageLimit = 3
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPager2.setPageTransformer(compositePageTransformer)

        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

            }
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000)
                setLog(TAG, "onPageSelected: position "+position)
                if (position == 0){
                    textView2.text = getString(R.string.login_slider_str_1)
                    textView3.text = getString(R.string.login_slider_str_2)
                }
                else if (position ==1){
                    textView2.text = getString(R.string.login_slider_str_7)
                    textView3.text = getString(R.string.login_slider_str_8)
                }
                else if (position == 2){
                    textView2.text = getString(R.string.login_slider_str_5)
                    textView3.text = getString(R.string.login_slider_str_6)
                }
                for (i in 0 until dotscount) {
                    dots[i]?.setImageDrawable(ContextCompat.getDrawable(this@LoginMainActivity, R.drawable.non_active_dot))
                }

                dots[position]?.setImageDrawable(ContextCompat.getDrawable(this@LoginMainActivity, R.drawable.active_dot))
            }
        })
        action = intent.getIntExtra("action", 0)
        configureGoogleClient()
        configureFacebook()

        auth = FirebaseAuth.getInstance()
        var isClickEnable = true
        btnSendOTP.setOnClickListener {
            if ((selectedCountryCode.equals("+91", true) && etMobileNumber.text.length<10))
                return@setOnClickListener
            else if ((!selectedCountryCode.equals("+91", true) && etMobileNumber.text.length<5))
                return@setOnClickListener
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(this, btnSendOTP!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val number = etMobileNumber.text.trim().toString()
            if (number.isNotEmpty()) {
                //API Call
                if((selectedCountryCode.equals("+91", true) && number.length < 10)){
                    val messageModel = MessageModel(getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
                    CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","onCodeSent")
                }else{
                    //setProgressBarVisible(true)
                    if (isClickEnable) {
                        isClickEnable = false
                        setLog("setCountryAndHE", "btnSendOTP-click-isCountryDataLoaded-$isCountryDataLoaded-heMsisdnWithIsdCode-${heMsisdnWithIsdCode}-heMobileNumber-$heMobileNumber-number-$number")

                        if (isHEDataLoaded && heMobileNumber.equals(number, true) && !heMsisdnWithIsdCode.isNullOrBlank()){
                            setLog("setCountryAndHE", "btnSendOTP-click-callHELogin caled")
                            callHELogin(heMsisdnWithIsdCode)
                        }else{
                            generateOtp(number)
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            isClickEnable = true
                        }, 5000)
                    }
                }
            } else {
                //val messageModel = MessageModel(getString(R.string.login_str_28), MessageType.NEUTRAL, true)
                val messageModel = MessageModel(getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","onCodeSent")
            }
        }

        etMobileNumber.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                setLog("agbasfg", "${s.toString()} $selectedCountryCode")
                if (selectedCountryCode == "+91"){
                    if (s?.length!! >= 10)
                        btnSendOTP.setBackgroundResource(R.drawable.corner_radius_18_bg_blue)
                    else
                        btnSendOTP.background = null
                }
                else{
                    if (s?.length!! >= 5)
                        btnSendOTP.setBackgroundResource(R.drawable.corner_radius_18_bg_blue)
                    else
                        btnSendOTP.background = null
                }

            }
        })

        tvTermCondtion?.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY,"Tapped TnC Continue")
                EventManager.getInstance().sendEvent(TappedTCContinueEvent(hashMap))
            }

        }
        ll_Skip.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String,String>()
                hashMap.put(EventConstant.SOURCE_EPROPERTY,"Skip")
                setLog("TAG","login${hashMap}")
                EventManager.getInstance().sendEvent(SkipEvent(hashMap))
            }
            url = ""
            if(isForAudio)
            {
                overridePendingTransition(R.anim.enter, R.anim.exit)
                finish()
            }
            else
                redirectToHome()
        }
/*        ll_LoginMobile.setOnClickListener {
           callLoginWithMobileActivity()
        }*/
        coordinator.bottomSheetLL.setCornerRadius(resources.getDimensionPixelSize(R.dimen.common_popup_round_corner).toFloat())
        sheetBehavior = BottomSheetBehavior.from<CornerRadiusFrameLayout>(coordinator.bottomSheetLL)
        sheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN // will hide bottom sheet
        //initRecyclerView()

        sheetBehavior?.setPeekHeight(0)

        // fill otp and call the on click on button
        llEmail.setOnClickListener {
            signInWithEmail()
        }

        // click on Google button
        llGoogle.setOnClickListener {
            signInToGoogle()
        }


        // click on facebook button
        llFacebook.setOnClickListener {
            signInToFacebook()
        }

        tvPrivacyPolicy.setOnClickListener{
            CommonUtils.openCommonWebView(Constant.privacyPolicy,this)
        }
        tvTermsOfServices.setOnClickListener {
            CommonUtils.openCommonWebView(Constant.termAndConditionLink,this)
        }

        //click on apple button
        /*llAppleLogin.setOnClickListener {
            signInToApple()
        }*/



//        appleAuthURLFull = "https://appleid.apple.com/auth/authorize?client_id=com.hungama.myplay.staging.signin&redirect_uri=https://www.hungama.com/apple_redirect&response_type=code%20id_token&state=123456&scope=name%20email&response_mode=form_post"

//        val configuration = SignInWithAppleConfiguration(
//            clientId = "com.hungama.myplay.staging.signin",
//            redirectUri = "https://www.hungama.com/apple_redirect",
//            scope = "email"
//        )

        //            redirectUri = "https://user.api.hungama.com:8081/v1/apple-callback",

        val configuration = SignInWithAppleConfiguration(
            clientId = "com.hungama.myplay.staging.signin",
            redirectUri = "https://user.api.hungama.com/v1/apple-callback",
            scope = "email",
        )

//        val configuration = SignInWithAppleConfiguration(
//            clientId = "com.hungama.web.signin",
//            redirectUri = "https://www.hungama.com/apple_redirect",
//            scope = "email"
//        )
        val callback: (SignInWithAppleResult) -> Unit = { result ->
            when (result) {
                is SignInWithAppleResult.Success -> {
                    setLog("SAMPLE_APP", "User Success Apple Sign In:${result.authorizationCode}")
                    val messageModel = MessageModel(result.authorizationCode, MessageType.NEUTRAL, true)
                    CommonUtils.showToast(this, messageModel,"LoginMainActivity","onCreate")

                    callSocialLogin(
                        "",
                        "",
                        result.authorizationCode,
                        "",
                        "",
                        "apple",
                        "",
                        ""
                    )
                }
                is SignInWithAppleResult.Failure -> {
                    setLog("SAMPLE_APP", "Received error from Apple Sign In ${result.error.message}")
                }
                is SignInWithAppleResult.Cancel -> {
                    setLog("SAMPLE_APP", "User canceled Apple Sign In")
                }
            }
        }
        llAppleLogin.setOnClickListener {
            signInToApple()
        }

        ImageLoader.loadImage(
            this,
            imageFlag,
            "https://storage.googleapis.com/static-media-hungama-com/NewFlags/in.png",
            R.drawable.bg_gradient_placeholder
        )
        imageFlag.setOnClickListener {
            //Open Bottom sheet
            hideKeyboard()
            sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED) // Will show the bottom sheet
            sheetBehavior?.setPeekHeight(resources.getDimensionPixelSize(R.dimen.dimen_400))
        }

        etCountryCode.setOnClickListener {

            //Open Bottom sheet
            hideKeyboard()
            sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED) // Will show the bottom sheet
            sheetBehavior?.setPeekHeight(resources.getDimensionPixelSize(R.dimen.dimen_400))
        }

        coordinator.llBtnClose.setOnClickListener{
            sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
        }

        coordinator.bottomSheetLL.setOnTouchListener(View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN // will hide bottom sheet
                    sheetBehavior?.peekHeight = 0
                    hideKeyboard()
                }
                return@OnTouchListener false
            }
            true
        })

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         */
        sheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        backdrop?.hide()
                        hideKeyboard()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        backdrop?.show()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN) // will hide bottom sheet
                        sheetBehavior?.setPeekHeight(0)
                        backdrop?.hide()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED ->{
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        if (action == SIGNIN_WITH_GOOGLE){
            signInToGoogle()
        }else if (action == SIGNIN_WITH_FACEBOOK){
            signInToFacebook()
        }else if (action == SIGNIN_WITH_APPLE){
            signInToApple()
        }else if (action == SIGNIN_WITH_EMAIL){
            callLoginEvent("Email")
            val intent: Intent
            intent = Intent(this@LoginMainActivity, EnterEmailActivity::class.java)
            intent.putExtra("action", action)
            startActivity(intent)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }else if (action == SIGNIN_WITH_MOBILE){
            callLoginWithMobileActivity()
            callLoginEvent("Phone")
            val intent = Intent(this@LoginMainActivity, EnterMobileNumberActivity::class.java)
            intent.putExtra("action", action)
            startActivityForResult(intent, SIGNIN_WITH_MOBILE)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }



        if (Constant.DEFAULT_COUNTRY_CODE.equals("IN", true)){
            clLanguage?.show()
        }else{
            clLanguage?.hide()
        }
        clLanguage?.setOnClickListener {
            hideKeyboard()
            val intent = Intent(this@LoginMainActivity, ChooseLanguageActivity::class.java)
            intent.putExtra("action", action)
            startActivityForResult(intent, SIGNIN_WITH_MOBILE)
            overridePendingTransition(R.anim.enter, R.anim.exit)
        }
        rvLoginPlateformSequence?.apply {
            layoutManager =
                GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
            adapter = LoginPlatformSequenceAdapter(context, loginPlatformSequenceList, object : LoginPlatformSequenceAdapter.OnLoginPlatformClickListener{
                override fun onLoginPlatformClickListener(position: Int) {
                    if (!loginPlatformSequenceList.isNullOrEmpty() && loginPlatformSequenceList.size > position){
                        when(loginPlatformSequenceList.get(position).id){
                            SIGNIN_WITH_EMAIL -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_EMAIL")
                                signInWithEmail()
                            }
                            SIGNIN_WITH_GOOGLE -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_GOOGLE")
                                signInToGoogle()
                            }
                            SIGNIN_WITH_FACEBOOK -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_FACEBOOK")
                                signInToFacebook()
                            }
                            SIGNIN_WITH_APPLE -> {
                                setLog("UserLogin", "LoginMainActivity-OnCreate()-onLoginPlatformClickListene()-SIGNIN_WITH_APPLE")
                                //setUpSignInWithAppleOnClick(supportFragmentManager, configuration, callback)
                                //openCCT()
                                signInToApple()
                            }
                        }
                    }
                }
            })
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }

        if (url.isNotEmpty())
            clLanguage.hide()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    private fun generateOtp(number: String) {
        try {

            login()
        } catch (e: Exception) {
            e.printStackTrace()
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","generateOtp")
        }
    }

    private fun setUpCountryData() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            userViewModel?.getCountryData(this)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {

                                setLog("TAG", "generateOTPCallRespObserver:: $it?.data")
                                countryDataList = it?.data
                                setLog("listOfCountrySearch","setUpCountryData-countryDataList.size-${countryDataList.size}")
                                setCountryCodeListData()
                                isCountryDataLoaded = true
                                setLog("setCountryAndHE", "setUpCountryData-isCountryDataLoaded-$isCountryDataLoaded-isHEDataLoaded-$isHEDataLoaded")


                                for(country in countryDataList){
                                    if(isHEDataLoaded){
                                        if (country.code?.trim().equals(heCountryName?.trim(), true)){
                                            setLog("setCountryAndHE", "countryDataList item:${country}")
                                            selectedCountry=country.name!!
                                            selectedCountryCode = country.dialCode!!
                                            changeTitle(selectedCountryCode)
                                            etCountryCode.setText("(${selectedCountryCode})")
                                            if (!TextUtils.isEmpty(country.flag)){
                                                ImageLoader.loadImage(
                                                    this,
                                                    imageFlag,
                                                    country.flag!!,
                                                    R.drawable.bg_gradient_placeholder
                                                )
                                            }
                                            etMobileNumber?.setText(heMobileNumber)
                                            break
                                        }
                                    }else{
                                        if (country.code.equals(Constant.DEFAULT_COUNTRY_CODE, true)){
                                            selectedCountry=country.name!!
                                            selectedCountryCode = country.dialCode!!
                                            etCountryCode.setText("(${selectedCountryCode})")
                                            changeTitle(selectedCountryCode)
                                            if (!TextUtils.isEmpty(country.flag)){
                                                ImageLoader.loadImage(
                                                    this,
                                                    imageFlag,
                                                    country.flag!!,
                                                    R.drawable.bg_gradient_placeholder
                                                )
                                            }
                                            break
                                        }
                                    }

                                    if (selectedCountryCode == "+91")
                                    {
                                        setMaxMixText(true)
                                        if (etMobileNumber.text?.length!! > 10){
                                        btnSendOTP.background = null
                                        etMobileNumber.setText("")
                                        }
                                    }
                                    else
                                    {
                                        setMaxMixText(false)
                                    }

                                }

//                                setCountryAndHE()
                                initRecyclerView()
                            }

                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","setUpCountryData")
        }
        setProgressBarVisible(true)
    }



    private fun initRecyclerView() {
        layoutManger = LinearLayoutManager(this)
        coordinator.rvCounrtyList.setLayoutManager(layoutManger)

        val countryListAdapter = CountryListAdapter(this).apply {
            itemClick = { countryTitle ->
                setLog(">>>>>>>>>",countryTitle.country)
                selectedCountry=countryTitle.country
                selectedCountryCode = countryTitle.code
                setLog("TAG", "initRecyclerView: "+countryTitle.country)
                setLog("EnterMobileNumber", "initRecyclerView - $selectedCountryCode")

                if (selectedCountryCode == "+91"){
                    if (etMobileNumber.text?.length!! == 10)
                        btnSendOTP.setBackgroundResource(R.drawable.corner_radius_18_bg_blue)
                    else if (etMobileNumber.text?.length!! > 10){
                        btnSendOTP.background = null
                        etMobileNumber.setText("")
                    }
                    else
                        btnSendOTP.background = null
                    setMaxMixText(true)
                }
                else{
                    setMaxMixText(false)

                    if (etMobileNumber.text?.length!! >= 5)
                        btnSendOTP.setBackgroundResource(R.drawable.corner_radius_18_bg_blue)
                    else
                        btnSendOTP.background = null
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String,String>()
                    hashMap.put(EventConstant.ACTION_EPROPERTY,""+selectedCountryCode)
                    setLog("LOGIN","MobileCountrycode${hashMap}")
                    EventManager.getInstance().sendEvent(LoginMobileNumberCountryCodeEvent(hashMap))
                }

                changeTitle(selectedCountryCode)
                etCountryCode.setText("("+selectedCountryCode+")")
                if (!TextUtils.isEmpty(countryTitle.imageURL)){
                    ImageLoader.loadImage(
                        context,
                        imageFlag,
                        countryTitle.imageURL,
                        R.drawable.bg_gradient_placeholder
                    )
                }

                listOfCountry.forEach{it ->
                    setLog("TAG", "initRecyclerView: "+it.country)

                    setLog("TAG", "initRecyclerView: it.country without"+it.country)
                    setLog("TAG", "initRecyclerView: it.code withous"+it.code)
                    setLog("TAG", "initRecyclerView: selectedCountry with"+selectedCountry)
                    setLog("TAG", "initRecyclerView: selectedCountryCode"+selectedCountryCode)
                    if(it.country.equals(selectedCountry)){
                        setLog("TAG", "initRecyclerView: selectedCountry"+selectedCountry)
                        setLog("TAG", "initRecyclerView: it.country"+it.country)
                        it.isSelected=true
                    }else{
                        it.isSelected=false
                    }
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
                hideKeyboard()
                sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
                coordinator.etSearch.setText("")
            }
        }
        coordinator.rvCounrtyList.adapter = countryListAdapter
        countryListAdapter.setCountryList(setCountryCodeListData())

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().length > 0){
//                    listOfCountrySearch = mutableListOf<CountryModel>()
                    setLog("listOfCountrySearch", "text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}")
                    /*listOfCountry?.forEachIndexed { index, it ->
                        if (it.dialCode.equals("+92")){
                            setLog("listOfCountrySearch","pk-true-it-$it-index-$index")
                        }
                     *//*   if(it.country.contains(s.toString(), true)){
                            setLog("listOfCountrySearch", "IF-text-${s}-it-$it-index-$index-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                        else if(it.code.contains("+"+s.toString(),true)){
                            setLog("listOfCountrySearch", "ElseIf-text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(setSearchedList(listOfCountry, s.toString()))
                        }*//*

                    }*/
                    /*listOfCountry.forEach{it ->
                        if (it.code.equals("pk")){
                            setLog("listOfCountrySearch","pk-true-it-$it")
                        }
                        if(it.country.contains(s.toString(), true)){
                            setLog("listOfCountrySearch", "IF-text-${s}-it-$it-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                        else if(it.code.equals("+"+s.toString(),true)){
                            setLog("listOfCountrySearch", "ElseIf-text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                    }*/
                    listOfCountrySearch.clear()
                    listOfCountrySearch.addAll(setSearchedList(listOfCountry, s.toString()))
                    countryListAdapter.setCountryList(listOfCountrySearch)

                }else{
                    listOfCountry.clear()
                    listOfCountrySearch.clear()
                    countryListAdapter.setCountryList(setCountryCodeListData())
                }
                /*Handler(Looper.getMainLooper()).post {
                    countryListAdapter.notifyDataSetChanged()
                }*/

            }
        }
        coordinator.etSearch.addTextChangedListener(textWatcher)

    }
    val listMain = mutableListOf<CountryModel>()

    fun setSearchedList(list:MutableList<CountryModel>, st:String):MutableList<CountryModel>{
        listMain.clear()
        for (item in list){
            if(item.country.lowercase().contains(st.lowercase()) || item.code.contains(st)){
                listMain.add(item)
            }
        }
        return listMain
    }

    fun changeTitle(countryCode: String){
/*        txtTitle.text = if (countryCode == "+91")
            this.getString(R.string.login_str_63)
        else
            this.getString(R.string.login_str_63_1)*/
    }

    fun setMaxMixText(isIndianNumber: Boolean){
        if (isIndianNumber)
        etMobileNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(10))
        else
        etMobileNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(15))
    }

    private fun setCountryCodeListData(): List<CountryModel> {
        listOfCountry = mutableListOf<CountryModel>()
        for(country in countryDataList){
            setLog("TAG", "generateDummyData: "+country.name)
            setLog("TAG", "generateDummyData: "+country.code)
            if (country.name.equals(selectedCountry, false)){
                val countryModel = CountryModel(country.name!!, country.dialCode!!,country.flag!!,true)
                listOfCountry.add(countryModel)
            }else{
                val countryModel = CountryModel(country.name!!, country.dialCode!!,country.flag!!,false)
                listOfCountry.add(countryModel)
            }
        }
        setLog("listOfCountrySearch","generateDummyData-listOfCountry.size-${listOfCountry.size}")
        return listOfCountry
    }
    private fun login() {
        number = etMobileNumber.text.trim().toString()

        setLog("EnterMobileNumber", "login - $selectedCountryCode")
        // get the phone number from edit text and append the country cde with it
        if (number.isNotEmpty()) {
            if ((selectedCountryCode.equals("+91", true) && isValidMobile(number, true)) || number.length > 4) {

                //number = "+91$number"
                number = number
                sendVerificationCode(number, selectedCountryCode.trim())
                //makeSendOtpCall(number, selectedCountryCode.trim())

            }else{
                //Utils.showSnakbar(parentMobile, false, "Enter valid mobile number")
                val messageModel = MessageModel(getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
                CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","login")
            }

        }else{
            val messageModel = MessageModel(getString(R.string.toast_str_33), getString(R.string.toast_str_2),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","login")
            //Utils.showSnakbar(parentMobile, false, "Enter mobile number")
        }
    }

    private fun sendVerificationCode(number: String, countryCode: String) {
        if (ConnectionUtil(this).isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, ""+number)
                setLog("LOGIN", "Mobilefilled${hashMap}")
                EventManager.getInstance().sendEvent(LoginMobileNumberFilledEvent(hashMap))
            }


/*            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(countryCode+number) // Phone number to verify
                .setTimeout(0, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            setLog("GFG", "Auth started")*/

            makeSendOtpCall(number, countryCode)

        }else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","sendVerificationCode")
        }
    }

    fun requestSMSPermisstion()
    {
        val permission = android.Manifest.permission.RECEIVE_SMS
        val grant = ContextCompat.checkSelfPermission(this, permission)
        if(grant != PackageManager.PERMISSION_GRANTED){
            val permission_list = arrayOf(permission)
            ActivityCompat.requestPermissions(this, permission_list, 1)
        }
    }

    private fun isValidMobile(phone: String, isIndianNumber:Boolean): Boolean {
        return Pattern.matches("[1-9][0-9]{9}", phone)
    }

    private fun makeSendOtpCall(number: String, countryCode: String) {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(this).isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                val hashMap = HashMap<String, String>()
                hashMap.put(EventConstant.ACTION_EPROPERTY, ""+number)
                setLog("LOGIN", "Mobilefilled${hashMap}")
                EventManager.getInstance().sendEvent(LoginMobileNumberFilledEvent(hashMap))
            }


            userViewModel?.generateOTPNumber(this, number, countryCode)?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                setLog("kasbgbaslkj", "otpAPi")
                                val intent:Intent
                                intent = Intent(this@LoginMainActivity, MobileOTPVarifyActivity::class.java)
                                intent.putExtra("mobile",number)
                                intent.putExtra("countryCode",countryCode)
                                intent.putExtra("action", action)
                                startActivityForResult(intent, Constant.SIGNIN_WITH_MOBILE)
                                overridePendingTransition(R.anim.enter, R.anim.exit)

                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String, String>()
                                    hashMap.put(EventConstant.SOURCE_EPROPERTY, "Mobile")
                                    setLog("LOGIN", "emailfilled${hashMap}")
                                    EventManager.getInstance().sendEvent(LoginOTPEvent(hashMap))
                                }

                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setProgressBarVisible(false)
                        }
                    }
                })
        } else {
            val messageModel = MessageModel(getString(R.string.toast_message_5), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","makeSendOtpCall")
        }
        setProgressBarVisible(true)
    }

    private fun callLoginWithMobileActivity() {
        callLoginEvent("Phone")
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                CommonUtils.hapticVibration(this, ll_LoginMobile!!,
                    HapticFeedbackConstants.CONTEXT_CLICK
                )
            }
        }catch (e:Exception){

        }
        val intent = Intent(this@LoginMainActivity, EnterMobileNumberActivity::class.java)
        intent.putExtra("action", action)
        intent.putExtra("mainLogin", true)
        if (url.isNotEmpty()) {
            intent.putExtra(Constant.DeepLink_Payment, url)
        }
        startActivityForResult(intent, SIGNIN_WITH_MOBILE)
        overridePendingTransition(R.anim.enter, R.anim.exit)
    }

    fun getHEApiCall(heApi_url: String) {
        if(userViewModel==null){
            userViewModel = ViewModelProvider(
                this
            ).get(UserViewModel::class.java)
        }
        if (ConnectionUtil(this).isOnline) {
            userViewModel?.getHEApiCall(this,heApi_url)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            setLog(TAG, "getHEApiCall resp:${it?.data?.data}")
                            if (it?.data?.data != null)
                                loadHEdata(it?.data?.data)

                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            setProgressBarVisible(false)
                        }
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()

        var heApi_url=""
        if(!TextUtils.isEmpty(Constant.HE_API_URL)){
            heApi_url=Constant.HE_API_URL
        }else{
            heApi_url=CommonUtils.getFirebaseConfigAdsData().he_api.url
        }

        setLog("Login","HEAPI_URL:${heApi_url}")
        if(!TextUtils.isEmpty(heApi_url)){
            getHEApiCall(heApi_url)
        }else{
            setLog("Login","HEAPI_URLnot valid url :${heApi_url}}")
        }
        setLog("displayDiscover", "he_api-${CommonUtils.getFirebaseConfigHEAPIData()}")
    }

    override fun onDestroy() {
        isActiveActivity = false
        val lbm = LocalBroadcastManager.getInstance(this)
        if (receiver != null)
        {
            lbm.unregisterReceiver(receiver as BroadcastReceiver)
        }
        super.onDestroy()
        hideKeyboard()
    }
    private fun setUpViewModel() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        setProgressBarVisible(false)
    }

    private fun callSocialLogin(
        uid: String,
        providerUid: String,
        email: String,
        userName: String,
        userImage: String,
        provider: String,
        firstname:String,
        lastname:String
    ) {
        dismissLoader()
        if (ConnectionUtil(this@LoginMainActivity).isOnline) {

            try {
                val mainJson = JSONObject()
                val clientDataJson = JSONObject()
                val login_provider_uidJson = JSONObject()
                login_provider_uidJson.put(
                    "value",
                    uid
                )
                clientDataJson.put("login_provider_uid", login_provider_uidJson)

                val silentUserIdJson = JSONObject()
                silentUserIdJson.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("silent_user_id", silentUserIdJson)
                val uidJsonObject = JSONObject()
                uidJsonObject.put("value", SharedPrefHelper.getInstance().getUserId())
                clientDataJson.put("uid", uidJsonObject)

                val usernameJson = JSONObject()
                if(!TextUtils.isEmpty(email)){
                    usernameJson.put(
                        "value",
                        email
                    )
                }else{
                    usernameJson.put(
                        "value",
                        uid
                    )
                }

                clientDataJson.put("username", usernameJson)

                val emailJson = JSONObject()
                emailJson.put(
                    "value",
                    email
                )
                clientDataJson.put("email", emailJson)

                val nameJson = JSONObject()
                nameJson.put(
                    "value",
                    firstname + " " + lastname
                )
                clientDataJson.put("name", nameJson)

                val imageJson = JSONObject()
                imageJson.put(
                    "value",
                    userImage
                )
                //clientDataJson.put("image", imageJson)

                val uidJson = JSONObject()
                uidJson.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("uid", uidJson)

                val login_providerJson = JSONObject()
                //facebook //googleplus
                login_providerJson.put(
                    "value",
                    provider
                )
                clientDataJson.put("login_provider", login_providerJson)

                val is_site_uidJson = JSONObject()
                is_site_uidJson.put(
                    "value",
                    false
                )
                clientDataJson.put("is_site_uid", is_site_uidJson)

                mainJson.put("process", "gigya_login")
                mainJson.put("method", "signup_login")
                mainJson.put("client_data", clientDataJson)

                SharedPrefHelper.getInstance().save(PrefConstant.USER_NAME,userName)
                //SharedPrefHelper.getInstance().save(PrefConstant.USER_IMAGE,userImage)
                userViewModel?.socialLogin(
                    this@LoginMainActivity,
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                if (it?.data != null) {
                                    setLog(TAG, "Registration:: provider :${provider} isNewUser:${it?.data?.result?.data?.newUser}")
                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it.data.result.data.newUser.contains("true",true)){
                                        setLog(TAG, "Registration provider:${provider}")
                                        Utils.registerUserMethod_AF(provider)
                                    }else{
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val hashMap1 =
                                                java.util.HashMap<String, String>()
                                            hashMap1.put(EventConstant.METHOD_EPROPERTY,provider)
                                            setLog("LOGIN","Success${hashMap1}")
                                            EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                        }

                                    }
                                    if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-true")
                                        getUserSubscriptionStatus()
                                    }else{
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-false")
                                        redirectToHome()
                                    }
                                }
                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                val messageModel = MessageModel(it?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(this, messageModel,"LoginMainActivity","callSocialLogin")

                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String,String>()
                                    hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,it.message!!)
                                    hashMap.put(EventConstant.SOURCE_EPROPERTY,"Email")
                                    setLog("LOGIN","Success${hashMap}")
                                    EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                                }

                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel,"LoginMainActivity","callSocialLogin")
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"LoginMainActivity","callSocialLogin")
        }
    }

    private fun signInWithEmail(){
        callLoginEvent("Email")
        setLog("PrintUrl", " Login Email " + url.toString())
        val intent: Intent
        intent = Intent(this@LoginMainActivity, EnterEmailActivity::class.java)
        intent.putExtra("action", action)
        if (url.isNotEmpty())
            intent.putExtra(Constant.DeepLink_Payment,  url)
        startActivityForResult(intent, SIGNIN_WITH_EMAIL)
        overridePendingTransition(R.anim.enter, R.anim.exit)
    }

    private fun configureGoogleClient() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInToGoogle() {
        callLoginEvent("Gmail")
        showLoader()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass the activity result back to the Facebook SDK
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGNIN_WITH_GOOGLE || requestCode == SIGNIN_WITH_FACEBOOK
            || requestCode == SIGNIN_WITH_MOBILE || requestCode == SIGNIN_WITH_EMAIL
            || requestCode == SIGNIN_WITH_APPLE || requestCode == SIGNIN_WITH_ALL){
            if (resultCode == Activity.RESULT_OK){

            }else if (resultCode == Activity.RESULT_CANCELED){

            }else{
                action = requestCode
                redirectToHome()
            }
        }
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                //setLog("TAG", "firebaseAuthWithGoogle:" + account.id)
                //firebaseAuthWithGoogle(account.idToken!!, account.id!!)
                parseGoogleData(task)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                setLog("Google sign in failed", e.message.toString())
            } catch (e:Exception){

            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, id: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                dismissLoader()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    setLog("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    setLog(
                        "TAG",
                        "uid-:" + user!!.uid + " provider_uid-:" + idToken + " name-:" + user.displayName + " email-:" + user.email + "name-:" + user.photoUrl
                    )
                    loginSuccess(user, "googleplus", idToken)
                } else {
                    // If sign in fails, display a message to the user.
                    setLog("signInWithCredential:failure", task.exception.toString())
                }
            }
    }

    private fun signInToFacebook() {
        callLoginEvent("Facebook")
        showLoader()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "user_friends","email"))
    }

    fun configureFacebook() {
// Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                setLog("TAG", "facebook:onSuccess:$loginResult")
                //handleFacebookAccessToken(loginResult.accessToken)
                getFacebookProfileData(loginResult)
            }

            override fun onCancel() {
                setLog("TAG", "facebook:onCancel")
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(applicationContext, messageModel,"LoginMainActivity","configureFacebook")
            }

            override fun onError(error: FacebookException) {
                setLog("facebook:onError", error.message.toString())
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(applicationContext, messageModel,"LoginMainActivity","configureFacebook")
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        setLog("TAG", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                dismissLoader()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    setLog("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    setLog(
                        "TAG",
                        "uid-:" + user!!.uid + " provider_uid-:" + token.token + " name-:" + user.displayName + " email-:" + user.email + "name-:" + user.photoUrl
                    )
                    loginSuccess(user, "facebook", token.token)
                } else {
                    // If sign in fails, display a message to the user.
                    setLog("signInWithCredential:failure", task.exception?.message.toString())
                    val messageModel = MessageModel("Authentication failed.", MessageType.NEUTRAL, true)
                    CommonUtils.showToast(applicationContext, messageModel,"LoginMainActivity","handleFacebookAccessToken")
                }
            }
    }


    fun loginSuccess(user: FirebaseUser?, provider: String, providerToken: String) {
        if (user != null) {
            setLog("loginSuccess", "provider-$provider - user.uid-${user.uid} - providerToken-${providerToken} - user.email-${user.email} - user.displayName-${user.displayName} - user.photoUrl-${user.photoUrl}")
            callSocialLogin(
                user.uid.toString(),
                providerToken.toString(),
                user.email.toString(),
                user.displayName.toString(),
                user.photoUrl.toString(),
                provider.toString(),
                user.displayName.toString(),
                user.displayName.toString()
            )
        }

    }

    fun redirectToHome() {
        hideKeyboard()

        val userId = SharedPrefHelper.getInstance().getUserId().toString()
        if (userId.isNotEmpty()){
            AppsFlyerLib.getInstance().setCustomerIdAndLogSession(userId, this)
        }

/*        val appsFlyerUserId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID)
        setLog("appsFlyerUserId", appsFlyerUserId.toString())*/

        if (action == Constant.SIGNIN_WITH_GOOGLE
            || action == Constant.SIGNIN_WITH_FACEBOOK
            || action == Constant.SIGNIN_WITH_MOBILE
            || action == Constant.SIGNIN_WITH_EMAIL
            || action == Constant.SIGNIN_WITH_APPLE
            || action == Constant.SIGNIN_WITH_ALL){
            overridePendingTransition(R.anim.enter, R.anim.exit)
            val returnIntent = Intent()
            returnIntent.putExtra("result", action)
            setResult(action, returnIntent)
            finish()
        }else{
            if (AppsflyerSubscriber.deeplink.isNotEmpty()){
                intent = CommonUtils.getDeeplinkIntentData(Uri.parse(AppsflyerSubscriber.deeplink))
                intent.setClass(this, MainActivity::class.java)
            }
            else{
                intent = Intent(this@LoginMainActivity, MainActivity::class.java)
                if (url.isNotEmpty())
                    intent.putExtra(Constant.DeepLink_Payment, url)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.enter, R.anim.exit)
            finish()
        }

        if(SharedPrefHelper.getInstance().isUserLoggedIn()){
            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.LOG_IN_SOURCE, "onboarding")
            userDataMap.put(EventConstant.LOG_IN_METHOD, SIGNUPMETHOD)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
        }
    }

    fun showLoader() {
        if (!this.isFinishing) {
            DisplayDialog.getInstance(this).showProgressDialog(this@LoginMainActivity, false)
        }
    }

    fun dismissLoader() {
        if (!this.isFinishing) {
            DisplayDialog.getInstance(this).dismissProgressDialog()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setProgressBarVisible(false)
        dismissLoader()

    }
    private fun setProgressBarVisible(it: Boolean) {
        if (it) {
//            showLoader()
            pb_progress?.visibility = View.VISIBLE
        } else {
//            dismissLoader()
            pb_progress?.visibility = View.GONE
        }
    }


    fun signInToApple(){
        setLog("UserLogin", "LoginMainActivity-signInToApple()")
        // Localize the Apple authentication screen in French.
        provider.addCustomParameter("locale", "en")
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                setLog("UserLogin", "LoginMainActivity-checkPending:onSuccess:$authResult")
                // Get the user profile with authResult.getUser() and
                // authResult.getAdditionalUserInfo(), and the ID
                // token from Apple with authResult.getCredential().
                val user = authResult.user
            }.addOnFailureListener { e ->
                setLog("UserLogin", "LoginMainActivity-checkPending:onFailure-${e.message.toString()}")
            }
        } else {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener { authResult ->
                    // Sign-in successful!
                    setLog("UserLogin", "LoginMainActivity-activitySignIn:onSuccess:${authResult.user}")
                    val user = authResult.user
                    loginSuccess(user, "apple", "")
                    // ...
                }
                .addOnFailureListener { e ->
                    setLog("UserLogin", "LoginMainActivity-activitySignIn:onFailure-${e.message.toString()}")
                    val messageModel = MessageModel("Authentication failed.", MessageType.NEUTRAL, true)
                    CommonUtils.showToast(this, messageModel,"LoginMainActivity","signInToApple")
                }
        }
    }

    /**
     * Get user's basic profile data from facebook.
     */
    private fun getFacebookProfileData(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(
            loginResult.accessToken
        ) { `object`, response ->
            parseFacebookData(`object`, AccessToken.getCurrentAccessToken()?.token!!)
            setLog("TAG", "FACEBOOK TOKEN : " + AccessToken.getCurrentAccessToken()?.token!!)
//            try {
//                LoginManager.getInstance().logOut()
//            } catch (ignore: Exception) {
//            }
        }
        val parameters = Bundle()
        val REQUIRED_FIELDS = "fields"
        parameters.putString(REQUIRED_FIELDS, permissions)
        request.parameters = parameters
        request.executeAsync()
    }

    private fun parseFacebookData(graphJson: JSONObject?, token: String) {
        try {
            if (graphJson != null) {
                val ids = graphJson.optString(ID)
                val names = graphJson.optString(NAME)
                val gender = graphJson.optString(GENDER)
                val emails = graphJson.optString(EMAIL)
                val first_name = graphJson.optString(FIRST_NAME)
                val last_name = graphJson.optString(LAST_NAME)
                val image_url = "http://graph.facebook.com/$ids/picture?type=large"
                setLog(
                    "Tag",
                    "FBDATA" + "-->\nId " + ids + "\n-->Name " + names + "\n-->email " + emails + names + "\n-->email " + emails + "\n-->FirstName " + first_name + "\n-->LAstName " + last_name
                )
                SIGNUPMETHOD="Facebook"

                var id = ""
                var idToken = ""
                var email = ""
                var displayName = ""
                var image = ""
                var name = ""
                var fullName = ""

                if (!TextUtils.isEmpty(ids)){
                    id = ids.toString()
                }
                if (!TextUtils.isEmpty(token)){
                    idToken = token.toString()
                }
                if (!TextUtils.isEmpty(emails)){
                    email = emails.toString()
                }
                if (!TextUtils.isEmpty("$first_name$last_name")){
                    displayName = "$first_name $last_name"
                }
                if (image_url != null && !TextUtils.isEmpty(""+image_url)){
                    image = image_url.toString()
                }
                if (!TextUtils.isEmpty(first_name)){
                    name = first_name.toString()
                }
                if (!TextUtils.isEmpty(last_name)){
                    fullName = last_name.toString()
                }
                callSocialLogin(
                    id,
                    idToken,
                    email,
                    displayName,
                    image,
                    "facebook",
                    name,
                    fullName
                )
            }
        }catch (e:Exception){

        }

    }

    /*
     * Facebook JSON fields for user's basic profile.
     */
    companion object FacebookFields {
        internal val ID = "id"
        internal val NAME = "name"
        internal val GENDER = "gender"
        internal val EMAIL = "email"
        internal val FIRST_NAME = "first_name"
        internal val LAST_NAME = "last_name"
        internal val PICTURE = "picture"
        var isActiveActivity = false
//        internal val USER_FRIENDS = "user_friends"

        internal val permissions: String
            get() = ID + "," +
                    NAME + "," +
                    GENDER + "," +
                    EMAIL + "," +
                    FIRST_NAME + "," +
                    LAST_NAME + "," +
                    PICTURE
    }

    private fun parseGoogleData(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            setLog(
                "Tag",
                "GoogleDATA" + "-->\nId " + account!!.id + "\n-->FirstName " + account.givenName + "\n-->LastName " + account.familyName + "\n-->email " + account.email + "\n-->Photo " + account.photoUrl
            )
            //googleSignInClient!!.signOut()
            SIGNUPMETHOD="Google"
            var id = ""
            var idToken = ""
            var email = ""
            var displayName = ""
            var image = ""
            var name = ""
            var fullName = ""

            if (!TextUtils.isEmpty(account.id)){
                id = account.id.toString()
            }
            if (!TextUtils.isEmpty(account.idToken)){
                idToken = account.idToken.toString()
            }
            if (!TextUtils.isEmpty(account.email)){
                email = account.email.toString()
            }
            if (!TextUtils.isEmpty(account.displayName)){
                displayName = account.displayName.toString()
            }
            if (account.photoUrl != null && !TextUtils.isEmpty(""+account.photoUrl)){
                image = account.photoUrl.toString()
            }
            if (!TextUtils.isEmpty(account.givenName)){
                name = account.givenName.toString()
            }
            if (!TextUtils.isEmpty(account.familyName)){
                fullName = account.familyName.toString()
            }
            callSocialLogin(
                id,
                idToken,
                email,
                displayName,
                image,
                "googleplus",
                name,
                fullName
            )


        } catch (e: ApiException) {
            setLog("TAG", " signInResult:failed code=" + e.statusCode)
        } catch (e:Exception){
            setLog("TAG", " signInResult:Exceptione=" + e.message)
        }

    }

    suspend fun isAppleLoggedIn(): Boolean {
        val expireTime = SharedPrefHelper.getInstance().getAppleVerifyRefreshToken()

        val currentTime = System.currentTimeMillis() / 1000L // Check the current Unix Time

        return if (currentTime >= expireTime!!) {
            // After 24 hours validate the Refresh Token and generate new Access Token
            val untilUnixTime =
                currentTime + (60 * 60 * 24) // Execute the method after 24 hours again
            SharedPrefHelper.getInstance().setAppleVerifyRefreshToken(untilUnixTime)
            verifyRefreshToken()
        } else {
            true
        }
    }


    // Show 'Sign in with Apple' login page in a dialog
    @SuppressLint("SetJavaScriptEnabled")
    fun setupAppleWebViewDialog(url: String) {
        appledialog = Dialog(this)
        val webView = WebView(this)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = AppleWebViewClient()
        webView.settings.javaScriptEnabled = true
//        webView.loadUrl(url)
        webView.loadUrl("https://appleid.apple.com/auth/authorize?client_id=com.hungama.myplay.staging.signin&redirect_uri=https%3A%2F%2Fuser.api.hungama.com%2Fv1%2Fapple-callback&response_type=code%20id_token&state=123456&scope=email&response_mode=form_post&frame_id=a3a2fc4d-8624-49d2-b74c-a9ba303d9c5f&m=12&v=1.5.4")
        appledialog.setContentView(webView)
        appledialog.show()
    }

    // A client to know about WebView navigation
    // For API 21 and above
    @Suppress("OverridingDeprecatedMember")
    inner class AppleWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            setLog("AppleLogin", "onPageStarted: request:${url}")
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            setLog("AppleLogin", "onLoadResource: request:${url}")
        }
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            setLog("AppleLogin", "shouldOverrideUrlLoading: request:${request?.url}")

            if (request?.url.toString().startsWith("https://www.hungama.com/")) {

                handleUrl(request?.url.toString())

                // Close the dialog after getting the authorization code
                if (request?.url.toString().contains("success=")) {
                    appledialog.dismiss()
                }
                return true
            }
            return true
        }

        // For API 19 and below
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            setLog("AppleLogin", "shouldOverrideUrlLoading: request:${url}")
            if (url.startsWith("https://www.hungama.com/")) {

                handleUrl(url)

                // Close the dialog after getting the authorization code
                if (url.contains("success=")) {
                    appledialog.dismiss()
                }
                return true
            }
            return false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            setLog("AppleLogin", "onPageFinished: request:${url}")

            // retrieve display dimensions
            val displayRectangle = Rect()
            val window = this@LoginMainActivity.window
            window.decorView.getWindowVisibleDisplayFrame(displayRectangle)

            // Set height of the Dialog to 90% of the screen
            val layoutParams = view?.layoutParams
            layoutParams?.height = (displayRectangle.height() * 0.9f).toInt()
            view?.layoutParams = layoutParams
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            //setLog("requestoo", request?.url.toString() + " ===== " + request?.method)
            //val uri = Uri.parse(request?.url.toString())
            //val args = uri.queryParameterNames
            //setLog("QeuryPara", args.toString())
            //setLog("QeuryPara1", request?.requestHeaders.toString())
            return super.shouldInterceptRequest(view, request)
        }

        // Check WebView url for access token code or error
        @SuppressLint("LongLogTag")
        private fun handleUrl(url: String) {

            val uri = Uri.parse(url)
            // val server = uri.authority
            //val path = uri.path
            //val protocol = uri.scheme
            //val args = uri.queryParameterNames
            //setLog("QeuryParam", args.toString())
            val success = uri.getQueryParameter("success")
            if (success == "true") {

                // Get the Authorization Code from the URL
                appleAuthCode = uri.getQueryParameter("code") ?: ""
                setLog("Apple Code: ", appleAuthCode)

                // Get the Client Secret from the URL
                appleClientSecret = uri.getQueryParameter("client_secret") ?: ""
                setLog("Apple Client Secret: ", appleClientSecret)

                // Save the Client Secret (appleClientSecret) using SharedPreferences
                // This will allow us to verify if refresh Token is valid every time they open the app after cold start.
                SharedPrefHelper.getInstance().setAppleClientSecret(appleClientSecret)

                //Check if user gave access to the app for the first time by checking if the url contains their email
                if (url.contains("email")) {

                    //Get user's First Name
                    val firstName = uri.getQueryParameter("first_name")
                    setLog("Apple User First Name: ", firstName ?: "")
                    appleFirstName = firstName ?: "Not exists"

                    //Get user's Middle Name
                    val middleName = uri.getQueryParameter("middle_name")
                    setLog("Apple User Middle Name: ", middleName ?: "")
                    appleMiddleName = middleName ?: "Not exists"

                    //Get user's Last Name
                    val lastName = uri.getQueryParameter("last_name")
                    setLog("Apple User Last Name: ", lastName ?: "")
                    appleLastName = lastName ?: "Not exists"

                    //Get user's email
                    val email = uri.getQueryParameter("email")
                    setLog("Apple User Email: ", email ?: "Not exists")
                    appleEmail = email ?: ""
                }

                // Exchange the Auth Code for Access Token
                requestForAccessToken(appleAuthCode, appleClientSecret)
            } else if (success == "false") {
                setLog("ERROR", "We couldn't get the Auth Code")
            }
        }
    }

    private fun requestForAccessToken(code: String, clientSecret: String) {

        val grantType = "authorization_code"

        val postParamsForAuth =
            "grant_type=" + grantType + "&code=" + code + "&redirect_uri=" + AppleConstants.REDIRECT_URI + "&client_id=" + AppleConstants.CLIENT_ID + "&client_secret=" + clientSecret

        CoroutineScope(Dispatchers.Default).launch {
            val httpsURLConnection =
                withContext(Dispatchers.IO) { URL(AppleConstants.TOKENURL).openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded"
            )
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true
            withContext(Dispatchers.IO) {
                val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
                outputStreamWriter.write(postParamsForAuth)
                outputStreamWriter.flush()
            }
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8

            val jsonObject = JSONTokener(response).nextValue() as JSONObject

            val accessToken = jsonObject.getString("access_token") // Here is the access token
            setLog("Apple Access Token is: ", accessToken)
            appleAccessToken = accessToken

            val expiresIn = jsonObject.getInt("expires_in") // When the access token expires
            setLog("expires in: ", expiresIn.toString())

            val refreshToken =
                jsonObject.getString("refresh_token") // The refresh token used to regenerate new access tokens. Store this token securely on your server.
            setLog("refresh token: ", refreshToken)

            // Save the RefreshToken Token (refreshToken) using SharedPreferences
            // This will allow us to verify if refresh Token is valid every time they open the app after cold start.
            SharedPrefHelper.getInstance().setAppleRefreshToken(refreshToken)


            val idToken =
                jsonObject.getString("id_token") // A JSON Web Token that contains the user’s identity information.
            setLog("ID Token: ", idToken)

            // Get encoded user id by splitting idToken and taking the 2nd piece
            val encodedUserID = idToken.split(".")[1]

            //Decode encodedUserID to JSON
            val decodedUserData = String(Base64.decode(encodedUserID, Base64.DEFAULT))
            val userDataJsonObject = JSONObject(decodedUserData)
            // Get User's ID
            val userId = userDataJsonObject.getString("sub")
            setLog("Apple User ID :", userId)
            appleId = userId

            withContext(Dispatchers.Main) {
                //openDetailsActivity()
                /*callSocialLogin(
                    appleId,
                    appleAccessToken,
                    appleEmail,
                    appleFirstName + " " + appleLastName,
                    "",
                    "apple"
                )*/
            }
        }
    }

    private fun openDetailsActivity() {
        val myIntent = Intent(baseContext, MainActivity::class.java)
        myIntent.putExtra("apple_id", appleId)
        myIntent.putExtra("apple_first_name", appleFirstName)
        myIntent.putExtra("apple_middle_name", appleMiddleName)
        myIntent.putExtra("apple_last_name", appleLastName)
        myIntent.putExtra("apple_email", appleEmail)
        myIntent.putExtra("apple_access_token", appleAccessToken)
        startActivity(myIntent)
    }

    private suspend fun verifyRefreshToken(): Boolean {

        // Verify Refresh Token only once a day
        val refreshToken = SharedPrefHelper.getInstance().getAppleRefreshToken()
        val clientSecret = SharedPrefHelper.getInstance().getAppleClientSecret()

        val postParamsForAuth =
            "grant_type=refresh_token" + "&client_id=" + AppleConstants.CLIENT_ID + "&client_secret=" + clientSecret + "&refresh_token=" + refreshToken

        val httpsURLConnection =
            withContext(Dispatchers.IO) { URL(AppleConstants.TOKENURL).openConnection() as HttpsURLConnection }
        httpsURLConnection.requestMethod = "POST"
        httpsURLConnection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded"
        )
        httpsURLConnection.doInput = true
        httpsURLConnection.doOutput = true
        withContext(Dispatchers.IO) {
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            outputStreamWriter.write(postParamsForAuth)
            outputStreamWriter.flush()

        }
        return try {
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            val jsonObject = JSONTokener(response).nextValue() as JSONObject
            val newAccessToken = jsonObject.getString("access_token")
            //Replace the Access Token on your server with the new one
            setLog("New Access Token: ", newAccessToken)
            true
        } catch (e: Exception) {
            setLog(
                "ERROR: ",
                "Refresh Token has expired or user revoked app credentials"
            )
            false
        }
    }

//    fun setUpSignInWithAppleOnClick(
//        fragmentManager: FragmentManager,
//        configuration: SignInWithAppleConfiguration,
//        callback: (SignInWithAppleResult) -> Unit
//    ) {
//        callLoginEvent("Apple")
//        val fragmentTag = "SignInWithAppleButton-SignInWebViewDialogFragment"
//        val service = SignInWithAppleService(fragmentManager, fragmentTag, configuration, callback)
//        service.show()
//    }

    //    fun setUpSignInWithAppleOnClick(
//        fragmentManager: FragmentManager,
//        configuration: SignInWithAppleConfiguration,
//        callback: SignInWithAppleCallback
//    ) {
//        setUpSignInWithAppleOnClick(fragmentManager, configuration, callback.toFunction())
//    }
    private val sliderRunnable =
        java.lang.Runnable { viewPager2.currentItem = viewPager2.currentItem + 1 }

    fun getUserSubscriptionStatus() {
        val userSubscriptionViewModel = ViewModelProvider(
            this
        ).get(UserSubscriptionViewModel::class.java)
        if (ConnectionUtil(this).isOnline) {
            userSubscriptionViewModel?.getUserSubscriptionStatusDetail(this)?.observe(this,
                Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            setProgressBarVisible(false)
                            redirectToHome()
                        }

                        Status.LOADING -> {
                            setProgressBarVisible(true)
                        }

                        Status.ERROR -> {
                            redirectToHome()
                            setProgressBarVisible(false)
                        }
                    }
                })
        }
    }



    private fun loadHEdata(data: HERespModel.Data?) {
        val userDataMap = java.util.HashMap<String, String>()
        if(!data?.msisdn.isNullOrEmpty() &&!data?.msisdn?.equals("9999999999",true)!!){
            isHEDataLoaded = true
            heCountryCode = data.isdCode
            heCountryName = data.country
            heMobileNumber = data.msisdn
            heMsisdnWithIsdCode = data.msisdn
            if (heMobileNumber.length>10){
                heMobileNumber = heMobileNumber.drop(2)
            }
            tv_send_otp?.text = getString(R.string.download_str_9)
/*            tv_select_language?.text = getString(R.string.login_via_mobile_number)
            txtTitle?.text = getString(R.string.login_via_mobile_number_subtext)*/
            setLog("setCountryAndHE", "loadHEdata-isCountryDataLoaded-$isCountryDataLoaded-isHEDataLoaded-$isHEDataLoaded")
            setCountryAndHE()
            userDataMap.put(EventConstant.HE_AVAILABLE, ""+true)
        }else{
            userDataMap.put(EventConstant.HE_AVAILABLE, ""+false)
        }
        EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
    }

    private fun setCountryAndHE(){
        setLog("setCountryAndHE", "setCountryAndHE-loadHEdata-isCountryDataLoaded-${isCountryDataLoaded}-isHEDataLoaded-${isHEDataLoaded} heCountryName:${heCountryName} heCountryCode${heCountryCode}")

        if (countryDataList?.size!!>0){
            for(country in countryDataList){
                if (isHEDataLoaded&&country.code.equals(heCountryName, true)){
                    setLog("setCountryAndHE", "countryDataList item:${country}")
                    selectedCountry=country.name!!
                    selectedCountryCode = country.dialCode!!
                    etCountryCode.setText("(${selectedCountryCode})")
                    changeTitle(selectedCountryCode)
                    if (!TextUtils.isEmpty(country.flag)){
                        ImageLoader.loadImage(
                            this,
                            imageFlag,
                            country.flag!!,
                            R.drawable.bg_gradient_placeholder
                        )
                    }
                    etMobileNumber?.setText(heMobileNumber)
                    break
                }
            }
        }else if (isHEDataLoaded){
            selectedCountry=heCountryName
            selectedCountryCode = heCountryCode
            etCountryCode.setText("(+${selectedCountryCode})")
            etMobileNumber?.setText(heMobileNumber)
        }
    }

    private fun callHELogin(data: HERespModel.Data) {

        if (ConnectionUtil(this@LoginMainActivity).isOnline) {
            showLoader()
            try {
                val jsonObject: JSONObject=JSONObject()

                jsonObject.put("silent_user_id",SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,""))


                val mainJson = JSONObject()
                val clientDataJson = JSONObject()
                val username = JSONObject()
                username.put(
                    "value",
                    "+"+data?.msisdnWithIsdCode
                )
                mainJson.put("process", "mobile_login")
                mainJson.put("HE", true)
                clientDataJson.put("username", username)



                val silent_user_id = JSONObject()
                silent_user_id.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("silent_user_id", silent_user_id)



                mainJson.put("method", "signup_login")
                mainJson.put("client_data", clientDataJson)

                setLog("TAG", "SSOmobileLogin HE:: ${mainJson}")
                userViewModel?.SSOmobileLogin(
                    this@LoginMainActivity,
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                if (it?.data != null) {
                                    setLog("TAG", "socialLoginRespObserver:: $it?.data")
                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it.data.result.data.newUser.contains("true",true)){
                                        setLog(TAG, "callSocialLogin provider:${provider}")
                                        Utils.registerUserMethod_AF(
                                            "HE"
                                        )
                                    }else{
                                        val hashMap1 =
                                            java.util.HashMap<String, String>()
                                        hashMap1.put(EventConstant.METHOD_EPROPERTY,"HE")
                                        setLog("LOGIN","Success${hashMap1}")
                                        EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                    }
                                    if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-true")
                                        getUserSubscriptionStatus()
                                    }else{
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-false")
                                        redirectToHome()
                                    }
                                }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                val messageModel = MessageModel(it?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(this, messageModel,"LoginMainActivity","callHELogin Error")

                                val hashMap = HashMap<String,String>()
                                hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,it.message!!)
                                hashMap.put(EventConstant.SOURCE_EPROPERTY,"HE")
                                setLog("LOGIN","Success${hashMap}")
                                EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel,"LoginMainActivity","callHELogin")
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"LoginMainActivity","callHELogin")
        }
    }

    private fun callHELogin(heMsisdnWithIsdCode:String) {
        if(action==0){
            action=Constant.SIGNIN_WITH_HE
        }

        if (ConnectionUtil(this@LoginMainActivity).isOnline) {
            showLoader()
            try {
                val jsonObject: JSONObject = JSONObject()

                jsonObject.put("silent_user_id",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,""))


                val mainJson = JSONObject()
                val clientDataJson = JSONObject()
                val username = JSONObject()
                username.put(
                    "value",
                    "+"+heMsisdnWithIsdCode
                )
                mainJson.put("process", "mobile_login")
                mainJson.put("HE", true)
                clientDataJson.put("username", username)



                val silent_user_id = JSONObject()
                silent_user_id.put(
                    "value",
                    SharedPrefHelper.getInstance().get(PrefConstant.SILENT_USER_ID,"")
                )
                clientDataJson.put("silent_user_id", silent_user_id)



                mainJson.put("method", "signup_login")
                mainJson.put("client_data", clientDataJson)

                setLog("TAG", "SSOmobileLogin HE:: ${mainJson}")
                userViewModel?.SSOmobileLogin(
                    this@LoginMainActivity,
                    mainJson.toString()
                )?.observe(this,
                    Observer {
                        when(it.status){
                            Status.SUCCESS->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                if (it?.data != null) {
                                    setLog("TAG", "socialLoginRespObserver:: $it?.data")
                                    if(it?.data?.result?.data?.newUser!=null&&!TextUtils.isEmpty(it?.data?.result?.data?.newUser) && it.data.result.data.newUser.contains("true",true)){
                                        Utils.registerUserMethod_AF(
                                            "HE"
                                        )
                                    }else{
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val hashMap1 =
                                                java.util.HashMap<String, String>()
                                            hashMap1.put(EventConstant.METHOD_EPROPERTY,"HE")
                                            setLog("LOGIN","Success${hashMap1}")
                                            EventManager.getInstance().sendEvent(LoginSuccessEvent(hashMap1))
                                        }

                                    }
                                    if (SharedPrefHelper.getInstance().isUserLoggedIn()){
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-true")
                                        getUserSubscriptionStatus()
                                    }else{
                                        setLog("LoginSubscription", "LoginMainActivity-socialLogin-isUserLogdIn-false")
                                        redirectToHome()
                                    }
                                }

                            }

                            Status.LOADING ->{
                                setProgressBarVisible(true)
                            }

                            Status.ERROR ->{
                                setProgressBarVisible(false)
                                dismissLoader()
                                val messageModel = MessageModel(it?.message.toString(), MessageType.NEUTRAL, true)
                                CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","callHELogin")
                                CoroutineScope(Dispatchers.IO).launch {
                                    val hashMap = HashMap<String,String>()
                                    hashMap.put(EventConstant.ERRORTYPE_EPROPERTY,it.message!!)
                                    hashMap.put(EventConstant.SOURCE_EPROPERTY,"HE")
                                    setLog("LOGIN","Success${hashMap}")
                                    EventManager.getInstance().sendEvent(LoginErrorEvent(hashMap))
                                }

                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","callHELogin")
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"EnterMobileNumberActivity","callHELogin")
        }
    }

    private fun callLoginEvent(loginWith: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap=HashMap<String,String>()
            dataMap.put(EventConstant.LOGIN_EPROPERTY,"false")
            dataMap.put(EventConstant.METHOD_EPROPERTY,loginWith)
            dataMap.put(EventConstant.SCREEN_NAME_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
            dataMap.put(EventConstant.SOURCE_EPROPERTY,EventConstant.SOURCE_ONBOARDING)
            EventManager.getInstance().sendEvent(LoginEvent(dataMap))
        }
    }

    private var GFG_URI = "https://appleid.apple.com/auth/authorize?client_id=com.hungama.myplay.staging.signin&redirect_uri=https%3A%2F%2Fuser.api.hungama.com%2Fv1%2Fapple-callback&response_type=code%20id_token&state=123456&scope=email&response_mode=form_post&frame_id=a3a2fc4d-8624-49d2-b74c-a9ba303d9c5f&m=12&v=1.5.4"
    private var package_name = "com.android.chrome";
    fun openCCT(){
        val builder = CustomTabsIntent.Builder()

        // to set the toolbar color use CustomTabColorSchemeParams
        // since CustomTabsIntent.Builder().setToolBarColor() is deprecated

        val params = CustomTabColorSchemeParams.Builder()
        params.setToolbarColor(ContextCompat.getColor(this@LoginMainActivity, R.color.colorPrimary))
        builder.setDefaultColorSchemeParams(params.build())

        // shows the title of web-page in toolbar
        builder.setShowTitle(true)

        // setShareState(CustomTabsIntent.SHARE_STATE_ON) will add a menu to share the web-page
        builder.setShareState(CustomTabsIntent.SHARE_STATE_OFF)

        // To modify the close button, use
        // builder.setCloseButtonIcon(bitmap)

        // to set weather instant apps is enabled for the custom tab or not, use
        builder.setInstantAppsEnabled(true)

        //  To use animations use -
        //  builder.setStartAnimations(this, android.R.anim.start_in_anim, android.R.anim.start_out_anim)
        //  builder.setExitAnimations(this, android.R.anim.exit_in_anim, android.R.anim.exit_out_anim)
        val customBuilder = builder.build()

        if (isPackageInstalled(package_name)) {
            // if chrome is available use chrome custom tabs
            customBuilder.intent.setPackage(package_name)
            customBuilder.launchUrl(this, Uri.parse(GFG_URI))
        } else {
            // if not available use WebView to launch the url
        }
    }
}