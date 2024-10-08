package com.hungama.music.data.webservice.remote.data

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hungama.music.data.webservice.WSConstants
import com.hungama.music.data.webservice.remote.VolleySinglton
import com.hungama.music.ui.main.view.activity.MainActivity
import com.hungama.music.utils.CommonUtils.setLog
import com.hungama.music.utils.Constant
import com.hungama.music.utils.Constant.default_music_language_code
import com.hungama.music.utils.Constant.default_video_language_code
import com.hungama.music.utils.preference.SharedPrefHelper
import com.hungama.myplay.activity.BuildConfig
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class DataManager {

    companion object {
        var instance: DataManager? = null
        @Synchronized
        fun getInstance(context: Context): DataManager? {
            if (instance == null) {
                instance = DataManager()
            }
            return instance
        }
    }



    suspend fun getVolleyRequestArray(
        context: Context?,
        url: String?,
        dataValues: DataValues
    ) {
//        val currentSpan = Sentry.getSpan()
//         val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")
            val mlang= SharedPrefHelper.getInstance().get(Constant.APPMUSICLANG,default_music_language_code)
            val vlang= SharedPrefHelper.getInstance().get(Constant.APPVIDEOLANG,default_video_language_code)
            val userId= SharedPrefHelper.getInstance().getUserId()
            val version = getApiVersion(url.toString())

            var finalURL=""
        if(url?.contains("countries",true)!!){
            finalURL=url
        }else if(url?.contains("?",true)!!){
                finalURL=url+"&alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }else{
                finalURL=url+"?alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }

        finalURL = getAPIUrl(finalURL , Constant.COUNTRY_CODE)

            setLog("API_request", "getVolleyRequestArray: finalURL :${finalURL}")

            val cacheRequest = JsonArrayRequest(finalURL,
                { jsonArray -> dataValues.setJsonArrayDataResponse(jsonArray)
//                   child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
                }
            ) { error -> //showData(error.toString());
                dataValues.setVolleyError(error)
//                 child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
            }


            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                WSConstants.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)

    }

    suspend fun getOpenVolleyRequest(
        context: Context?,
        url: String?,
        reqJsonObject: JSONObject?,
        dataValues: DataValues
    ) {
//        val currentSpan = Sentry.getSpan()
//         val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")
            val mlang= SharedPrefHelper.getInstance().get(Constant.APPMUSICLANG,default_music_language_code)
            val vlang= SharedPrefHelper.getInstance().get(Constant.APPVIDEOLANG,default_video_language_code)
            val userId= SharedPrefHelper.getInstance().getUserId()
            val version = getApiVersion(url.toString())
            var finalURL=""
            if(url?.contains("?",true)!!){
                finalURL=url+"&alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }else{
                finalURL=url+"?alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }

        finalURL = finalURL + Constant.COUNTRY_CODE
        setLog("API_request", "getVolleyRequestArray: finalURL :${finalURL}")

            val cacheRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET, finalURL,
                reqJsonObject, Response.Listener { response -> //    showData(response.toString());
//                   child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
                    dataValues.setJsonDataResponse(response)
                }, Response.ErrorListener { error -> //showData(error.toString());
//                     child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
                    dataValues.setVolleyError(error)

                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params.put(
                        WSConstants.WS_HEADER_CONTENT_TYPE,
                        WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                    )
                    params.put(
                        WSConstants.PAY_API_KEY,
                        WSConstants.PAY_API_KEY_VALUE,
                    )
                    if(MainActivity.lastBottomItemPosClicked==0){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "home",
                        )
                    }else if(MainActivity.lastBottomItemPosClicked==1){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "listen",
                        )
                    }else if(MainActivity.lastBottomItemPosClicked==2){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "watch",
                        )
                    }
                    setLog("TAG", "getOpenVolleyRequest: finalURL :${finalURL} params:${params}")
                    return params
                }
            }

            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)

    }

    suspend fun postVolleyRequest(
        context: Context?,
        urll: String?,
        reqJsonObject: JSONObject?,
        dataValues: DataValues,
        maxRetry: Int = DefaultRetryPolicy.DEFAULT_MAX_RETRIES
    ) {
//        val currentSpan = Sentry.getSpan()
//         val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")
            val version = getApiVersion(urll.toString())
            reqJsonObject?.put("device", "android")
            reqJsonObject?.put("variant", version)

        val url = getAPIUrl(urll.toString(), Constant.COUNTRY_CODE)

        setLog("API_request", url + " "+ reqJsonObject.toString())


            val cacheRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url,
                reqJsonObject, Response.Listener { response -> //    showData(response.toString());
//                   child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
                    dataValues.setJsonDataResponse(response)
                }, Response.ErrorListener { error ->
                    //showData(error.toString());
//                     child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
                    dataValues.setVolleyError(error)
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params.put(
                        WSConstants.WS_HEADER_CONTENT_TYPE,
                        WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                    )
                    params.put(
                        WSConstants.PAY_API_KEY,
                        WSConstants.PAY_API_KEY_VALUE,
                    )
                    setLog("finalURL", "finalURL :${url} jsonObject:${reqJsonObject} params:${params}")
                    return params
                }
            }

            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                maxRetry,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)


    }


    suspend fun deleteVolleyRequest(
        context: Context?,
        urll: String?,
        reqJsonObject: JSONObject?,
        dataValues: DataValues
    ) {
//        val currentSpan = Sentry.getSpan()
//         val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")
            setLog("deleteVolleyRequest", "jsonObject:$reqJsonObject")
            val version = getApiVersion(urll.toString())
            reqJsonObject?.put("device", "android")
            reqJsonObject?.put("variant", version)
        val finalURL = getAPIUrl(urll.toString(), Constant.COUNTRY_CODE)
        setLog("API_request", "$finalURL")
            val cacheRequest: JsonObjectRequest = object : JsonObjectRequest(Request.Method.DELETE, finalURL,
                reqJsonObject, Response.Listener { response -> //    showData(response.toString());
//                   child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
                    dataValues.setJsonDataResponse(response)
                }, Response.ErrorListener { error -> //showData(error.toString());
//                     child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
                    dataValues.setVolleyError(error)
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params.put(
                        WSConstants.WS_HEADER_CONTENT_TYPE,
                        WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                    )
                    params.put(
                        WSConstants.PAY_API_KEY,
                        WSConstants.PAY_API_KEY_VALUE,
                    )
                    setLog("finalURL", "finalURL :${url} jsonObject:${reqJsonObject} params:${params}")
                    return params
                }
            }
            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)

    }

    suspend fun putVolleyRequest(
        context: Context?,
        urll: String?,
        reqJsonObject: JSONObject?,
        dataValues: DataValues
    ) {
//        val currentSpan = Sentry.getSpan()
//         val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")
            setLog("putVolleyRequest", "jsonObject:$reqJsonObject")
            val version = getApiVersion(urll.toString())
            reqJsonObject?.put("device", "android")
            reqJsonObject?.put("variant", version)
        setLog("final_url", "url:${urll} jsonObject:$reqJsonObject")

        val finalURL = getAPIUrl(urll.toString(), Constant.COUNTRY_CODE)
        setLog("API_request", "$finalURL")

            val cacheRequest: JsonObjectRequest = object : JsonObjectRequest(Request.Method.PUT, finalURL,
                reqJsonObject, Response.Listener { response -> //    showData(response.toString());
//                   child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
                    dataValues.setJsonDataResponse(response)
                }, Response.ErrorListener { error -> //showData(error.toString());
//                     child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
                    dataValues.setVolleyError(error)
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params.put(
                        WSConstants.WS_HEADER_CONTENT_TYPE,
                        WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                    )
                    params.put(
                        WSConstants.PAY_API_KEY,
                        WSConstants.PAY_API_KEY_VALUE,
                    )
                    setLog("finalURL", "finalURL :${url} jsonObject:${reqJsonObject} params:${params}")
                    return params
                }

            }
            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)

    }

    suspend fun getStringVolleyRequest(
        context: Context?,
        url: String?,
        reqJsonObject: JSONObject?,
        dataValues: DataValues
    ) {
//        val currentSpan = Sentry.getSpan()
//         val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")
            val mlang= SharedPrefHelper.getInstance().get(Constant.APPMUSICLANG,default_music_language_code)
            val vlang= SharedPrefHelper.getInstance().get(Constant.APPVIDEOLANG,default_video_language_code)
            val userId= SharedPrefHelper.getInstance().getUserId()

            val version = getApiVersion(url.toString())
            var finalURL=""
            if(url?.contains("?",true)!!){
                finalURL=url+"&alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }else{
                finalURL=url+"?alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }

        finalURL = getAPIUrl(finalURL , Constant.COUNTRY_CODE)
        setLog("API_request", "$finalURL")

            val cacheRequest: StringRequest = object : StringRequest(Method.GET, finalURL, Response.Listener { response -> //    showData(response.toString());
//               child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
                dataValues.setJsonStringDataResponse(response)
            }, Response.ErrorListener { error -> //showData(error.toString());
//                 child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
                dataValues.setVolleyError(error)
            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params.put(
                        WSConstants.WS_HEADER_CONTENT_TYPE,
                        WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                    )
                    params.put(
                        WSConstants.PAY_API_KEY,
                        WSConstants.PAY_API_KEY_VALUE,
                    )
                    if(MainActivity.lastBottomItemPosClicked==0){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "home",
                        )
                    }else if(MainActivity.lastBottomItemPosClicked==1){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "listen",
                        )
                    }else if(MainActivity.lastBottomItemPosClicked==2){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "watch",
                        )
                    }

                    setLog("TAG", "getStringVolleyRequest: finalURL :${finalURL} params:${params} Thread:${Thread.currentThread().name}")
                    return params
                }
            }

            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)

    }

    suspend fun getVolleyRequest(
        context: Context?,
        url: String?,
        reqJsonObject: JSONObject?,
        dataValues: DataValues
    ) {


//        val currentSpan = Sentry.getSpan()
//        val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")

            val mlang= SharedPrefHelper.getInstance().get(Constant.APPMUSICLANG,default_music_language_code)
            val vlang= SharedPrefHelper.getInstance().get(Constant.APPVIDEOLANG,default_video_language_code)
            val userId= SharedPrefHelper.getInstance().getUserId()

            val version = getApiVersion(url.toString())
            var finalURL=""

        var deviceType:String="android"
        if(url?.contains("playable") == true){
            deviceType= "android1"
        }
        else if (url?.contains("/banner") ==  true)
            deviceType = "android"
        else
            deviceType = Constant.API_DEVICE_TYPE

            if(url?.contains("brandhub",true)!! || url?.contains("180.179.149.11",true)!!|| url?.contains("202.87.41.147",true)!! || url.contains("users/recent",true)!! || url?.contains("getstore.php",true)!! || url?.contains("get-store",true)!!|| url?.contains("musiclang",true)!!|| url?.contains("videolang",true)!!|| url?.contains("applang",true)!!|| url?.contains("preference/type",true)!!){
                finalURL=url
            }else{
                if(url?.contains("?",true)!!){
                    finalURL=url+"&alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${deviceType}&variant="+version+ (if(url.contains("uid",true)) "" else "&uid=" +userId)+"&storeId="+Constant.DEFAULT_STORE_ID+"&appVersion="+BuildConfig.VERSION_CODE.toString()
                }else{
                    finalURL=url+"?alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${deviceType}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID+"&appVersion="+ BuildConfig.VERSION_CODE.toString()
                }
            }
        finalURL = getAPIUrl(finalURL , Constant.COUNTRY_CODE)

        setLog("API_request", " " + finalURL)

            val cacheRequest: StringRequest = object : StringRequest(Method.GET, finalURL, Response.Listener { response -> //    showData(response.toString());
//                child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
                try {
                    dataValues.setJsonDataResponse(JSONObject(response))
//                    setLog("alhgkfsa","$response")
                }
                catch (e:Exception){
//                    setLog("alhgkfsa"," try ${e.cause}")
                }

            }, Response.ErrorListener { error -> //showData(error.toString());
//                child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
                dataValues.setVolleyError(error)
                setLog("alhgkfsa"," error ${error.cause}")
            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params.put(
                        WSConstants.WS_HEADER_CONTENT_TYPE,
                        WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                    )
                    params.put(
                        WSConstants.PAY_API_KEY,
                        WSConstants.PAY_API_KEY_VALUE,
                    )
                    if(MainActivity.lastBottomItemPosClicked==0){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "home",
                        )
                    }else if(MainActivity.lastBottomItemPosClicked==1){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "listen",
                        )
                    }else if(MainActivity.lastBottomItemPosClicked==2){
                        params.put(
                            WSConstants.HEADER_IDENTIFIER_KEY,
                            "watch",
                        )
                    }
                    setLog("TAG", "getVolleyRequest: finalURL :${finalURL} params:${params} Thread:${Thread.currentThread().name}")
                    return params
                }

                /*override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                    var cacheEntry: Cache.Entry? = HttpHeaderParser.parseCacheHeaders(response)
                    if (cacheEntry == null) {
                        cacheEntry = Cache.Entry()
                    }
                    val cacheHitButRefreshed =
                        (3 * 60 * 1000).toLong() // in 3 minutes cache will be hit, but also refreshed on background
                    val cacheExpired =
                        (24 * 60 * 60 * 1000).toLong() // in 24 hours this cache entry expires completely
                    val now = System.currentTimeMillis() // Current Time
                    val softExpire =
                        now + cacheHitButRefreshed // sum of current time and cache refresh rate
                    val ttl = now + cacheExpired // sum of current time and chacheExpiry time
                    cacheEntry.data = response?.data
                    cacheEntry.softTtl = softExpire
                    cacheEntry.ttl = ttl
                    var headerValue: String
                    headerValue = response?.headers?.get("Date").toString()
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue)
                    }
                    headerValue = response?.headers?.get("Last-Modified").toString()
                    if (headerValue != null || headerValue != "null") {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue)
                    }
                    cacheEntry.responseHeaders = response?.headers
                    Log.d("DataManager...", "From Cache")
                    return try {
                        val json = String(
                            cacheEntry.data,
                            charset(HttpHeaderParser.parseCharset(response?.headers))
                        )
                        Response.success(json, cacheEntry)
                    } catch (e: UnsupportedEncodingException) {
                        Response.error(ParseError(e))
                    } catch (e: JsonSyntaxException) {
                        Response.error(ParseError(e))
                    }
                }*/
            }



            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)


    }

    suspend fun postVolleyRequestWithFormData(
        context: Context?,
        url: String?,
        dataValues: DataValues
    ) {
            val cacheRequest: StringRequest = object : StringRequest(
                Method.POST,url,
                Response.Listener { response ->
                    setLog("TAG", "postVolleyRequestWithFormData response : ${response}")
                    if(!response?.isNullOrBlank()!!){
                        dataValues.setJsonDataResponse(JSONObject(response))
                    }else{
                        dataValues.setJsonDataResponse(JSONObject())
                    }

                },
                Response.ErrorListener { volleyError -> // error occurred
                    setLog("TAG", "ErrorListener volleyError: ${volleyError}")
                    dataValues.setVolleyError(volleyError)
                }) {

                override fun getParams(): MutableMap<String, String> {
                    val params: HashMap<String, String> = HashMap()
                    params.put("user_id",SharedPrefHelper.getInstance().getUserId()!!)

                    setLog("TAG", "getParams params: ${params}")
                    return params
                }

                override fun getBodyContentType(): String {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }
            }
            cacheRequest.retryPolicy = DefaultRetryPolicy(
                WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)



    }

    fun getVolleyRequestAuto(
        context: Context?,
        url: String?,
        reqJsonObject: JSONObject?,
        dataValues: DataValues
    ) {

        val mlang= SharedPrefHelper.getInstance().get(Constant.APPMUSICLANG,default_music_language_code)
        val vlang= SharedPrefHelper.getInstance().get(Constant.APPVIDEOLANG,default_video_language_code)
        val userId= SharedPrefHelper.getInstance().getUserId()

        val version = getApiVersion(url.toString())
        var finalURL=""


        if(url?.contains("brandhub",true)!! || url?.contains("180.179.149.11",true)!!|| url?.contains("202.87.41.147",true)!! || url.contains("users/recent",true)!! || url?.contains("getstore.php",true)!! || url?.contains("get-store",true)!!|| url?.contains("musiclang",true)!!|| url?.contains("videolang",true)!!|| url?.contains("applang",true)!!|| url?.contains("preference/type",true)!!){
            finalURL=url
        }else{
            if(url?.contains("?",true)!!){
                finalURL=url+"&alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }else{
                finalURL=url+"?alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
            }
        }

        finalURL = getAPIUrl(finalURL , Constant.COUNTRY_CODE)
        setLog("API_request","getVolleyRequestAuto: finalURL :${finalURL}")
        val cacheRequest: StringRequest = object : StringRequest(Method.GET, finalURL, Response.Listener { response -> //    showData(response.toString());
            setLog("TAG", "getVolleyRequest: response :${JSONObject(response)}")
            dataValues.setJsonDataResponse(JSONObject(response))
        }, Response.ErrorListener { error ->
            setLog("TAG", "getVolleyRequest: error :${error.message}")
            dataValues.setVolleyError(error)
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params.put(
                    WSConstants.WS_HEADER_CONTENT_TYPE,
                    WSConstants.WS_HEADER_CONTENT_TYPE_JSON
                )
                params.put(
                    WSConstants.PAY_API_KEY,
                    WSConstants.PAY_API_KEY_VALUE,
                )
                if(MainActivity.lastBottomItemPosClicked==0){
                    params.put(
                        WSConstants.HEADER_IDENTIFIER_KEY,
                        "home",
                    )
                }else if(MainActivity.lastBottomItemPosClicked==1){
                    params.put(
                        WSConstants.HEADER_IDENTIFIER_KEY,
                        "listen",
                    )
                }else if(MainActivity.lastBottomItemPosClicked==2){
                    params.put(
                        WSConstants.HEADER_IDENTIFIER_KEY,
                        "watch",
                    )
                }

                setLog("TAG", "getVolleyRequest: finalURL :${finalURL} params:${params} Thread:${Thread.currentThread().name}")
                return params
            }
        }



        cacheRequest.retryPolicy = DefaultRetryPolicy(
            WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)


    }

    fun getVolleyRequestArrayAuto(
        context: Context?,
        url: String?,
        dataValues: DataValues
    ) {
//        val currentSpan = Sentry.getSpan()
//         val child = currentSpan?.startChild(url!!, javaClass.simpleName)
//            ?: Sentry.startTransaction(url!!, "task")
        val mlang= SharedPrefHelper.getInstance().get(Constant.APPMUSICLANG,default_music_language_code)
        val vlang= SharedPrefHelper.getInstance().get(Constant.APPVIDEOLANG,default_video_language_code)
        val userId= SharedPrefHelper.getInstance().getUserId()
        val version = getApiVersion(url.toString())

        var finalURL=""
        if(url?.contains("countries",true)!!){
            finalURL=url
        }else if(url?.contains("?",true)!!){
            finalURL=url+"&alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
        }else{
            finalURL=url+"?alang="+ SharedPrefHelper.getInstance().getLanguage()+"&vlang="+vlang+"&mlang="+mlang+"&platform=a&device=${Constant.API_DEVICE_TYPE}&variant="+version+"&uid="+userId+"&storeId="+Constant.DEFAULT_STORE_ID
        }

        finalURL = getAPIUrl(finalURL , Constant.COUNTRY_CODE)

        setLog("API_request","getVolleyRequestArray: finalURL :${finalURL}")

        val cacheRequest = JsonArrayRequest(finalURL,
            { jsonArray -> dataValues.setJsonArrayDataResponse(jsonArray)
//                   child?.finish(SpanStatus.OK)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.OK)
            }
        ) { error -> //showData(error.toString());
            dataValues.setVolleyError(error)
//                 child?.finish(SpanStatus.UNKNOWN_ERROR)
//                Sentry.captureException(error!!)
//                val transaction = Sentry.getSpan()
//                transaction?.finish(SpanStatus.INTERNAL_ERROR)
        }


        cacheRequest.retryPolicy = DefaultRetryPolicy(
            WSConstants.WS_CONNECTION_TIMEOUT_INT.toInt(),
            WSConstants.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        VolleySinglton.getInstance(context!!)?.addToRequestQueue(cacheRequest)

    }


    public fun getApiVersion(url:String): String{
        val pattern2 = Regex("""/v\d+""")
        val ver : Sequence<MatchResult> = pattern2.findAll(url, 0)
        var version = ""
        ver.forEach() {
                matchResult -> version = matchResult.value
        }
        if (!TextUtils.isEmpty(version)){
            //version = version.replace("/$", "")
            version=version.replace("/","",true)
            return version
        }else{
            return "v1"
        }
    }

    fun getAPIUrl(baseUrl:String, url:String):String{
        return if (baseUrl.contains("?")) "$baseUrl&$url" else "$baseUrl?$url"
    }
}