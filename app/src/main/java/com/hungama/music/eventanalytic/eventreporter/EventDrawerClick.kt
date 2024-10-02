package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class EventDrawerClick(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.EVENT_DRAWER_CLICK
    }

    override fun getType(): EventType {
       return EventType.EventClick
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}