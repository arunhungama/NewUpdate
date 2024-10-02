package com.hungama.music.eventanalytic.eventreporter

import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventType
import com.hungama.music.eventanalytic.event.IEvent

class LoginMobileNumberCountryCodeEvent(val hashMap: HashMap<String,String>): IEvent {
    override fun getName(): String {
        return EventConstant.LOGINMOBILENUMBERCOUNTRYCODE_ENAME
    }

    override fun getType(): EventType {
        return EventType.LOGIN_MOBILE_NUMBER_COUNTRY_CODE
    }

    override fun getProperty(): HashMap<String, String> {
        return hashMap
    }
}