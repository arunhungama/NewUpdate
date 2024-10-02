package com.hungama.music.eventanalytic.eventsubscriber

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.hungama.plus.amp.MiPushHelper
import com.moengage.core.BuildConfig
import com.xiaomi.channel.commonutils.android.Region
import logcat.logcat

/**
 * @author Umang Chamaria
 * Date: 2022/09/02
 */
class ApplicationLifecycleObserver(private val context: Context): DefaultLifecycleObserver {
    var MOENGAGE_APP_ID = ""
    var MOENGAGE_APP_KEY = ""
    var SENDERID = "102086738647"

    override fun onCreate(owner: LifecycleOwner) {
        if(BuildConfig.DEBUG){
            MOENGAGE_APP_ID = "AESG8OUL9LQ5CRALTHN7SFDR"
            MOENGAGE_APP_KEY = "PXGA4LZ3VNHO"
            SENDERID = "155634998008"
        }else{
            MOENGAGE_APP_ID = "GU1CJU9JH0K80P2J4G3ESGPB"
            MOENGAGE_APP_KEY = "e1mrWX5J3MFYbrBOVVG+FALL"
            SENDERID = "102086738647"
        }
        super.onCreate(owner)
    }



    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logcat { "application coming to foreground." }
        MiPushHelper.initialiseMiPush(
            context,
            appKey = MOENGAGE_APP_KEY,
            appId = MOENGAGE_APP_ID,
            // replace the region with the actual region.
            Region.Global
        )
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
    }
}