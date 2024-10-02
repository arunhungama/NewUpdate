package com.hungama.music.ui.main.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.MessageModel
import com.hungama.music.data.model.MessageType
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.AppLaunchEvent
import com.hungama.music.eventanalytic.eventreporter.AutoAppLanguageEvent
import com.hungama.music.eventanalytic.eventreporter.LanguageChangedEvent
import com.hungama.music.eventanalytic.eventreporter.LoginAutoSkipEvent
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.home.eventsubscriber.AppsflyerSubscriber
import com.hungama.music.model.LangItem
import com.hungama.music.ui.main.viewmodel.MusicLanguageViewModel
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.CommonUtils.faDrawable
import com.hungama.music.utils.CommonUtils.getDeeplinkIntentData
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Utils
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.R
import kotlinx.android.synthetic.main.activity_choose_language.*
import kotlinx.android.synthetic.main.activity_phone_number.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class DeeplinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manageIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        manageIntent(intent)
    }


    private fun manageIntent(intent: Intent?) {
        if (intent != null && intent?.data != null) {
            val appLinkAction: String? = intent?.action
            val appLinkData: Uri? = intent?.data
            showDeepLinkUrl(appLinkAction, appLinkData)
        }else{
            sendEvent()
            startActivity(Intent(this, MainActivity::class.java).putExtra(EventConstant.SOURCE,"deeplink"))
        }
    }

    private fun showDeepLinkUrl(appLinkAction: String?, appLinkData: Uri?) {
        setLog("TAG", "showDeepLinkUrl: appLinkData:$appLinkData appLinkAction:$appLinkAction")
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            sendEvent()
            println("alhgasho"+ " ${SharedPrefHelper.getInstance().get(PrefConstant.CHOOES_LANGUAGE,true)}")

/*            if(SharedPrefHelper.getInstance().get(PrefConstant.CHOOES_LANGUAGE,true))
            {
                AppsflyerSubscriber.deeplink = appLinkData.toString()
                val uri = Uri.parse(appLinkData.toString())
                val parameters = uri.queryParameterNames.associateWith { uri.getQueryParameters(it) } // {limit:[25], time:[0dfdbac117], since:[1374196005], md5:[d8959d12ab687ed5db978cb078f1e]}
                val vskipLogin = parameters.get("skiplogin").toString()
                val strAlang = parameters.get("alang").toString()

                var alangEventCheck = ""
                var skipLoginEvent = ""
                var alangEvent = ""

                println("alhgasho"+ " $vskipLogin ${strAlang}")
                var alang = ""
                if (strAlang == "null" || strAlang.contains("false")){
                    val intent = getDeeplinkIntentData(appLinkData)
                    intent.putExtra(Constant.EXTRA_URL, appLinkData.toString())
                    AppsflyerSubscriber.deeplink = appLinkData.toString()
                    intent.setClass(this, ChooseLanguageActivity::class.java)
                    alangEventCheck = "false"
                    startActivity(intent)
                    finish()
                }
                else{
                    val languages = arrayOf<String>("en","gu","hi","kn","ml","mr","pa","ta","te","bn")
                    val languagesFullName = arrayOf<String>("English","Gujrati","Hindi","Kannada","Malayalam","Marathi","Punjabi","Tamil","Telugu","Bengal")
                    for (i in languages.indices){
                        if (strAlang.contains(languages[i])){
                            val selectedLangItem = LangItem()
                            selectedLangItem?.code = languages[i]
                            selectedLangItem?.title = languagesFullName[i]
                            alangEventCheck = "true"
                            alangEvent = languagesFullName[i]

                            selectedLangItem?.let {
                                SharedPrefHelper.getInstance().saveLanguageObject(PrefConstant.LANG_DATA, it)
                            }
                            SharedPrefHelper.getInstance().setLanguage(languages[i])
                            SharedPrefHelper.getInstance().setLanguageTitle(languagesFullName[i])
                            SharedPrefHelper.getInstance().save(PrefConstant.CHOOES_LANGUAGE, false)

                            if (alangEvent.isNotEmpty())
                            {
                                val userDataMap= java.util.HashMap<String, String>()
                                userDataMap.put(EventConstant.APP_LANGUAGE,""+languagesFullName[i])
                                EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
                            }

                            setLanguage(languages[i], vskipLogin, appLinkData)

                            break
                        }
                    }


                }

                skipLoginEvent = if (vskipLogin == "null" || vskipLogin.contains("false"))
                    "false"
                else
                    "true"

                val dataMapAutoAlang= java.util.HashMap<String, String>()
                dataMapAutoAlang[EventConstant.AUTO_SELECTED] = alangEventCheck
                dataMapAutoAlang[EventConstant.ALANG] = alangEvent
                EventManager.getInstance().sendEvent(AutoAppLanguageEvent(dataMapAutoAlang))

                val dataMap= java.util.HashMap<String, String>()
                dataMap[EventConstant.AUTO_SELECTED] = skipLoginEvent
                EventManager.getInstance().sendEvent(LoginAutoSkipEvent(dataMap))

            }*/
            if(SharedPrefHelper.getInstance().get(PrefConstant.CHOOES_LANGUAGE,true)) {
                AppsflyerSubscriber.deeplink = appLinkData.toString()
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
            }
            else {
                val intent = getDeeplinkIntentData(appLinkData)
                intent.setClass(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    fun setLanguage(strLang:String, vskipLogin:String, appLinkData:Uri){
        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            if (strLang.isNotEmpty())
                setLocale(strLang)

            if (vskipLogin == "null" || vskipLogin.contains("false")){
                val intent = getDeeplinkIntentData(appLinkData)
                intent.putExtra(Constant.EXTRA_URL, appLinkData.toString())
                AppsflyerSubscriber.deeplink = appLinkData.toString()
                intent.setClass(this@DeeplinkActivity, LoginMainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent = getDeeplinkIntentData(appLinkData)
                intent.putExtra(Constant.EXTRA_URL, appLinkData.toString())
                AppsflyerSubscriber.deeplink = appLinkData.toString()
                intent.setClass(this@DeeplinkActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun sendEvent() {
        CoroutineScope(Dispatchers.IO).launch {
            val dataMap = HashMap<String, String>()
            dataMap.put(EventConstant.AB_NC_EPROPERTY, "1")
            dataMap.put(EventConstant.SOURCE_EPROPERTY, "Android")
            EventManager.getInstance().sendEvent(AppLaunchEvent(dataMap))
        }
    }
    private fun saveUserLangPref(type:String, preference: String) {
        if (ConnectionUtil(this@DeeplinkActivity).isOnline) {

            try {
                val mainJson = JSONObject()
                val prefArrays = JSONArray()
                prefArrays.put(preference)

                mainJson.put("type", type)
                mainJson.put("preference", prefArrays)

                val musicLanguageListViewModel = ViewModelProvider(this).get(MusicLanguageViewModel::class.java)
                musicLanguageListViewModel?.saveUserPref(this@DeeplinkActivity, mainJson.toString())

            } catch (e: Exception) {
                e.printStackTrace()
                Utils.showSnakbar(this,
                    parentMobile!!,
                    false,
                    getString(R.string.discover_str_2)
                )
            }


        } else {
            val messageModel = MessageModel(getString(R.string.toast_str_35), getString(R.string.toast_message_5),
                MessageType.NEGATIVE, true)
            CommonUtils.showToast(this, messageModel,"ChooseLanguageActivity","saveUserLangPref")
        }
    }


    fun setLocale(localeName: String) {
        print("alhgasho $localeName")

        val myLocale = Locale(localeName)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
        onConfigurationChanged(conf)
    }
}