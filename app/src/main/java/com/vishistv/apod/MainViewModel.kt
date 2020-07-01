package com.vishistv.apod

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vishistv.apod.datafiles.Apod
import com.vishistv.apod.extension.apiRx
import com.vishistv.apod.extension.toast
import com.vishistv.apod.network.ApiRepository
import com.vishistv.apod.network.Env
import com.vishistv.apod.util.Constants
import com.vishistv.apod.util.Log
import java.lang.Exception

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "MainViewModel"
    }

    private val _getApod = MutableLiveData<Apod>()

    val getApod: LiveData<Apod> = _getApod

    fun fetchApod(date: String?) {
        val option = mutableMapOf(Constants.API_KEY to Env.API_KEY)

        date?.let {
            option[Constants.DATE] = date
        }

        apiRx<Apod> {
            request = ApiRepository.getApod(option)
            success { apod ->
                _getApod.postValue(apod)
            }
            error {
                _getApod.postValue(null)
                try {
                    Log.d(TAG, "👺 ${it.localizedMessage}")
                } catch (e: Exception) {
                    _getApod.postValue(null)
                }
            }
        }
    }
}