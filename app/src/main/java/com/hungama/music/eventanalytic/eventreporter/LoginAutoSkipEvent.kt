package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LoginAutoSkipEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.LOGIN_AUTO_SKIP
    }

    override fun getType(): EventType {
        return EventType.LOGIN_AUTO_SKIP
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}