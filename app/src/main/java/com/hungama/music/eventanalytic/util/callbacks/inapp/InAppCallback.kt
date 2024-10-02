package com.hungama.music.eventanalytic.util.callbacks.inapp

import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.InAppSelfHandledModel
import com.hungama.music.utils.CommonUtils.setLog
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.InAppLifeCycleListener
import com.moengage.inapp.listeners.OnClickActionListener
import com.moengage.inapp.listeners.SelfHandledAvailableListener
import com.moengage.inapp.model.ClickData
import com.moengage.inapp.model.InAppData
import com.moengage.inapp.model.SelfHandledCampaignData


/**
 * @author
 * Date: 2021-02-21
 */
class InAppCallback : OnClickActionListener,InAppLifeCycleListener,SelfHandledAvailableListener{

  companion object{
    var mInAppCampaignList= HashMap<String,InAppSelfHandledModel>()
  }


  override fun onSelfHandledAvailable(inAppCampaign: SelfHandledCampaignData?) {
   // super.SelfHandledCampaignData(inAppCampaign)
    setLog("SetMoengageDataMain", " "  + Gson().toJson(inAppCampaign?.campaign!!.payload))

    try {
      if(inAppCampaign.campaign?.payload!=null&&!TextUtils.isEmpty(inAppCampaign.campaign?.payload)){
        val inAppSelfHandledModel = Gson().fromJson<InAppSelfHandledModel>(
          inAppCampaign.campaign?.payload,
          InAppSelfHandledModel::class.java
        ) as InAppSelfHandledModel

        inAppSelfHandledModel?.campaignId=inAppCampaign.campaignData.campaignId
        inAppSelfHandledModel.inAppCampaign =inAppCampaign
        if(mInAppCampaignList.size>0){
          var check=true
          mInAppCampaignList?.values?.forEachIndexed { index, moEInAppCampaign ->
            try {
              if (inAppSelfHandledModel.bottom_nav_position == moEInAppCampaign?.bottom_nav_position && inAppSelfHandledModel.top_nav_position == moEInAppCampaign?.top_nav_position) {
                check=false
                setLog("setMoengageData", "onSelfHandledAvailable mInAppCampaignList status"+check)
              }
            }catch (e :Exception){

            }
          }
          if(check){
            mInAppCampaignList.put(inAppSelfHandledModel.campaign_id, inAppSelfHandledModel)
          }
        }
        else {
          mInAppCampaignList.put(inAppSelfHandledModel.campaign_id, inAppSelfHandledModel)
        }

        setLog("setMoengageData", "onSelfHandledAvailable mInAppCampaignList size:${mInAppCampaignList?.size}")
      }

    }catch (exp:Exception){
      exp.printStackTrace()
    }

  }

  override fun onDismiss(inAppData: InAppData) {

  }

  override fun onShown(inAppData: InAppData) {
  }

  override fun onClick(clickData: ClickData): Boolean {
    return false
  }
}
