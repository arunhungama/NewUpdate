package com.hungama.music.ui.main.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.BackgroundActivityEvent
import com.hungama.music.eventanalytic.eventreporter.EventDrawerClick
import com.hungama.music.eventanalytic.eventreporter.EventDrawerView
import com.hungama.music.eventanalytic.eventreporter.ProgressiveSurveyTappedEvent
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.myplay.activity.R
import com.hungama.music.utils.CommonUtils
import com.hungama.music.utils.customview.bottomsheet.SuperBottomSheetFragment
import com.hungama.music.utils.preference.PrefConstant
import com.hungama.music.utils.preference.SharedPrefHelper
import kotlinx.android.synthetic.main.battery_optimization_permission_dialog.*
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.*
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.tvCancel
import kotlinx.android.synthetic.main.delete_downloaded_content_dialog.tvDelete
import kotlinx.android.synthetic.main.payment_success_dialog.tvHome
import kotlinx.android.synthetic.main.payment_success_dialog.tvViewTicket

class PaytmSuccessPermissionDialog(val eventId:String,val eventName:String,paytmSuccessPermission: PaytmSuccessPermission) : SuperBottomSheetFragment(){
    val paytmSuccessPermission = paytmSuccessPermission


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.payment_success_dialog,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    dialog!!.setCanceledOnTouchOutside(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        tvViewTicket.setOnClickListener {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), tvViewTicket!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val dataMap = HashMap<String, String>()
            dataMap[EventConstant.TYPE_EPROPERTY] = "offline_event"
            dataMap[EventConstant.SCREEN_NAME_EPROPERTY] = "insider payment success"
            dataMap[EventConstant.EVENTID] = eventId
            dataMap[EventConstant.EVENTNAME] = eventName

            dataMap[EventConstant.button_text_2] = "View My Tickets"
            EventManager.getInstance().sendEvent(EventDrawerClick(dataMap))
            paytmSuccessPermission.onPermission(0)
        }

        CommonUtils.applyButtonTheme(requireContext(), tvHome)

        tvHome.setOnClickListener {

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    CommonUtils.hapticVibration(requireContext(), tvHome!!,
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }catch (e:Exception){

            }
            val dataMap = HashMap<String, String>()
            dataMap[EventConstant.TYPE_EPROPERTY] = "offline_event"
            dataMap[EventConstant.SCREEN_NAME_EPROPERTY] = "insider payment success"
            dataMap[EventConstant.EVENTID] = eventId
            dataMap[EventConstant.EVENTNAME] = eventName

            dataMap[EventConstant.button_text_1] = "Explore Hungama"
            EventManager.getInstance().sendEvent(EventDrawerClick(dataMap))
            paytmSuccessPermission.onPermission(1)

        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        paytmSuccessPermission.onDismiss()
        super.onDismiss(dialog)
    }

    override fun getCornerRadius() = requireContext().resources.getDimension(R.dimen.common_popup_round_corner)

//    override fun getStatusBarColor() = Color.RED

    override fun isSheetAlwaysExpanded(): Boolean = true

    override fun getExpandedHeight(): Int = requireContext().resources.getDimension(R.dimen.dimen_400).toInt()

    interface PaytmSuccessPermission{
        fun onPermission(type:Int)
        fun onDismiss()
    }
}

