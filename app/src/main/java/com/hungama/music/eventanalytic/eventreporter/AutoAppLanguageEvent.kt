package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class AutoAppLanguageEvent(val hashMap:HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.AUTO_APP_LANGUAGE
    }

    override fun getType(): EventType {
        return EventType.AUTO_APP_LANGUAGE
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}