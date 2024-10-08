package com.hungama.music.ui.main.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hungama.myplay.activity.R
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.ui.main.adapter.MusicPlaybackSettingQualityAdapter
import com.hungama.music.data.model.MusicPlaybackSettingStreamQualityModel
import com.hungama.music.data.model.QualityAction
import com.hungama.music.utils.Constant
import kotlinx.android.synthetic.main.music_playback_setting_stream_quality.*

class DownloadVideoStreamQuality(
    val streamQualityList: ArrayList<MusicPlaybackSettingStreamQualityModel>,
    val onItemClick: OnItemClick
) : SuperBottomSheetFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.music_playback_setting_stream_quality, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle.setText(getString(R.string.general_setting_str_22))

        btnAsk?.setOnClickListener {
            dismiss()
        }
        rvStreamQuality.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 1)
            adapter = MusicPlaybackSettingQualityAdapter(requireContext(), streamQualityList,
                object : MusicPlaybackSettingQualityAdapter.OnItemClick {
                    override fun onUserClick(position: Int, onlyDismissPopUp:Boolean) {
                        if (onItemClick != null){
                            if (!onlyDismissPopUp){
                                onItemClick.onUserClick(position, Constant.DOWNLOAD_VIDEOSTREAMQUALITY)
                            }

                            dismiss()
                        }
                    }


                }, QualityAction.VIDEO_PLAYBACK_DOWNLOAD_QUALITY)
            setRecycledViewPool(RecyclerView.RecycledViewPool())
            setHasFixedSize(true)
        }
    }

    override fun getCornerRadius() =
        requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true
    override fun getExpandedHeight(): Int =
        requireContext().resources.getDimension(R.dimen.dimen_350).toInt()

    /*override fun getPeekHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_540).toInt()*/

    override fun getBackgroundColor(): Int =
        requireContext().resources.getColor(R.color.transparent)

    interface OnItemClick {
        fun onUserClick(position: Int, settingType:Int)
    }
}

