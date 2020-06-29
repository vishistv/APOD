package com.vishistv.apod.extension

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

fun <T> ViewModel.apiRx(init: RequestWrapper<T>.() -> Unit) {
    val wrap = RequestWrapper<T>()
    init.invoke(wrap)
    call(wrap)
}

private fun <T> call(wrap: RequestWrapper<T>) {
    wrap.request?.subscribeOn(Schedulers.io())
        ?.observeOn(AndroidSchedulers.mainThread())
        ?.subscribe(object : DisposableSingleObserver<T>() {
            override fun onSuccess(t: T) {
                this.dispose()
                wrap.success(t)
            }

            override fun onError(e: Throwable) {
                this.dispose()
                wrap.fail(e)
            }
        })
}

class RequestWrapper<T> : LifecycleObserver {

    var request: Single<T>? = null

    internal var success: (T) -> Unit = {}
    internal var fail: (Throwable) -> Unit = {}

    fun success(onSuccess: (T) -> Unit) {
        success = onSuccess
    }

    fun error(onError: (Throwable) -> Unit) {
        fail = onError
    }
}

