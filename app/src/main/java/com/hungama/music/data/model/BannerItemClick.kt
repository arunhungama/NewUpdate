package com.hungama.music.data.model

import com.hungama.music.utils.customview.fontview.FontAwesomeImageView

interface BannerItemClick {
    fun bannerItemClick(
        isClicked: Boolean,
        pos: Int,
        bodyData: BodyRowsItemsItem?
    )
    fun onCheckSatusplaylist(
        isClicked: Boolean,
        pos: Int,
        bodyData: BodyRowsItemsItem?
    )
}