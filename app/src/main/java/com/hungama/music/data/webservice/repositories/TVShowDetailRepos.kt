package com.hungama.music.data.webservice.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.music.data.model.PlaylistDynamicModel
import com.hungama.myplay.activity.R
import com.hungama.music.eventanalytic.EventConstant
import com.hungama.music.eventanalytic.event.EventManager
import com.hungama.music.eventanalytic.eventreporter.ApiPerformanceEvent
import com.hungama.music.utils.ConnectionUtil
import com.hungama.music.utils.DateUtils
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.remote.data.DataManager
import com.hungama.music.data.webservice.remote.data.DataValues
import com.hungama.music.data.webservice.utils.Resource
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class TVShowDetailRepos {
    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<PlaylistDynamicModel>>()

    fun getTVShowDetailList(
        context: Context,
        id: String,
        type: String?
    ): MutableLiveData<Resource<PlaylistDynamicModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            var url=""
            if(type.equals("98",true)){
                url=WSConstants.METHOD_PLAYABLE_CONTENT_DYNAMIC+id+"/tvshowepisode/detail"
            }else{
                url=WSConstants.METHOD_PLAYABLE_CONTENT_DYNAMIC+id+"/tvshow/detail"
            }


            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context, url, JSONObject(), object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<PlaylistDynamicModel>>()
                    }

                    try {
                        val detailModel = Gson().fromJson<PlaylistDynamicModel>(
                            response.toString(),
                            PlaylistDynamicModel::class.java
                        ) as PlaylistDynamicModel

                        dataResp.postValue(Resource.success(detailModel))

                        /**
                         * event property start
                         */
                        val responseTime = DateUtils.getCurrentDateTime()
                        val diffInMillies: Long =
                            Math.abs(requestTime.getTime() - responseTime.getTime())

                        val diff: Long = TimeUnit.MILLISECONDS.toMillis(diffInMillies)
                        setLog(
                            TAG,
                            "getHomeListData: diff:$diff diffInMillies:$diffInMillies requestTime: $requestTime responseTime:$responseTime"
                        )

                        val hashMap = HashMap<String, String>()
                        hashMap.put(EventConstant.ERRORCODE_EPROPERTY, "")
                        hashMap.put(
                            EventConstant.NETWORKTYPE_EPROPERTY,
                            "" + ConnectionUtil(context).networkType
                        )
                        if(type.equals("98",true)){
                            hashMap.put(EventConstant.NAME_EPROPERTY, "tvshow_detailscreen")
                        }else{
                            hashMap.put(EventConstant.NAME_EPROPERTY, "tvshow_detailscreen")
                        }
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(
                            EventConstant.SOURCE_NAME_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(
                            EventConstant.SOURCE_EPROPERTY,
                            "" + HungamaMusicApp.getInstance().getEventData(id).sourceName
                        )
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY, "" + diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dataResp.postValue(
                            Resource.error(
                                context.getString(R.string.discover_str_2),
                                null
                            )
                        )
                    }
                }

                override fun setVolleyError(volleyError: VolleyError?) {
                    volleyError?.printStackTrace()
                    dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
                }

            })
        }


        return dataResp
    }
}