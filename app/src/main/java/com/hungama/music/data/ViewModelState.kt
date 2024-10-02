package com.hungama.music.data

import androidx.lifecycle.MutableLiveData
import com.hungama.music.ui.main.viewmodel.ErrorData

sealed class ViewModelState {
    object Loading : ViewModelState()
    object Success : ViewModelState()
    object Error : ViewModelState()
    object OurOfferingDetails : ViewModelState()
}

data class Data<T>(
    val viewModelState: ViewModelState,
    val data: T? = null,
    val stringState: String? = null,
    var boolState: Boolean = false,
    var intState: Int = 0,
    var errorData: ErrorData? = null,
    val header: String? = null
)

fun <T> MutableLiveData<Data<T>>.setSuccess(data: T? = null) {
    value = Data(ViewModelState.Success, data)
}

fun <T> MutableLiveData<Data<T>>.setSuccess(data: T? = null, header: String?) {
    value = Data(ViewModelState.Success, data, "", false, 0, null, header)
}

fun <T> MutableLiveData<Data<T>>.setLoading() {
    value = Data(ViewModelState.Loading, value?.data)
}

fun <T> MutableLiveData<Data<T>>.offeringDetails(
    offeringClickPosition: Int,
    shouldShow: Boolean,
    insertItem: String = ""
) {
    value = Data(
        ViewModelState.OurOfferingDetails,
        value?.data,
        intState = offeringClickPosition,
        boolState = shouldShow,
        stringState = insertItem
    )
}

fun <T> MutableLiveData<Data<T>>.setError(message: String? = null) {
    value = Data(ViewModelState.Error, value?.data, message)
}

fun <T> MutableLiveData<Data<T>>.setError(message: ErrorData? = null) {
    value = Data(ViewModelState.Error, value?.data, "", false, 0, message)
}

fun <T> MutableLiveData<Data<T>>.setError(message: ErrorData? = null, header: String?) {
    value = Data(ViewModelState.Error, value?.data, "", false, 0, message, header)
}

fun <T> MutableLiveData<Data<T>>.getData(): T? = value?.data