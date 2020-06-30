package com.vishistv.apod.extension

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import kotlin.coroutines.CoroutineContext

fun <T> ViewModel.apiCx(context: CoroutineContext = Dispatchers.Default, init: suspend CxWrapper<T>.() -> Unit) {
    val wrap = CxWrapper<T>(context)
    wrap.launch {
        try {
            init.invoke(wrap)
            callCx(wrap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun <T> callCx(wrap: CxWrapper<T>) {
    val response: Response<T>? = wrap.request

    response?.let {
        if (it.isSuccessful) {
            wrap.success(it.body())
        } else {
            wrap.fail(Pair(it.code(), it.message()))
        }
    }
}

class CxWrapper<T>(override val coroutineContext: CoroutineContext) : CoroutineScope {

    var request: Response<T>? = null

    internal var success: (T?) -> Unit = {}
    internal var fail: (Pair<Int, String?>) -> Unit = {}

    fun success(onSuccess: (T?) -> Unit) {
        success = onSuccess
    }

    fun error(onError: (Pair<Int, String?>) -> Unit) {
        fail = onError
    }
}


