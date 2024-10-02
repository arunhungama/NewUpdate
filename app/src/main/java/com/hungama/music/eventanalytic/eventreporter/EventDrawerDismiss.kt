package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class EventDrawerDismiss(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.EVENT_DRAWER_DISMISS
    }

    override fun getType(): EventType {
       return EventType.EventDismiss
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}