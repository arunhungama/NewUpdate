package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class NpayGiftPayment(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.npay_gift
    }

    override fun getType(): EventType {
        return EventType.SDPCallback
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}