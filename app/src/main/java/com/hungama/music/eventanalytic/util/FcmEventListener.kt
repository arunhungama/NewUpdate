package com.hungama.music.eventanalytic.util

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.hungama.music.HungamaMusicApp
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.home.eventreporter.UserAttributeEvent
import com.hungama.music.utils.CommonUtils.setLog
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.firebase.listener.NonMoEngagePushListener
import com.moengage.pushbase.MoEPushHelper
import com.moengage.pushbase.listener.TokenAvailableListener
import com.moengage.pushbase.model.Token

class FcmEventListener : TokenAvailableListener, NonMoEngagePushListener {
    override fun onPushReceived(remoteMessage: RemoteMessage) {
        setLog("MessageReceived()", "onNonMoEngageMessageReceived(): $remoteMessage")
        // payload received, add your processing logic here

        if (MoEPushHelper.getInstance()
                .isFromMoEngagePlatform(remoteMessage.data) && MoEPushHelper.getInstance()
                .isSilentPush(remoteMessage.data)
        ) {
            MoEFireBaseHelper.getInstance()
                .passPushPayload(HungamaMusicApp.getInstance(), remoteMessage.data)
            return
        }

        if (MoEPushHelper.getInstance().isFromMoEngagePlatform(remoteMessage.data)) {
            MoEPushHelper.getInstance()
                .logNotificationReceived(HungamaMusicApp.getInstance(), remoteMessage.data)
        }


    }

    override fun onTokenAvailable(token: Token) {

        if(!HungamaMusicApp.getInstance().isFcmTokenPass!!) {
            // push token received, add your processing logic here
            MoEFireBaseHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(), token.pushToken)
//            MoEMiPushHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(),token)
//            MoEPushKitHelper.getInstance().passPushToken(HungamaMusicApp.getInstance(),token)
            setLog("onTokenAvailable()", "FcmEventListener Token Callback Received. Token: $token")
            val userDataMap= java.util.HashMap<String, String>()
            userDataMap.put(EventConstant.NOTIFICATION_TOKEN, token.pushToken)
            EventManager.getInstance().sendUserAttribute(UserAttributeEvent(userDataMap))
            HungamaMusicApp.getInstance().isFcmTokenPass=true
        }
    }
}