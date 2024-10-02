package  com.hungama.music.data.webservice.utils

import com.hungama.music.data.model.ErrorPropertiesModel

data class Resource<out T>(val status: Status, val data: T?, val message: String?,val error_pro:ErrorPropertiesModel?=null) {

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?,error_pro: ErrorPropertiesModel?=null): Resource<T> {
            return Resource(Status.ERROR, data, msg,error_pro)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

    }

}