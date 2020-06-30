package com.vishistv.apod

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vishistv.apod.datafiles.Apod
import com.vishistv.apod.extension.apiRx
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
    private val _isFetchingApod = MutableLiveData<Boolean>()

    val getApod: LiveData<Apod> = _getApod
    val isFetchingApod: LiveData<Boolean> = _isFetchingApod

    fun fetchApod(date: String?) {
        val option = mutableMapOf(Constants.API_KEY to Env.API_KEY)

//        date?.let {
        option[Constants.DATE] = "2016-11-05"
//        }

        apiRx<Apod> {
            request = ApiRepository.getApod(option)

            success { apod ->
                _isFetchingApod.postValue(false)
                _getApod.postValue(apod)
            }
            error {
                _isFetchingApod.postValue(false)
                try {
                    Log.d(TAG, "👺 ${it.localizedMessage}")
                } catch (e: Exception) {

                }
            }
        }
    }
}