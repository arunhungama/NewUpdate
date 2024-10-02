package com.hungama.music.ui.main.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.appsflyer.AppsFlyerLib
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.GameExitEvent
import com.hungama.music.eventanalytic.eventreporter.GameStartEvent
import com.hungama.music.ui.main.view.fragment.CommonDialog
import com.hungama.music.ui.main.viewmodel.GamelistViewModel
import com.hungama.music.utils.*
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.activity_toolbar_web_view.*
import kotlinx.android.synthetic.main.common_details_page_back_menu_header_on_scroll_visible.view.*
import kotlinx.android.synthetic.main.view_movie.view.fast_rewind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class CommonWebViewWithToolbarActivity : AppCompatActivity(), CommonDialog.CommonDialogListener {
    private val TAG = CommonWebViewWithToolbarActivity::class.java.name
    var logoutDialog: CommonDialog? = null
    var requestTime: Date? = null
    var isGameExitDatacall = "start"
    var isHomePage = false

    companion object {
        private val KEY_NAME = "NAME"
    }

    var gameListViewModel: GamelistViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_toolbar_web_view)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        val url = intent.getStringExtra(Constant.EXTRA_URL)
        val name = intent.getStringExtra(Constant.EXTRA_PAGE_DETAIL_NAME)
        tvActionBarHeading.text = name
        requestTime = DateUtils.getCurrentDateTime()
        isGameExitDatacall = "start"

        if (!intent.getStringExtra("flag").isNullOrEmpty()) {
            rlHeading.threeDotMenu.hide()
            rlHeading.show()

        } else rlHeading.hide()

        if (ConnectionUtil(this).isOnline) {
            gameListViewModel = ViewModelProvider(this).get(GamelistViewModel::class.java)
            if (!TextUtils.isEmpty(url)) {

                initSettings(webView)
                setCookies(webView);


                gameListViewModel!!.loadUrl(url, webView).webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {

                        setLog(TAG, "URL:" + request?.url.toString())

                        if (request?.url.toString().contains("yandex.com/games")) {
                            isHomePage = false
                            return false
                        }

                        val intent = Intent(Intent.ACTION_VIEW, request?.url)
                        startActivity(intent)
                        return true

                        view?.loadUrl(request?.url.toString())

                    }

                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

                    }

                    override fun onPageFinished(view: WebView, url: String) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            CookieManager.getInstance().flush();
                        } else {
                            CookieSyncManager.getInstance().sync();
                        }

                        if (Constant.isRedirected) {
                            GameStartData()
                        }
                    }
                }

            } else {
                val messageModel =
                    MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
                CommonUtils.showToast(
                    this,
                    messageModel,
                    "CommonWebViewWithToolbarActivity",
                    "onCreate"
                )
            }
        } else {
            val messageModel = MessageModel(
                getString(R.string.toast_message_5),
                getString(R.string.toast_message_5),
                MessageType.NEGATIVE,
                true
            )
            CommonUtils.showToast(
                this,
                messageModel,
                "CommonWebViewWithToolbarActivity",
                "onCreate"
            )
        }

        rlHeading.ivBack.setOnClickListener {
            back()
        }
    }

    private fun initSettings(siteWebView: WebView) {
        siteWebView.settings.javaScriptEnabled = true
        siteWebView.settings.allowContentAccess = true
        siteWebView.settings.allowFileAccess = true
        siteWebView.settings.allowUniversalAccessFromFileURLs = false
        siteWebView.settings.allowFileAccessFromFileURLs = true
        siteWebView.settings.blockNetworkImage = false
        siteWebView.settings.blockNetworkLoads = false
        siteWebView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        siteWebView.settings.databaseEnabled = false
        siteWebView.settings.setGeolocationEnabled(true)
        siteWebView.settings.javaScriptCanOpenWindowsAutomatically = true
        siteWebView.settings.loadsImagesAutomatically = true
        siteWebView.settings.setNeedInitialFocus(false)
        siteWebView.settings.saveFormData = true
        siteWebView.settings.setSupportMultipleWindows(false)
        siteWebView.settings.setSupportZoom(true)
        siteWebView.settings.domStorageEnabled = true
        //   siteWebView.settings.setAppCacheEnabled(true)
        siteWebView.settings.displayZoomControls = false
        siteWebView.settings.loadWithOverviewMode = true
        siteWebView.settings.useWideViewPort = true
        siteWebView.settings.builtInZoomControls = true
        siteWebView.settings.pluginState = WebSettings.PluginState.ON
        siteWebView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        siteWebView.settings.savePassword = true
    }

    private fun setCookies(webView: WebView) {
        CookieSyncManager.createInstance(this)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        } else {
            cookieManager.setAcceptCookie(true)
        }
    }

    fun back() {
        if (!isHomePage) {
            isHomePage = true
        } else {
            logoutDialog = CommonDialog(this, getString(R.string.your_want_exit), "Yes", "No")
            if (!logoutDialog?.isVisible!!) {
                logoutDialog!!.show(this.supportFragmentManager, "open dialog")

            } else {
                logoutDialog!!.dismiss()
            }
        }
    }

    override fun onBackPressed() {
        back()
    }

    override fun onStop() {
        isGameExitDatacall = "start"
        super.onStop()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("555555", isGameExitDatacall)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putString(KEY_NAME, "testts")
        isGameExitDatacall = ""
        super.onSaveInstanceState(outState, outPersistentState)

    }

    override fun onPause() {
        if (logoutDialog != null && logoutDialog!!.isVisible()) logoutDialog!!.dismiss()

        super.onPause()
    }

    /**
     * Loads given url in web view
     *
     */


    override fun dialogClick() {
        GameExitData()
        webView.destroy()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
            && keyCode == KeyEvent.KEYCODE_BACK
            && event?.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }

    fun GameStartData() {
        CoroutineScope(Dispatchers.IO).launch {
            val responseTime = DateUtils.getCurrentDateTime()
            val diffInMillies: Long = Math.abs(responseTime?.time!! - requestTime?.time!!)

            val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
            val hashMap = HashMap<String, String>()

            hashMap.put(
                EventConstant.CONTENTID_EPROPERTY,
                "" + intent.getStringExtra(Constant.defaultContentId)
            )
            hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "Game")
            hashMap.put(
                EventConstant.GAME_NAME, intent.getStringExtra(Constant.EXTRA_PAGE_DETAIL_NAME)!!
            )
            hashMap.put(
                EventConstant.CONTENT_TYPE_ID_EPROPERTY,
                "" + intent.getStringExtra(Constant.orderId)
            )
            Constant.isRedirected = false
            hashMap.put(EventConstant.GAME_URL, "" + intent.getStringExtra(Constant.EXTRA_URL))
            hashMap.put(EventConstant.GAME_VIEW, "" + intent.getStringExtra(Constant.MODE))
            hashMap.put(EventConstant.RESPONSE_TIME, "" + diff)

            EventManager.getInstance().sendEvent(GameStartEvent(hashMap))
        }
    }

    fun GameExitData() {
        CoroutineScope(Dispatchers.IO).launch {
            val responseTime = DateUtils.getCurrentDateTime()
            val diffInMillies: Long = Math.abs(responseTime?.time!! - requestTime?.time!!)

            val diff: Long = TimeUnit.MILLISECONDS.toSeconds(diffInMillies)
            val hashMap = HashMap<String, String>()
            hashMap.put(EventConstant.CONTENTID_EPROPERTY, "" + intent.getStringExtra(Constant.defaultContentId))
            hashMap.put(EventConstant.CONTENTTYPE_EPROPERTY, "Game")
            hashMap.put(EventConstant.GAME_NAME, intent.getStringExtra(Constant.EXTRA_PAGE_DETAIL_NAME)!!)
            hashMap.put(EventConstant.CONTENT_TYPE_ID_EPROPERTY, "" + intent.getStringExtra(Constant.orderId))
            hashMap.put(EventConstant.GAME_URL, "" + intent.getStringExtra(Constant.EXTRA_URL))
            hashMap.put(EventConstant.GAME_VIEW, "" + intent.getStringExtra(Constant.MODE))
            hashMap.put(EventConstant.DURATION_EPROPERTY, "" + diff)
            isGameExitDatacall = "call_done"
            EventManager.getInstance().sendEvent(GameExitEvent(hashMap))

            if (diff > 9) {
                val eventValue: MutableMap<String, Any> = HashMap()
                eventValue.put(EventConstant.CONTENTID_EPROPERTY, "" + intent.getStringExtra(Constant.defaultContentId))
                eventValue.put(EventConstant.CONTENTTYPE_EPROPERTY, "Game")
                eventValue.put(EventConstant.GAME_NAME, intent.getStringExtra(Constant.EXTRA_PAGE_DETAIL_NAME)!!)
                eventValue.put(EventConstant.CONTENT_TYPE_ID_EPROPERTY, "" + intent.getStringExtra(Constant.orderId))

                eventValue.put(EventConstant.GAME_URL, "" + intent.getStringExtra(Constant.EXTRA_URL))
                eventValue.put(EventConstant.GAME_VIEW, "" + intent.getStringExtra(Constant.MODE))
                eventValue.put(EventConstant.DURATION_EPROPERTY, "" + diff)
                isGameExitDatacall = "call_done"

                AppsFlyerLib.getInstance().logEvent(this@CommonWebViewWithToolbarActivity, EventConstant.GAME_EXIT, eventValue)

            }
        }
    }
}