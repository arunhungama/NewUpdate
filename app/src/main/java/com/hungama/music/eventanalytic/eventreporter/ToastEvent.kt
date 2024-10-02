package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class ToastEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.TOAST_MESSAGE
    }

    override fun getType(): EventType {
        return EventType.TOAST_MESSAGE
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}