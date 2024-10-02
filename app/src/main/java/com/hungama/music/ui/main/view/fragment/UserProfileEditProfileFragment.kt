package com.hungama.music.ui.main.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hungama.music.data.model.*
import com.hungama.music.data.webservice.utils.Status
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.LoginMobileNumberCountryCodeEvent
import com.hungama.music.ui.base.BaseActivity
import com.hungama.music.ui.base.BaseFragment
import com.hungama.music.ui.main.adapter.CountryListAdapter
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.ui.main.viewmodel.UserViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.applyButtonTheme
import com.hungama.music.utils.CommonUtils.hideKeyboard
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.CommonUtils.showKeyboard
import com.hungama.music.utils.customview.bottomsheet.CornerRadiusFrameLayout
import com.hungama.music.utils.customview.customspinnerview.OnSpinnerItemSelectedListener
import com.hungama.music.utils.customview.customspinnerview.SpinnerObserver
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import com.hungama.music.datepicker.utils.DateUtils
import com.hungama.music.datepicker.view.datePicker.DatePicker
import com.hungama.music.datepicker.view.popup.DatePickerPopup
import kotlinx.android.synthetic.main.activity_enter_mobile.*
import kotlinx.android.synthetic.main.common_header_action_bar.*
import kotlinx.android.synthetic.main.fragment_user_profile_edit_profile.*
import kotlinx.android.synthetic.main.fragment_user_profile_edit_profile.backdrop
import kotlinx.android.synthetic.main.fragment_user_profile_edit_profile.imageFlag
import kotlinx.android.synthetic.main.fragment_user_profile_edit_profile.etCountryCode
import kotlinx.android.synthetic.main.list_bottom_sheet.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.regex.Pattern


class UserProfileEditProfileFragment : BaseFragment(), SpinnerObserver,
    TextView.OnEditorActionListener, BaseActivity.OnLocalBroadcastEventCallBack {
    var popupWindow = PopupWindow()
    var isSpGenderShowing = false
    var userViewModel: UserViewModel? = null
    var email = ""
    var mobile = ""
    var spinner_pos = 0
    var isCountryDataLoaded = false
    var heCountryCode = ""
    var heCountryName = ""
    var heMobileNumber = ""
    var heMsisdnWithIsdCode = ""
    var isHEDataLoaded = false
    var selectedCountry : String ="India"
    var selectedCountryCode : String ="+91"
    var listOfCountry = mutableListOf<CountryModel>()
    var listOfCountrySearch = mutableListOf<CountryModel>()
    var layoutManger: LinearLayoutManager? = null
    var countryDataList = CountryDataModel()
    private lateinit var sheetBehavior: BottomSheetBehavior<CornerRadiusFrameLayout>
    private var datePickerPopup: DatePickerPopup? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideBottomNavigationAndMiniplayer()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile_edit_profile, container, false)
    }


    override fun initializeComponent(view: View) {
        applyButtonTheme(requireContext(), llSaveButton)
        getUserProfile()
        ivBack?.setOnClickListener { v -> backPress() }
        tvActionBarHeading?.text = getString(R.string.profile_str_20)
        //tvActionBarHeading?.text = "dfkjdajsfkljsajkfhdskjruiehfjkdshkf"
        tvActionBarHeading?.setPadding(70,0,0,0)


        if (spGender != null) {

            llDob.visibility = View.GONE
            spGender.setOnSpinnerItemSelectedListener<String> { _, _, index, text ->
                //Toast.makeText(requireContext(), index.toString() + "-" + text, Toast.LENGTH_SHORT).show()
                blurViewGender.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewGender.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewGender.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
                blurViewGender.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            }
            spGender.attachSpinnerObserver(this)


            spGender.setIsFocusable(true)
            //spGender.selectItemByIndex(0)
            text_date.setOnClickListener(this)
            ivDob.setOnClickListener(this)
            llSaveButton?.setOnClickListener(this)
            etName?.setOnEditorActionListener(this)
            ivEditUsername.setOnClickListener(this)
            ivUsernameEdit.setOnClickListener(this)
            ivUserEmailEdit.setOnClickListener(this)
            ivUserMobileEdit.setOnClickListener(this)

        }
        rlMain.bottomSheetLL.setCornerRadius(resources.getDimensionPixelSize(R.dimen.common_popup_round_corner).toFloat())
        sheetBehavior = BottomSheetBehavior.from<CornerRadiusFrameLayout>(rlMain.bottomSheetLL)
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN // will hide bottom sheet
        ImageLoader.loadImage(
            requireContext(),
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

        rlMain.llBtnClose.setOnClickListener{
            sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
        rlMain.bottomSheetLL.setOnTouchListener(View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (sheetBehavior?.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior?.setState(BottomSheetBehavior.STATE_HIDDEN) // will hide bottom sheet
                    sheetBehavior?.setPeekHeight(0)
                }
                return@OnTouchListener false
            }
            true
        })

        sheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        backdrop?.hide()
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

        /*val calendar = Calendar.getInstance()
        calendar.timeInMillis = DateUtils.getCurrentTime()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH]
        val year = calendar[Calendar.YEAR]
        text_date.text = ""+year+"-" + (month + 1) + "-" + day*/
        val typeface = ResourcesCompat.getFont(
            requireContext(),
            R.font.sf_pro_text_semibold
        )
        datepicker.offset = 2
        datepicker.setTextFont(typeface)
        datepicker.maxDate = DateUtils.getCurrentTime()
        datepicker.date = DateUtils.getCurrentTime()
        datepicker.minDate = DateUtils.getTimeMiles(1948, 8,15)
        datepicker.setPickerMode(DatePicker.MONTH_ON_FIRST)
        datepicker.setDataSelectListener { date, day, month, year ->
            /*Toast.makeText(
                requireContext(),
                "" + day + "/" + (month + 1) + "/" + year,
                Toast.LENGTH_SHORT
            ).show()*/
        if(text_date!=null && year !=null && month !=null &&day !=null){
            val fDate: String = SimpleDateFormat("yyyy-MM-dd").format(date)
//            text_date.text = ""+year+"-" + (month + 1) + "-" + day
            text_date.text = fDate
        }

        }

        CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
            resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
            resources.getDimensionPixelSize(R.dimen.dimen_0), 0)

        CommonUtils.PageViewEvent("","","","",
            "viewprofile","user profile_edit profile","")
    }

    override fun onClick(v: View) {
        super.onClick(v)
        if (v==ivEditUsername){
            etName.isEnabled = true
            etName.requestFocus()
            etName.setSelection(etName.text.toString().length)
            requireContext()?.showKeyboard(etName)
        }else if(v==text_date || v==ivDob){
            llDob.visibility = View.VISIBLE
        }else if(v==llSaveButton){
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), llSaveButton!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            llDob.visibility = View.GONE
           if(userViewModel!=null){
               callEditProfile()
           }
        }else if (v == ivUsernameEdit){
            edtUsername?.isEnabled = true
            edtUsername.requestFocus()
            edtUsername.setSelection(edtUsername.text.toString().length)
            requireContext()?.showKeyboard(edtUsername)
        }
        else if (v == ivUserEmailEdit){
            edtUserEmail?.isEnabled = true
            edtUserEmail.requestFocus()
            edtUserEmail.setSelection(edtUserEmail.text.toString().length)
            requireContext()?.showKeyboard(edtUserEmail)
        }
        else if (v == ivUserMobileEdit){
            edtUserMobile?.isEnabled = true
            edtUserMobile.requestFocus()
            edtUserMobile.setSelection(edtUserMobile.text.toString().length)
            requireContext()?.showKeyboard(edtUserMobile)
        }
    }

    private fun callEditProfile() {
        hideKeyboard()
        etName?.clearFocus()
        try {
            val mainJson = JSONObject()

            if (email.isEmpty() && edtUserEmail.text.toString().isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(edtUserEmail.text.toString()).matches()){
                val messageModel = MessageModel(getString(R.string.toast_str_1),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel,"UserProfileEditProfileFragment","callEditProfile")
                return
            }
            if (email.isEmpty() && edtUserEmail.text.toString().isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(edtUserEmail.text.toString()).matches()){
                mainJson.put("email", edtUserEmail?.text?.trim()?.toString())
            }

            if (mobile.isEmpty() && edtUserMobile.text.toString().isNotEmpty() && edtUserMobile.text.toString().length < 10 && !checkPhone(edtUserMobile.text.toString())){
                val messageModel = MessageModel(getString(R.string.please_enter_a_valid_phone_number), MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel,"UserProfileEditProfileFragment","callEditProfile")
                return
            }
            if (mobile.isEmpty() && edtUserMobile.text.toString().isNotEmpty() && edtUserMobile.text.toString().length >= 10 && checkPhone(edtUserMobile.text.toString())){
             val selectedCountryCode1 = selectedCountryCode
                mainJson.put("phone", selectedCountryCode1.replace("+", "") + edtUserMobile?.text?.trim()?.toString())
            }

            mainJson.put("handleName", edtUsername?.text?.trim()?.toString())

            if(spinner_pos.equals(0)){
                mainJson.put("gender", "M")
            }else if(spinner_pos.equals(1)){
                mainJson.put("gender", "F")
            }else if(spinner_pos.equals(2)){
                mainJson.put("gender", "U")
            }else{
                val messageModel = MessageModel(getString(R.string.please_select_gender),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel,"UserProfileEditProfileFragment","callEditProfile")
                return
            }

            if (!TextUtils.isEmpty(text_date?.text)){
                mainJson.put("dob", text_date?.text)
            }else{
                val messageModel = MessageModel(getString(R.string.please_select_date_of_birth),
                    MessageType.NEUTRAL, true)
                CommonUtils.showToast(requireContext(), messageModel,"UserProfileEditProfileFragment","callEditProfile")
                return
            }


            val displayName = etName?.text
            val parts  = displayName?.split(" ")?.toMutableList()
            val firstName = parts?.firstOrNull()
            parts?.removeAt(0)
            val lastName = parts?.joinToString(" ")
            mainJson.put("firstName", firstName)
            mainJson.put("lastName", lastName)
            userViewModel?.editProfile(
                requireContext(),
                mainJson
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it != null) {
                                if (!TextUtils.isEmpty(it?.data?.message)){
                                    val messageModel = MessageModel(it?.data?.message.toString(), MessageType.NEUTRAL, true)
                                    CommonUtils.showToast(requireContext(), messageModel,"UserProfileEditProfileFragment","callEditProfile")
                                }
                                val intent = Intent(Constant.EDIT_PROFILE_EVENT)
                                intent.putExtra("EVENT", Constant.EDIT_PROFILE_RESULT_CODE)
                                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                                backPress()
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

        } catch (e: Exception) {
            e.printStackTrace()
            Utils.showSnakbar(requireContext(),
                requireView(),
                false,
                getString(R.string.discover_str_2)
            )
        }
    }

    fun checkPhone(phone:String):Boolean{
            if (phone.isNotEmpty() && (phone.first().toString() == "9" || phone.first().toString() == "8" || phone.first().toString() == "7" || phone.first().toString() == "6"))
                return true
        return false
    }

    private fun setUpCountryData() {
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(requireContext()).isOnline) {
            userViewModel?.getCountryData(requireContext())?.observe(this,
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


                                for(country in countryDataList){
                                    /*if(isHEDataLoaded){
                                        if (country.code?.trim()?.contains(heCountryName.trim()) == true){
                                            setLog("setCountryAndHE", "countryDataList item:${country}")
                                            selectedCountry=country.name!!
                                            selectedCountryCode = country.dialCode!!
                                            etCountryCode.setText("(${selectedCountryCode})")
                                            if (!TextUtils.isEmpty(country.flag)){
                                                ImageLoader.loadImage(
                                                    requireContext(),
                                                    imageFlag,
                                                    country.flag!!,
                                                    R.drawable.bg_gradient_placeholder
                                                )
                                            }
                                            etMobileNumber?.setText(heMobileNumber)
                                            break
                                        }
                                    }else{*/
                                        if (country.code.equals(Constant.DEFAULT_COUNTRY_CODE, true)){
                                            selectedCountry=country.name!!
                                            selectedCountryCode = country.dialCode!!
                                            etCountryCode.setText("(${selectedCountryCode})")
                                            if (!TextUtils.isEmpty(country.flag)){
                                                ImageLoader.loadImage(
                                                    requireContext(),
                                                    imageFlag,
                                                    country.flag!!,
                                                    R.drawable.bg_gradient_placeholder
                                                )
                                            }
                                            break
                                        }
//                                    }


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
            CommonUtils.showToast(requireContext(), messageModel,"EnterMobileNumberActivity","setUpCountryData")
        }
        setProgressBarVisible(true)
    }

    private fun initRecyclerView() {
        layoutManger = LinearLayoutManager(requireContext())
        rlMain.rvCounrtyList.setLayoutManager(layoutManger)

        val countryListAdapter = CountryListAdapter(requireContext()).apply {
            itemClick = { countryTitle ->
                setLog(">>>>>>>>>",countryTitle.country)
                selectedCountry=countryTitle.country
                selectedCountryCode = countryTitle.code
                setLog("EnterMobileNumber", "initRecyclerView $countryTitle")
                CoroutineScope(Dispatchers.IO).launch {
                    val hashMap = HashMap<String,String>()
                    hashMap.put(EventConstant.ACTION_EPROPERTY,""+selectedCountryCode)
                    setLog("LOGIN","MobileCountrycode${hashMap}")
//                    EventManager.getInstance().sendEvent(LoginMobileNumberCountryCodeEvent(hashMap))
                }


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
                rlMain.etSearch.setText("")
            }
        }
        rlMain.rvCounrtyList.adapter = countryListAdapter
        countryListAdapter.setCountryList(setCountryCodeListData())

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().length > 0){
                    listOfCountrySearch = mutableListOf<CountryModel>()
                    setLog("listOfCountrySearch", "text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}")
                    listOfCountry?.forEachIndexed { index, it ->
                        if (it.dialCode.equals("+92")){
                            setLog("listOfCountrySearch","pk-true-it-$it-index-$index")
                        }
                        if(it.country.contains(s.toString(), true)){
                            setLog("listOfCountrySearch", "IF-text-${s}-it-$it-index-$index-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                        else if(it.code.equals("+"+s.toString(),true)){
                            setLog("listOfCountrySearch", "ElseIf-text-${s}-listOfCountrySearch.size-${listOfCountrySearch.size}-listOfCountry.size-${listOfCountry.size}")
                            listOfCountrySearch.addAll(listOf(it))
                        }
                    }
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
        rlMain.etSearch.addTextChangedListener(textWatcher)

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

    private fun isValidMobile(phone: String, isIndianNumber:Boolean): Boolean {
        return Pattern.matches("[1-9][0-9]{9}", phone)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onDestroy() {
        showBottomNavigationAndMiniplayer()
        hideKeyboard()
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            showBottomNavigationAndMiniplayer()
        }else{
            hideBottomNavigationAndMiniplayer()
        }
    }

    private fun getContextColor(@ColorRes resource: Int): Int {
        return ContextCompat.getColor(requireActivity(), resource)
    }

    override fun onSpinnerObserver(isShown: Boolean) {
        setLog("datePickerCustom", "isShown-$isShown")
        if (isShown){
            blurViewGender.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat())
            blurViewGender.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_0).toFloat())
            spGender.spinnerPopupBackgroundColor = ContextCompat.getColor(requireContext(), R.color.home_bg_color)
        }else{
            blurViewGender.setTopLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setTopRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setBottomLeftRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            blurViewGender.setBottomRightRadius(resources.getDimensionPixelSize(R.dimen.dimen_7).toFloat())
            spGender.spinnerPopupBackgroundColor = ContextCompat.getColor(requireContext(), R.color.blur_one_half_opacity_white_color)
        }
    }

    private fun getUserProfile(){
        userViewModel = ViewModelProvider(
            this
        ).get(UserViewModel::class.java)

        if (ConnectionUtil(requireContext()).isOnline) {

            userViewModel?.getUserProfileData(
                requireContext(), SharedPrefHelper.getInstance().getUserId()!!
            )?.observe(this,
                Observer {
                    when(it.status){
                        Status.SUCCESS->{
                            setProgressBarVisible(false)
                            if (it?.data != null) {
                                fillUserDetail(it?.data)
                            }
                        }

                        Status.LOADING ->{
                            setProgressBarVisible(true)
                        }

                        Status.ERROR ->{
                            setEmptyVisible(false)
                            setProgressBarVisible(false)
                            Utils.showSnakbar(requireContext(),requireView(), true, it.message!!)
                        }
                    }
                })
        }else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(requireContext(), messageModel,"UserProfileEditProfileFragment","getUserProfile")
        }
    }

    private fun fillUserDetail(it: UserProfileModel?) {
        if (it != null) {
            CommonUtils.saveUserProfileDetails(it)
            if (it.statusCode == 200 && it.result != null && it?.result?.size!! > 0){
                if (!TextUtils.isEmpty(it?.result?.get(0)?.handleName)){
                    edtUsername.setText(it?.result?.get(0)?.handleName)
                }else{
                    edtUsername.setText("")
                }

                if (it?.result?.get(0)?.profileImage!! != null && !TextUtils.isEmpty(it?.result?.get(0)?.profileImage!!.toString())){
                    ImageLoader.loadImage(requireContext(),ivUserImg,it?.result?.get(0)?.profileImage!!.toString(),R.drawable.ic_no_user_img)
                }else if (it?.result?.get(0)?.alternateProfileImage!! != null && !TextUtils.isEmpty(it?.result?.get(0)?.alternateProfileImage!!.toString())){
                    ImageLoader.loadImage(requireContext(),ivUserImg,it?.result?.get(0)?.alternateProfileImage!!.toString(),R.drawable.ic_no_user_img)
                }
                var username = ""
                if (!TextUtils.isEmpty(it?.result?.get(0)?.firstName)){
                    username += it?.result?.get(0)?.firstName + " "
                }
                if (!TextUtils.isEmpty(it?.result?.get(0)?.lastName)){
                    username += it?.result?.get(0)?.lastName?.trim()
                }

                if (!TextUtils.isEmpty(username)){
                    etName?.setText(username)
                }else{
                    if (!TextUtils.isEmpty(it?.result?.get(0)?.handleName)){
                        etName?.setText(it?.result?.get(0)?.handleName)
                    }else{
                        etName?.setText("")
                    }

                }

                if (!TextUtils.isEmpty(it?.result?.get(0)?.email)){
                    edtUserEmail.setText(it?.result?.get(0)?.email)
                    email = it?.result?.get(0)?.email.toString()
                    ivUserEmailEdit.visibility = View.GONE
                }else{
                    edtUserEmail.setText("")
                    ivUserEmailEdit.visibility = View.VISIBLE
                }
                if (!TextUtils.isEmpty(it?.result?.get(0)?.phone)){
                    edtUserMobile.setText(it?.result?.get(0)?.phone)
                    mobile = it?.result?.get(0)?.phone.toString()
                    heCountryName = mobile
                    heCountryName = heCountryName.substring(0, 2)
                    ivUserMobileEdit.visibility = View.GONE
                    etCountryCode.visibility = View.GONE
                    cardCountryCode.visibility = View.GONE
                    edtUserMobile.setPadding(requireContext().resources.getDimensionPixelSize(R.dimen.dimen_22), 0, 0,0)
                }else{
                    setUpCountryData()
                    edtUserMobile.setText("")
                    ivUserMobileEdit.visibility = View.VISIBLE
                    etCountryCode.visibility = View.VISIBLE
                    cardCountryCode.visibility = View.VISIBLE
                    edtUserMobile.setPadding(requireContext().resources.getDimensionPixelSize(R.dimen.dimen_5), 0, 0,0)
                }
                val gender = resources.getStringArray(R.array.Gender)

                if (!TextUtils.isEmpty(it?.result?.get(0)?.gender)){
                    if(it?.result?.get(0)?.gender?.equals("M",true)!!){
                        spGender.text= gender.get(0)
                    }else if(it?.result?.get(0)?.gender?.equals("F",true)!!){
                        spGender.text=gender.get(1)
                    }else if(it?.result?.get(0)?.gender?.equals("U",true)!!){
                        spGender.text=gender.get(2)
                    }else{
                        spGender.text="Select gender"
                    }
                }else{
                    spGender.text="Select gender"
                }

                spGender.setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<String?> { oldIndex, oldItem, newIndex, newItem ->
                    spinner_pos = newIndex
                })

                if (!TextUtils.isEmpty(it.result?.get(0)?.dob)){
                    text_date.text =  com.hungama.music.utils.DateUtils.convertDate(
                        if (it.result?.get(0)?.dob!!.contains("T"))
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD_HH_MM_WITH_T
                        else
                            com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD,
                        com.hungama.music.utils.DateUtils.DATE_FORMAT_YYYY_MM_DD,it?.result?.get(0)?.dob)!!
                }else{
                    text_date.text = ""
                }

                //etName.requestFocus()
                etName.setSelection(etName.text.toString().length)
                //Utils.showSoftKeyBoard(requireActivity(),etName)
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        setLog("actionSave", "On keyboard done click11")
        if (actionId == EditorInfo.IME_ACTION_DONE){
            callEditProfile()
            setLog("actionSave", "On keyboard done click")
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        setLocalBroadcast()
    }

    private fun setLocalBroadcast(){
        (requireActivity() as MainActivity).setLocalBroadcastEventCall(this, Constant.AUDIO_PLAYER_EVENT)
    }

    override fun onLocalBroadcastEventCallBack(context: Context?, intent: Intent) {
        if (isAdded){
            val event = intent.getIntExtra("EVENT", 0)
            if (event == Constant.AUDIO_PLAYER_RESULT_CODE) {
                CommonUtils.setPageBottomSpacing(scrollView, requireContext(),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), resources.getDimensionPixelSize(R.dimen.dimen_0),
                    resources.getDimensionPixelSize(R.dimen.dimen_0), 0)
            }
        }
    }
}