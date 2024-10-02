package com.hungama.music.ui.main.view.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.EventDrawerDismiss
import com.hungama.music.eventanalytic.eventreporter.EventDrawerView
import com.hungama.music.ui.main.view.fragment.PaytmSuccessPermissionDialog
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.Constant
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.activity_paytm_insider_webview.webView
import kotlinx.android.synthetic.main.common_details_page_with_close_header.ivClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PaytmInsiderWebviewActivity : AppCompatActivity() {
    private var webSettings: WebSettings? = null
    var eventName = ""
    var eventId = ""
    var SOURCE = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paytm_insider_webview)
        val url = intent.getStringExtra(Constant.EXTRA_URL)
        eventId = intent.getStringExtra(EventConstant.EVENTID).toString()
        eventName = intent.getStringExtra(EventConstant.EVENTNAME).toString()


        SOURCE = intent.getStringExtra(EventConstant.SOURCE).toString()
        Log.d("sarvesh",eventName+"////////"+eventId)
        if (!TextUtils.isEmpty(url)){
            loadUrl(url)
        }else{
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
            CommonUtils.showToast(this, messageModel,"CommonWebViewActivity","onCreate")
        }

        ivClose.setOnClickListener{
            onBackPressed()
        }
    }

    override fun isDestroyed(): Boolean {
        if(SOURCE.isNotBlank()) {
            Constant.paytminsider_redirection = true
        }
        return super.isDestroyed()
    }


    private fun loadUrl(url: String?) {
        if (!TextUtils.isEmpty(url)) {
            url?.let { webView.loadUrl(it)
                CommonUtils.setLog("url", " $it")}
        }else{
            val messageModel = MessageModel(getString(R.string.discover_str_2), MessageType.NEUTRAL, true)
            CommonUtils.showToast(this, messageModel,"CommonWebViewActivity","loadUrl")
        }
        webSettings = webView?.settings

        webView.settings.domStorageEnabled = true
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.pluginState = WebSettings.PluginState.ON

        // Set web view client
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                CommonUtils.setLog(TAG, "URL:" + request?.url.toString())
                if (request?.url.toString().contains("https://insider.in/payments/success?")){
                    openBottomDialog()
                    val intent = Intent(Constant.PAYTM_INSIDER)
                    LocalBroadcastManager.getInstance(this@PaytmInsiderWebviewActivity).sendBroadcast(intent)
                }else if (request?.url != null)
                {
                    view?.loadUrl(request?.url.toString())
                }


                return false

            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

            }

            override fun onPageFinished(view: WebView, url: String) {
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
            }
        }
    }
    fun openBottomDialog(){
        CoroutineScope(Dispatchers.IO).launch {

            val paytmSuccessPermissionDialog = PaytmSuccessPermissionDialog(eventId,eventName, object :
                PaytmSuccessPermissionDialog.PaytmSuccessPermission {

                override fun onPermission(type: Int) {
                    if(type == 0){
                        Constant.paytminsider_redirection = true
                        val intent = Intent(this@PaytmInsiderWebviewActivity, MainActivity::class.java)
                        intent.putExtra(Constant.EXTRA_PAGE_NAME, "library")
                        intent.putExtra(Constant.EXTRA_PAGE_DETAIL_NAME, "Purchases")
                        startActivityForResult(intent, 101)
                    }else if(type == 1){
                        Constant.paytminsider_redirection = true
                        val intent = Intent(this@PaytmInsiderWebviewActivity, MainActivity::class.java)
                        intent.putExtra(Constant.EXTRA_PAGE_NAME, "home")
                        startActivityForResult(intent,101)
                    }

                }

                override fun onDismiss() {
                    val dataMap = HashMap<String, String>()
                    dataMap[EventConstant.TYPE_EPROPERTY] = "offline_event"
                    dataMap[EventConstant.SCREEN_NAME_EPROPERTY] = "insider payment success"
                    dataMap[EventConstant.EVENTID] = eventId
                    dataMap[EventConstant.EVENTNAME] = eventName

                    dataMap[EventConstant.button_text_1] = "Explore Hungama"
                    dataMap[EventConstant.button_text_2] = "View My Tickets"
                    EventManager.getInstance().sendEvent(EventDrawerDismiss(dataMap))
                }
            })

            if(!isFinishing)
                if(!paytmSuccessPermissionDialog.isVisible && !supportFragmentManager.isDestroyed) {
                    paytmSuccessPermissionDialog.show(supportFragmentManager, "open logout dialog")

                    val dataMap = HashMap<String, String>()
                    dataMap[EventConstant.TYPE_EPROPERTY] = "offline_event"
                    dataMap[EventConstant.SCREEN_NAME_EPROPERTY] = "insider payment success"
                    dataMap[EventConstant.EVENTID] = eventId
                    dataMap[EventConstant.EVENTNAME] = eventName

                    dataMap[EventConstant.button_text_1] = "Explore Hungama"
                    dataMap[EventConstant.button_text_2] = "View My Tickets"
                    EventManager.getInstance().sendEvent(EventDrawerView(dataMap))

                } else {
                    paytmSuccessPermissionDialog.dismiss()
                }
        }
    }

    override fun onBackPressed() {
        webView.removeAllViews()
        webView.destroy()
        webView.clearHistory()
        webView.destroy()
        finish()
    }

}
