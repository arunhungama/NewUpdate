package com.hungama.music.data.webservice.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.hungama.music.HungamaMusicApp
import com.hungama.myplay.activity.R
import com.hungama.music.data.model.SongDetailModel
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
import java.util.HashMap
import java.util.concurrent.TimeUnit

class SongDetailRepos {
    private val TAG = javaClass.simpleName
    private var dataResp = MutableLiveData<Resource<SongDetailModel>>()

    fun getSongDetail(context: Context, id: String): MutableLiveData<Resource<SongDetailModel>> {
        CoroutineScope(Dispatchers.IO).launch{
            val url =
                WSConstants.METHOD_DETAIL_CONTENT_V1 + id + "/song/detail"


            var requestTime = DateUtils.getCurrentDateTime()
            dataResp.postValue(Resource.loading(null))
            DataManager.getInstance(context)?.getVolleyRequest(context,url, JSONObject(),object : DataValues {
                override fun setJsonDataResponse(response: JSONObject?) {
                    if (dataResp == null) {
                        dataResp = MutableLiveData<Resource<SongDetailModel>>()
                    }

                    try {
                        val detailModel = Gson().fromJson<SongDetailModel>(
                            response.toString(),
                            SongDetailModel::class.java
                        ) as SongDetailModel

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
                        hashMap.put(EventConstant.NAME_EPROPERTY, "song_detail_screen")
                        hashMap.put(
                            EventConstant.RESPONSECODE_EPROPERTY,
                            EventConstant.RESPONSE_CODE_200
                        )
                        hashMap.put(EventConstant.SOURCE_NAME_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.SOURCE_EPROPERTY, ""+HungamaMusicApp.getInstance().getEventData(id).sourceName)
                        hashMap.put(EventConstant.RESPONSETIME_EPROPERTY,""+diff)
                        hashMap.put(EventConstant.URL_EPROPERTY, com.hungama.music.utils.CommonUtils.getUrlWithoutParameters(url)!!)

                        EventManager.getInstance().sendEvent(ApiPerformanceEvent(hashMap))

                        /**
                         * event property end
                         */

                    } catch (exp: Exception) {
                        exp.printStackTrace()
                        dataResp.postValue(Resource.error(context.getString(R.string.discover_str_2), null))
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