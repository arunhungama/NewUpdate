package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class SearchResultPopulated(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.SEARCH_RESULT_POPULATED
    }

    override fun getType(): EventType {
       return EventType.CATEGORY_SEARCH
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}